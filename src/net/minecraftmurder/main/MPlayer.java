package net.minecraftmurder.main;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;

import net.minecraftmurder.main.MPlayerClass.PlayerClass;
import net.minecraftmurder.managers.PlayerManager;
import net.minecraftmurder.matches.Match;
import net.minecraftmurder.tools.ChatContext;
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
	public final static int RELOAD_TIME = 25;
	
	private String name;
	private Match match;
	private PlayerClass playerClass;
	
	private String killerName;
	
	private int coins;
	
	private int reloadTime = 0;
	private int gunBanTime = 0;
	
	private Murder plugin;
	
	// For future reference
	private Material knife = Material.IRON_SWORD;
	
	public MPlayer (String name, Murder plugin) {
		this.name = name;
		this.plugin = plugin;
		
		switchClass(PlayerClass.LOBBYMAN);
		load();
	}
	
	public void load () {
		YamlConfiguration config = SimpleFile.loadConfig(PlayerManager.PATH_PLAYERS + "/" + name + ".yml");
		coins = config.getInt("coins", 0);
	}
	
	public void save () {
		// TODO Remove, maybe?
		/*YamlConfiguration config = SimpleFile.loadConfig(PlayerManager.PATH_PLAYERS + "/" + name + ".yml");
		config.set("points", coins);
		SimpleFile.saveConfig(config, PlayerManager.PATH_PLAYERS + "/" + name + ".yml");*/
	}
	
	public PlayerClass getPlayerClass () {
		return playerClass;
	}
	
	public void switchClass (PlayerClass lobbyman) {
		this.playerClass = lobbyman;
		MPlayerClass.setDefaultClassInventory(getPlayer().getInventory(), lobbyman);
		// If I was turned into a spectator
		if (lobbyman == MPlayerClass.PlayerClass.SPECTATOR) {
			// Make all players unable to see me
			Player me = plugin.getPlayerManager().getPlayer(this);
			me.setGameMode(GameMode.CREATIVE);
			for (Player otherPlayer: Bukkit.getOnlinePlayers()) {
				// If they can see me, hide me
				if (otherPlayer.canSee(me))
					otherPlayer.hidePlayer(me);
			}
		} else {
			// Make all players able to see me
			Player me = getPlayer();
			me.setGameMode(GameMode.ADVENTURE);
			for (Player otherPlayer: Bukkit.getOnlinePlayers()) {
				// Don't run on self
				if (otherPlayer == me)
					continue;
				// If they can't see me, show me
				if (!otherPlayer.canSee(me))
					otherPlayer.showPlayer(me);
			}
		}
		
		// Update food level
		MPlayerClass.setFoodLevel(this);
	}
	
	public void onDeath () {
		Player player = getPlayer();
		
<<<<<<< HEAD
		// This isn't even a line? :
		// Packet14SpawnNamedEntity
=======
		LivingEntity zombie = (LivingEntity)player.getWorld().spawnEntity(player.getLocation(), EntityType.ZOMBIE);
		EntityEquipment equipment = zombie.getEquipment();
		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
		((SkullMeta) skull.getItemMeta()).setOwner(player.getName());
		equipment.setHelmet(skull);
		zombie.setHealth(0);
>>>>>>> 67f265d479d44823c020d3c65638a2649bf01049
		
		player.setHealth(20);
		match.onPlayerDeath(player);
	}
	
	public void decreaseGunBanTime () {
		gunBanTime--;
	}
	
	public int getGunBanTime () {
		return gunBanTime;
	}
	
	public void gunBan () {
		// Drop gun, then turn the player into an innocent
		Player player = getPlayer();
		player.getWorld().dropItem(player.getLocation(), new ItemStack(MPlayerClass.MATERIAL_GUN));
		player.sendMessage(ChatContext.MESSAGE_SHOTINNOCENT);
		player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 60, 2), true);
		switchClass(PlayerClass.INNOCENT);
		gunBanTime = Murder.GUNBAN_TIME;
	}
	
	public String getName () {
		return name;
	}
	
	public Match getMatch () {
		return match;
	}
	
	public void setMatch (Match match) {
		if (this.match != null)
			this.match.onPlayerQuit(getPlayer()); // Tell current match that this player left
		this.match = match;
		this.match.onPlayerJoin(getPlayer()); // Tell new match that this player joined
	}
	
	public Player getPlayer () {
		return Bukkit.getPlayer(name);
	}

	public int getReloadTime() {
		return reloadTime;
	}
	public void setReloadTime(int reloadTime) {
		this.reloadTime = reloadTime;
		Player player = getPlayer();
		player.setExp(1 - ((float)reloadTime / (float)RELOAD_TIME));
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
	
	public static boolean addCoins (String player, int count, boolean tell, Murder plugin) {
		MPlayer mplayer = plugin.getPlayerManager().getMPlayer(player);
		if (mplayer != null && tell) {
			mplayer.getPlayer().sendMessage(ChatContext.PREFIX_PLUGIN + ChatContext.COLOR_LOWLIGHT + "You earned " + ChatContext.COLOR_HIGHLIGHT + count + " coins" + ChatContext.COLOR_LOWLIGHT + "!");
		}
		return setCoins(player, getCoins(player) + count, false, plugin);
	}
	public static boolean setCoins (String player, int count, boolean tell, Murder plugin) {
		YamlConfiguration config = SimpleFile.loadConfig(PlayerManager.PATH_PLAYERS + "/" + player + ".yml");
		config.set("coins", count);
		// If player is online, update him
		MPlayer mplayer = plugin.getPlayerManager().getMPlayer(player);
		if (mplayer != null) {
			if (tell)
				mplayer.getPlayer().sendMessage(ChatContext.PREFIX_PLUGIN + ChatContext.COLOR_LOWLIGHT + "You now have " + ChatContext.COLOR_HIGHLIGHT + count + " coins" + ChatContext.COLOR_LOWLIGHT + "!");
			mplayer.load();
		}
		return SimpleFile.saveConfig(config, PlayerManager.PATH_PLAYERS + "/" + player + ".yml");
	}
	public static int getCoins (String player) {
		YamlConfiguration config = SimpleFile.loadConfig(PlayerManager.PATH_PLAYERS + "/" + player + ".yml");
		return config.getInt("coins", 0);
	}
	
	public static boolean addWarnLevel (String player, int level) {
		int totalLevel = getWarnLevel(player) + level;
		Player p = Bukkit.getPlayer(player);
		if (p != null)
			p.kickPlayer("You were punished for inappropriate behavior.");
		setUpcomingBan(player, p == null);
		
		Bukkit.getLogger().log(Level.INFO, player + "'s warn level was increased by " + level);
		
		YamlConfiguration config = SimpleFile.loadConfig(PlayerManager.PATH_PLAYERS + "/" + player + ".yml");
		config.set("warn.level", totalLevel);
		config.set("warn.date", Tools.dateToString(calculateBanDate(totalLevel), "-"));
		return SimpleFile.saveConfig(config, PlayerManager.PATH_PLAYERS + "/" + player + ".yml");
	}
	public static Date calculateBanDate (int level) {
		GregorianCalendar calendar = new GregorianCalendar();
		Bukkit.getLogger().log(Level.INFO, "Time now: " + calendar.getTime().toString());
		int l = (int) Math.pow(level / 2, 2) * 10;
		Bukkit.getLogger().log(Level.INFO, "Ban time in minutes: " + l);
		calendar.add(Calendar.MINUTE, l);
		Bukkit.getLogger().log(Level.INFO, "Ban time now: " + calendar.getTime().toString());
		return calendar.getTime();
	}
	public static boolean isBanned (String player) {
		YamlConfiguration config = SimpleFile.loadConfig(PlayerManager.PATH_PLAYERS + "/" + player + ".yml");
		boolean toBeBanned = config.getBoolean("warn.upcomingban", false);
		if (toBeBanned) {
			config.set("warn.date", Tools.dateToString(calculateBanDate(config.getInt("warn.level", 0)), "-"));
		}
		String dateString = config.getString("warn.date");
		if (dateString == null || "".equalsIgnoreCase(dateString))
			return false;
		
		Date date = Tools.stringToDate(dateString, "-");
		Bukkit.getLogger().log(Level.INFO, "BanTime: " + date.toString());
		Bukkit.getLogger().log(Level.INFO, "NowTime: " + new GregorianCalendar().getTime().toString());
		return new GregorianCalendar().getTime().before(date);
	}
	/**
	 * Gets the date this player is banned until. If the player has never been banned before, year 0 month 0 day 0 will be returned.
	 * If the player has been banned before, their previous ban date will be returned, even if the ban has expired.
	 * @param player
	 * The player's name
	 * @return
	 * Previous ban date, if any, otherwise year 0 month 0 day 0
	 */
	public static Date getBanDate (String player) {
		return Tools.stringToDate(SimpleFile.loadConfig(PlayerManager.PATH_PLAYERS + "/" + player + ".yml").getString("warn.date", Tools.dateToString(new GregorianCalendar(0, 0, 0).getTime(), "-")), "-");
	}
	public static int getWarnLevel (String player) {
		return SimpleFile.loadConfig(PlayerManager.PATH_PLAYERS + "/" + player + ".yml").getInt("warn.level", 0);
	}
	/**
	 * Sets whether a ban should be given to the user when they log in.
	 * This should be set to true if the user is offline when they receive their warning, otherwise false.
	 * @param shouldBeBanned
	 * Whether or not the user should receive their ban date when they log in.
	 * @param player
	 * The player's name.
	 * @return
	 * Whether or not the info could be saved to disk.
	 */
	public static boolean setUpcomingBan (String player, boolean shouldBeBanned) {
		YamlConfiguration config = SimpleFile.loadConfig(PlayerManager.PATH_PLAYERS + "/" + player + ".yml");
		config.set("warn.upcomingban", shouldBeBanned);
		return SimpleFile.saveConfig(config, PlayerManager.PATH_PLAYERS + "/" + player + ".yml");
	}
	public static boolean getUpcomingBan (String player) {
		return SimpleFile.loadConfig(PlayerManager.PATH_PLAYERS + "/" + player + ".yml").getBoolean("warn.upcomingban", false);
	}

	public Material getKnife() {
		return knife;
	}

	public void setKnife(Material knife) {
		this.knife = knife;
	}
}
