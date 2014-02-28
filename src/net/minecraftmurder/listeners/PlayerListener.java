package net.minecraftmurder.listeners;

import java.util.List;
import java.util.logging.Level;

import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.main.MPlayerClass;
import net.minecraftmurder.main.Murder;
import net.minecraftmurder.matches.Match;
import net.minecraftmurder.matches.PlayMatch;
import net.minecraftmurder.tools.ChatContext;
import net.minecraftmurder.tools.MLogger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {
	
	private Murder plugin;

	public PlayerListener(Murder plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.setJoinMessage(null);
		plugin.getPlayerManager().onPlayerJoin(event.getPlayer());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		plugin.getPlayerManager().onPlayerQuit(event.getPlayer());
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (plugin.isStarted()) {
			// Only drop iron ingots
			event.setCancelled(!(event.getItemDrop().getItemStack().getType() == MPlayerClass.MATERIAL_GUNPART));
		}
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		if (!plugin.isStarted()) return;
		
		event.setCancelled(true);
		
		Player player = event.getPlayer();
		MPlayer mPlayer = plugin.getMPlayer(player);
		if (mPlayer == null) return;
		Match match = mPlayer.getMatch();
		if (match == null) return;
		
		String message = player.getDisplayName() + ": " + event.getMessage();
		if (mPlayer.getPlayerClass() == MPlayerClass.SPECTATOR) {
			String grayMessage = ChatColor.GRAY + ChatColor.stripColor(event.getMessage());
			for (MPlayer other: match.getMPlayers()) {
				if (other.getPlayerClass() == MPlayerClass.SPECTATOR) {
					other.getPlayer().sendMessage(grayMessage);
				}
			}
			MLogger.log(Level.INFO, "[Match " + match.hashCode() + "] *DEAD* " + message);
		} else {
			match.sendMessage(player.getDisplayName() + ": " + event.getMessage());
			MLogger.log(Level.INFO, "[Match " + match.hashCode() + "] " + message);
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		MLogger.log(Level.SEVERE, "A player died! " + event.getDeathMessage());
		((Player)event.getEntity()).kickPlayer("If you see this, report it to staff!");
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if (!plugin.isStarted())
			return;
		
		event.setCancelled(true);
		
		Player player = event.getPlayer();
		MPlayer mPlayer = plugin.getPlayerManager().getMPlayer(player);
		Match match = mPlayer.getMatch();
		if (match == null) return;
		if (match instanceof PlayMatch) {
			PlayMatch playMatch = (PlayMatch) match;
			// If match hasn't started destroy item
			if (!playMatch.isPlaying()) {
				event.getItem().remove();
				return;
			}
		}
		
		Material material = event.getItem().getItemStack().getType();
		
		if (material.equals(MPlayerClass.MATERIAL_GUN)) {
			if (mPlayer.getPlayerClass() == MPlayerClass.INNOCENT && mPlayer.getGunBanTime() <= 0) {
				player.sendMessage(ChatContext.MESSAGE_PICKEDUPGUN);
				MPlayerClass.giveGun(player.getInventory());
				// Remove drop and play sound
				event.getItem().remove();
				player.getLocation().getWorld().playSound(player.getLocation(), Sound.ITEM_PICKUP, 1, 1);
			}
		} else if (material.equals(MPlayerClass.MATERIAL_GUNPART)) {
			if (mPlayer.getPlayerClass() == MPlayerClass.INNOCENT) {
				String message = ChatContext.PREFIX_PLUGIN + "You picked up scrap. ";
				message += ChatContext.COLOR_HIGHLIGHT + "(" + MPlayerClass.getGunPartCount(player.getInventory()) + "/" + Murder.CRAFTGUNPARTS_COUNT + ")";
				player.sendMessage(message);
				// Remove drop and play sound
				event.getItem().remove();
				player.getLocation().getWorld().playSound(player.getLocation(), Sound.ITEM_PICKUP, 1, 1);
			} else if (mPlayer.getPlayerClass() == MPlayerClass.MURDERER) {
				// Remove drop and play sound
				event.getItem().remove();
				player.getLocation().getWorld().playSound(player.getLocation(), Sound.ITEM_PICKUP, 1, 1);
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!plugin.isStarted())
			return;
		
		Player player = event.getPlayer();
		MPlayer mPlayer = plugin.getMPlayer(player);
		ItemStack itemInHand = player.getItemInHand();
		
		// Spectators can't interact
		if (mPlayer.getPlayerClass() == MPlayerClass.SPECTATOR) {
			event.setCancelled(true);
			return;
		}
		
		boolean rightClicked = (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK);

		if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (event.getClickedBlock().getState() instanceof Sign) {
				Sign sign = (Sign) event.getClickedBlock().getState();
				if (plugin.getSignManager().existsSigns(sign.getLocation())) {
					plugin.getSignManager().getMSign(sign.getLocation()).onInteract(mPlayer);
				}
			}
		}
		if (itemInHand.getType() == MPlayerClass.MATERIAL_GUN && rightClicked) {
			if (mPlayer.getReloadTime() <= 0) {
				// Fire arrow
				Arrow arrow = player.launchProjectile(Arrow.class);
				arrow.setVelocity(player.getEyeLocation().getDirection().multiply(Murder.ARROW_SPEED));
				event.setCancelled(true);
				mPlayer.setReloadTime(MPlayer.RELOAD_TIME);
				player.getWorld().playSound(player.getLocation(), Sound.FIREWORK_BLAST, 1, .8f);
			}
		} else if (itemInHand.getType() == MPlayerClass.MATERIAL_TELEPORTER && rightClicked) {
			event.setCancelled(true);
			event.getPlayer().setItemInHand(null);
			// Teleport all gunners and innocents in the player's match to a random spawn
			final List<MPlayer> playersInMatch = mPlayer.getMatch().getMPlayers();
			for (MPlayer mP: playersInMatch) {
				if (mP.getPlayerClass() == MPlayerClass.INNOCENT || mP.getPlayerClass() == MPlayerClass.GUNNER)
					mP.getPlayer().teleport(mPlayer.getMatch().getArena().getRandomSpawn("player").getLocation());
			}
			mPlayer.getMatch().sendMessage(ChatContext.PREFIX_PLUGIN + ChatContext.COLOR_MURDERER + "The Murderer" + ChatContext.COLOR_LOWLIGHT + " used the teleportation device.");
			// Play sound at each player's new location one tick later
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() {
					for (MPlayer mPlayer: playersInMatch) {
						Player player = mPlayer.getPlayer();
						player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, .5f);
					}
				}
			}, 1);
		}
	}
}