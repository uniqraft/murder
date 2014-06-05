package net.minecraftmurder.listeners;

import java.util.Date;
import java.util.logging.Level;

import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.main.MPlayerClass;
import net.minecraftmurder.main.Murder;
import net.minecraftmurder.managers.PlayerManager;
import net.minecraftmurder.managers.SignManager;
import net.minecraftmurder.matches.Match;
import net.minecraftmurder.matches.PlayMatch;
import net.minecraftmurder.tools.ChatContext;
import net.minecraftmurder.tools.MLogger;
import net.minecraftmurder.tools.StringTools;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListener implements Listener {
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if (Murder.getInstance().isDevMode())
			return;
		Entity entity = event.getRightClicked();
		if (entity instanceof ItemFrame) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onServerListPing(ServerListPingEvent event) {
		event.setMaxPlayers(Murder.MAX_PLAYERS);
	}

	@EventHandler
	public void onEntityCombust(EntityCombustEvent event) {
		final Entity entity = event.getEntity();
		new BukkitRunnable() {
			@Override
			public void run() {
				if (entity.getFireTicks() != 0) {
					entity.setFireTicks(0);
				} else {
					this.cancel();
				}
			}
		}.runTaskTimer(Murder.getInstance(), 1L, 10L);
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		event.setResult(Result.ALLOWED);
		Player player = event.getPlayer();

		// Kick if player is banned
		if (MPlayer.isBanned(player.getName())) {
			Date date = MPlayer.getBanDate(player.getName());
			if (date != null) {
				String dateString = date.toString();
				event.setResult(Result.KICK_OTHER);
				event.setKickMessage("You are banned until " + dateString);
				MLogger.log(Level.INFO, player.getName()
						+ " tried to login but is banned until: " + dateString);
			} else {
				// If there isn't a date, the file is corrupt
				event.setResult(Result.KICK_OTHER);
				event.setKickMessage("Player file corrupt. Contact staff.");
				MLogger.log(Level.SEVERE, player.getName()
						+ "'s ban date info is corrupt.");
			}
			return;
		}

		// If the server is full
		Player[] onlinePlayers = Bukkit.getOnlinePlayers();
		if (onlinePlayers.length >= Murder.MAX_PLAYERS) {
			if (player.hasPermission("murder.joinfull")) {
				if (onlinePlayers.length > Murder.MAX_PLAYERS
						+ Murder.VIP_SLOTS) {
					event.setResult(Result.KICK_OTHER);
					event.setKickMessage("No empty VIP slots.");
					MLogger.log(Level.INFO, "VIP Player " + player.getName()
							+ " was rejected. No free VIP slots.");
					return;
				}
			} else {
				event.setResult(Result.KICK_OTHER);
				event.setKickMessage("Server is full. Only VIP players can join.");
				MLogger.log(Level.INFO, "Player " + player.getName()
						+ " was rejected. Server full.");
				return;
			}
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.setJoinMessage(null);

		Player player = event.getPlayer();
		if (player.hasPermission("murder.owner"))
			player.setDisplayName(ChatColor.RED + "[Owner] " + ChatColor.WHITE
					+ player.getName());
		else if (player.hasPermission("murder.admin"))
			player.setDisplayName(ChatColor.RED + "[Admin] " + ChatColor.WHITE
					+ player.getName());
		else if (player.hasPermission("murder.mod"))
			player.setDisplayName(ChatColor.BLUE + "[Mod] " + ChatColor.WHITE
					+ player.getName());
		else if (player.hasPermission("murder.ultra"))
			player.setDisplayName(ChatColor.DARK_PURPLE + "[Ultra] "
					+ ChatColor.WHITE + player.getName());
		else if (player.hasPermission("murder.vip"))
			player.setDisplayName(ChatColor.AQUA + "[VIP] " + ChatColor.WHITE
					+ player.getName());

		PlayerManager.onPlayerJoin(player);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		MPlayer mPlayer = PlayerManager.getMPlayer(event.getPlayer());
		PlayMatch pm = null;
		if (mPlayer.getMatch() instanceof PlayMatch)
			pm = (PlayMatch) mPlayer.getMatch();
		if (pm != null)
			pm.setReloading(true);
		PlayerManager.onPlayerQuit(mPlayer);
		if (pm != null)
			pm.setReloading(false);
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (Murder.getInstance().isStarted()) {
			// Only drop iron ingots
			event.setCancelled(!(event.getItemDrop().getItemStack().getType() == MPlayerClass.MATERIAL_GUNPART));
			event.getPlayer().updateInventory();
		}
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		if (!Murder.getInstance().isStarted())
			return;

		String message = "";
		String[] words = event.getMessage().split(" ");
		for (String swear : Murder.swears) {
			for (int i = 0; i <= words.length; i++) {
				if (swear.equalsIgnoreCase(words[i])) {
					// Regex is cool sometimes
					words[i].replaceAll(".", "*");
				}
				message += words[i] + " ";
			}
		}

		event.setCancelled(true);

		Player player = event.getPlayer();
		MPlayer mPlayer = PlayerManager.getMPlayer(player);
		if (mPlayer == null) {
			MLogger.log(Level.WARNING,
					"MPlayer is null in AsyncPlayerChatEvent.");
			return;
		}
		Match match = mPlayer.getMatch();
		if (match == null) {
			MLogger.log(Level.WARNING, "MPlayer's match is null.");
			return;
		}

		if (mPlayer.getPlayerClass() == MPlayerClass.SPECTATOR) {
			String grayMessage = ChatColor.GRAY
					+ ChatColor.stripColor(player.getName() + ": " + message);
			for (MPlayer other : match.getMPlayers()) {
				if (other.getPlayerClass() == MPlayerClass.SPECTATOR) {
					other.getPlayer().sendMessage(grayMessage);
				}
			}
			MLogger.log(Level.INFO, "(Match " + match.hashCode() + ") *DEAD* "
					+ player.getName() + ": " + event.getMessage());
		} else {
			match.sendMessage(player.getDisplayName() + ": " + message);
			MLogger.log(Level.INFO, "(Match " + match.hashCode() + ") "
					+ player.getName() + ": " + event.getMessage());
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		MLogger.log(Level.SEVERE, "A player died! " + event.getDeathMessage());
		((Player) event.getEntity())
				.kickPlayer("If you see this, report it to staff!");
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if (!Murder.getInstance().isStarted())
			return;

		event.setCancelled(true);

		Player player = event.getPlayer();
		MPlayer mPlayer = PlayerManager.getMPlayer(player);
		Match match = mPlayer.getMatch();
		if (match == null)
			return;
		if (match instanceof PlayMatch) {
			PlayMatch playMatch = (PlayMatch) match;
			// If match hasn't started destroy item
			if (!playMatch.isPlaying()) {
				event.getItem().remove();
				return;
			}
		}

		Item item = event.getItem();
		Material material = item.getItemStack().getType();

		if (material.equals(MPlayerClass.MATERIAL_GUN)) {
			if (mPlayer.getPlayerClass() == MPlayerClass.INNOCENT
					&& mPlayer.getGunBanTime() <= 0) {
				player.sendMessage(ChatContext.MESSAGE_PICKEDUPGUN);
				mPlayer.switchPlayerClass(MPlayerClass.GUNNER);
				mPlayer.setReloadTime(MPlayer.RELOAD_TIME);
				// Remove drop and play sound
				event.getItem().remove();
				player.getLocation()
						.getWorld()
						.playSound(player.getLocation(), Sound.ITEM_PICKUP, 1,
								1);
			}
		} else if (material.equals(MPlayerClass.MATERIAL_GUNPART)) {
			if (mPlayer.getPlayerClass() == MPlayerClass.INNOCENT) {
				int count = MPlayerClass.getGunPartCount(player.getInventory()) + 1;
				MPlayer.addCoins(mPlayer.getName(), 4, true);
				if (count == 5) {
					mPlayer.switchPlayerClass(MPlayerClass.GUNNER);
				} else {
					String message = "You picked up scrap. ";
					MPlayerClass.giveGunPart(player.getInventory());
					message += ChatContext.COLOR_HIGHLIGHT + "(" + count + "/"
							+ Murder.CRAFTGUNPARTS_COUNT + ")";
					player.sendMessage(message);
				}
				// Remove drop and play sound
				event.getItem().remove();
				player.getLocation()
						.getWorld()
						.playSound(player.getLocation(), Sound.ITEM_PICKUP, 1,
								1);
			} else if (mPlayer.getPlayerClass() == MPlayerClass.MURDERER) {
				// Remove drop and play sound
				event.getItem().remove();
				player.getLocation()
						.getWorld()
						.playSound(player.getLocation(), Sound.ITEM_PICKUP, 1,
								1);
			}
		} else if (MPlayerClass.isKnife(material)) {
			if (mPlayer.getPlayerClass() == MPlayerClass.MURDERER) {
				if (!item.isInsideVehicle()) {
					player.getInventory().setItem(1, item.getItemStack());
					// Remove drop and play sound
					event.getItem().remove();
					player.getLocation()
							.getWorld()
							.playSound(player.getLocation(), Sound.ITEM_PICKUP,
									1, 1);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!Murder.getInstance().isStarted())
			return;

		Player player = event.getPlayer();
		MPlayer mPlayer = PlayerManager.getMPlayer(player);
		ItemStack itemInHand = player.getItemInHand();

		boolean rightClicked = (event.getAction() == Action.RIGHT_CLICK_AIR || event
				.getAction() == Action.RIGHT_CLICK_BLOCK);

		// Spectators can't interact...
		if (mPlayer.getPlayerClass() == MPlayerClass.SPECTATOR) {
			// ... unless they're spectating another player.
			if (itemInHand.getType() == MPlayerClass.MATERIAL_DETECTOR
					&& rightClicked) {
				mPlayer.getMInventory().openSpectatorMenu();
			}
			event.setCancelled(true);
		}

		if (event.getAction() == Action.PHYSICAL
				&& event.getClickedBlock().getType() == Material.SOIL)
			event.setCancelled(true);

		// If clicked a block
		if (event.getAction() == Action.LEFT_CLICK_BLOCK
				|| event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (event.getClickedBlock().getState() instanceof Sign) {
				Sign sign = (Sign) event.getClickedBlock().getState();
				if (SignManager.existsSigns(sign.getLocation())) {
					SignManager.getMSign(sign.getLocation())
							.onInteract(mPlayer);
				}
			}
		}

		if (rightClicked
				&& mPlayer.getMatch().onPlayerInteractItem(itemInHand, mPlayer))
			event.setCancelled(true);
	}
}