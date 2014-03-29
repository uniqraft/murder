package net.minecraftmurder.main;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;

import net.minecraftmurder.inventory.MInventory;
import net.minecraftmurder.managers.PlayerManager;
import net.minecraftmurder.matches.Match;
import net.minecraftmurder.tools.ChatContext;
import net.minecraftmurder.tools.MLogger;
import net.minecraftmurder.tools.Paths;
import net.minecraftmurder.tools.SimpleFile;
import net.minecraftmurder.tools.Tools;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MPlayer {
	public final static int RELOAD_TIME = 40;

	private String name;
	private Match match;

	private MPlayerClass playerClass;
	private MInventory inventory;

	private String killerName;

	/** When at 0, the player can fire their gun */
	private int reloadTime = 0;
	/** When at 0, the player can pickup a gun */
	private int gunBanTime = 0;

	public MPlayer(String name) {
		this.name = name;

		inventory = new MInventory(this);
	}

	public MPlayerClass getPlayerClass() {
		return playerClass;
	}

	public void switchPlayerClass(MPlayerClass playerClass) {
		this.playerClass = playerClass;
		MPlayerClass.setDefaultClassInventory(this, playerClass);
		Player me = getPlayer();
		// If I was turned into a spectator
		if (playerClass == MPlayerClass.SPECTATOR) {
			// Make all players unable to see me
			me.setGameMode(GameMode.CREATIVE);
			for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
				// Don't run on self
				if (otherPlayer == me)
					continue;
				otherPlayer.hidePlayer(me);
			}
		} else {
			// Make all players able to see me
			me.setGameMode(GameMode.ADVENTURE);
			for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
				// Don't run on self
				if (otherPlayer == me)
					continue;
				otherPlayer.showPlayer(me);
			}
		}

		MPlayerClass.setFoodLevel(this);
		me.setHealth(20);
		for (PotionEffect effect : me.getActivePotionEffects())
			me.removePotionEffect(effect.getType());
	}

	public void onDeath() {
		Player player = getPlayer();

		final LivingEntity zombie = (LivingEntity) player.getWorld()
				.spawnEntity(player.getLocation(), EntityType.ZOMBIE);
		EntityEquipment equipment = zombie.getEquipment();
		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
		((SkullMeta) skull.getItemMeta()).setOwner(player.getName());
		equipment.setHelmet(skull);
		Bukkit.getScheduler().scheduleSyncDelayedTask(Murder.getInstance(),
				new Runnable() {
					@Override
					public void run() {
						zombie.setHealth(0);
					}
				}, 1L);

		MPlayerClass.setFoodLevel(this);
		player.setHealth(20);
		for (PotionEffect effect : player.getActivePotionEffects())
			player.removePotionEffect(effect.getType());

		match.onPlayerDeath(PlayerManager.getMPlayer(player));
	}

	public void decreaseGunBanTime() {
		gunBanTime--;
	}

	public int getGunBanTime() {
		return gunBanTime;
	}

	public void gunBan() {
		// Drop gun, then turn the player into an innocent
		Player player = getPlayer();
		player.getWorld().dropItem(player.getLocation(),
				new ItemStack(MPlayerClass.MATERIAL_GUN));
		player.sendMessage(ChatContext.MESSAGE_SHOTINNOCENT);
		player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 60,
				2), true);
		switchPlayerClass(MPlayerClass.INNOCENT);
		gunBanTime = Murder.GUNBAN_TIME;
	}

	public String getName() {
		return name;
	}

	public void setMatch(Match match) {
		if (this.match != null)
			this.match.onPlayerQuit(this); // Tell current match that
													// this player left
		this.match = match;
		this.match.onPlayerJoin(this); // Tell new match that this player
												// joined
	}

	public Match getMatch() {
		return match;
	}

	public Player getPlayer() {
		return Bukkit.getPlayer(name);
	}

	public int getReloadTime() {
		return reloadTime;
	}

	public void setReloadTime(int reloadTime) {
		this.reloadTime = reloadTime;
		Player player = getPlayer();
		player.setExp(1 - ((float) reloadTime / (float) RELOAD_TIME));
		if (reloadTime <= 0) {
			player.playSound(player.getLocation(), Sound.LAVA_POP, 1, 1.8f);
		}
	}

	public void addReloadTime(int reloadTime) {
		setReloadTime(getReloadTime() + reloadTime);
	}

	public String getKillerName() {
		return killerName;
	}

	public void setKiller(Player killer) {
		this.killerName = killer.getName();
	}

	public void setKiller(String killerName) {
		this.killerName = killerName;
	}

	public static boolean addCoins(String player, int count, boolean tell) {
		return addCoins(player, count, true, false);
	}

	public static boolean addCoins(String player, int count, boolean tell,
			boolean ignoreMultiplier) {
		MPlayer mPlayer = PlayerManager.getMPlayer(player);
		// TODO Fixed error, but not optimal. Now only /online/ VIP players get double coins.
		boolean vip = (mPlayer != null && mPlayer.getPlayer().hasPermission("murder.vip") && !ignoreMultiplier && count > 0);

		if (vip)
			count *= 2;

		if (mPlayer != null && tell) {
			if (count >= 0) {
				mPlayer.getPlayer().sendMessage(
						ChatContext.PREFIX_PLUGIN
								+ ChatContext.COLOR_LOWLIGHT
								+ "You earned "
								+ ChatContext.COLOR_HIGHLIGHT
								+ ((vip) ? (int) (count / 2d) : count)
								+ ChatContext.COLOR_LOWLIGHT
								+ (count != 1 ? " coins" : " coin")
								+ ((vip) ? (ChatContext.COLOR_INNOCENT
										+ " (x2 = " + count + ")"
										+ ChatContext.COLOR_LOWLIGHT + "!")
										: "!"));
			} else {
				mPlayer.getPlayer().sendMessage(
						ChatContext.PREFIX_PLUGIN + ChatContext.COLOR_LOWLIGHT
								+ "You lost " + ChatContext.COLOR_HIGHLIGHT
								+ Math.abs(count) + ChatContext.COLOR_LOWLIGHT
								+ (count != 1 ? " coins" : " coin") + "!");
			}
		}

		return setCoins(player, getCoins(player) + count, false);
	}

	public static boolean setCoins(String player, int count, boolean tell) {
		YamlConfiguration config = SimpleFile.loadConfig(Paths.FOLDER_PLAYERS
				+ player + ".yml");
		config.set("coins", count);
		// If player is online, update him
		MPlayer mPlayer = PlayerManager.getMPlayer(player);
		if (mPlayer != null) {
			if (tell)
				mPlayer.getPlayer().sendMessage(
						ChatContext.PREFIX_PLUGIN + ChatContext.COLOR_LOWLIGHT
								+ "You now have " + ChatContext.COLOR_HIGHLIGHT
								+ Math.abs(count) + ChatContext.COLOR_LOWLIGHT
								+ (count != 1 ? " coins" : " coin") + "!");
		}
		return SimpleFile.saveConfig(config, Paths.FOLDER_PLAYERS + player
				+ ".yml");
	}

	public static int getCoins(String player) {
		String path = Paths.FOLDER_PLAYERS + player + ".yml";
		if (!SimpleFile.exists(path))
			return 0;
		YamlConfiguration config = SimpleFile.loadConfig(path);
		return config.getInt("coins", 0);
	}

	public static boolean addWarnLevel(String player, int level) {
		int totalLevel = getWarnLevel(player) + level;
		Player p = Bukkit.getPlayer(player);
		if (p != null) {
			p.kickPlayer("You were punished for inappropriate behavior.");
		}
		setUpcomingBan(player, p == null);

		MLogger.log(Level.INFO, player + "'s warn level was increased by "
				+ level);

		YamlConfiguration config = SimpleFile.loadConfig(Paths.FOLDER_PLAYERS
				+ player + ".yml");
		config.set("warn.level", totalLevel);
		Date date = calculateBanDate(totalLevel);
		config.set("warn.date", Tools.dateToString(date, "-"));
		MLogger.log(Level.INFO, "Banned until " + date.toString());
		return SimpleFile.saveConfig(config, Paths.FOLDER_PLAYERS + player
				+ ".yml");
	}

	public static Date calculateBanDate(int level) {
		GregorianCalendar calendar = new GregorianCalendar();
		// (x / 5)^8 * 10 + x*5 - 20
		int l = (int) Math.round(Math.pow((double) level / 5d, 8d) * 10 + level
				* 5 - 20);
		MLogger.log(Level.INFO, "Ban time in minutes: " + Math.max(l, 0d));
		calendar.add(Calendar.MINUTE, l);
		return calendar.getTime();
	}

	public static boolean isBanned(String player) {
		YamlConfiguration config = SimpleFile.loadConfig(Paths.FOLDER_PLAYERS
				+ player + ".yml");
		boolean toBeBanned = config.getBoolean("warn.upcomingban", false);
		if (toBeBanned) {
			Date date = calculateBanDate(config.getInt("warn.level", 0));
			config.set("warn.date", Tools.dateToString(date, "-"));
			config.set("warn.upcomingban", false);
			MLogger.log(Level.INFO, "Played had upcomming ban, banned until: "
					+ date.toString());
		}
		String dateString = config.getString("warn.date");
		if (dateString == null || "".equalsIgnoreCase(dateString))
			return false;

		SimpleFile.saveConfig(config, Paths.FOLDER_PLAYERS + player + ".yml");
		Date date = Tools.stringToDate(dateString, "-");
		return new GregorianCalendar().getTime().before(date);
	}

	/**
	 * Gets the date this player is banned until. If the player has never been
	 * banned before, year 0 month 0 day 0 will be returned. If the player has
	 * been banned before, their previous ban date will be returned, even if the
	 * ban has expired.
	 * 
	 * @param player
	 *            The player's name
	 * @return Previous ban date, if any, otherwise year 0 month 0 day 0
	 */
	public static Date getBanDate(String player) {
		return Tools.stringToDate(
				SimpleFile.loadConfig(Paths.FOLDER_PLAYERS + player + ".yml")
						.getString(
								"warn.date",
								Tools.dateToString(new GregorianCalendar(0, 0,
										0).getTime(), "-")), "-");
	}

	public static int getWarnLevel(String player) {
		return SimpleFile.loadConfig(Paths.FOLDER_PLAYERS + player + ".yml")
				.getInt("warn.level", 0);
	}

	/**
	 * Sets whether a ban should be given to the user when they log in. This
	 * should be set to true if the user is offline when they receive their
	 * warning, otherwise false.
	 * 
	 * @param shouldBeBanned
	 *            Whether or not the user should receive their ban date when
	 *            they log in.
	 * @param player
	 *            The player's name.
	 * @return Whether or not the info could be saved to disk.
	 */
	public static boolean setUpcomingBan(String player, boolean shouldBeBanned) {
		YamlConfiguration config = SimpleFile.loadConfig(Paths.FOLDER_PLAYERS
				+ player + ".yml");
		config.set("warn.upcomingban", shouldBeBanned);
		return SimpleFile.saveConfig(config, Paths.FOLDER_PLAYERS + player
				+ ".yml");
	}

	public static boolean getUpcomingBan(String player) {
		return SimpleFile.loadConfig(Paths.FOLDER_PLAYERS + player + ".yml")
				.getBoolean("warn.upcomingban", false);
	}

	public MInventory getMInventory() {
		return inventory;
	}
}