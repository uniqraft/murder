package net.minecraftmurder.listeners;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import net.minecraftmurder.main.MLogger;
import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.main.MPlayerClass;
import net.minecraftmurder.main.Murder;
import net.minecraftmurder.matches.Match;
import net.minecraftmurder.tools.ChatContext;

import org.bukkit.Bukkit;
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
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {
	
	private Murder plugin;

	public PlayerListener(Murder plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerToggleSprint (PlayerToggleSprintEvent event) {
		final Player player = event.getPlayer();
		MPlayer mPlayer = plugin.getMPlayer(player);
		if (mPlayer.getPlayerClass() == MPlayerClass.GUNNER || mPlayer.getPlayerClass() == MPlayerClass.INNOCENT) {
			final int previousFood = player.getFoodLevel();
			// Disable sprint by setting food level to 0
			player.setFoodLevel(0);
			player.sendMessage(ChatContext.PREFIX_PLUGIN + "Only the murderer can sprint.");
			// One tick later, change food level back
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				
				@Override
				public void run() {
					player.setFoodLevel(previousFood);
				}
			}, 1);
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (MPlayer.isBanned(player.getName())) {
			Date date = MPlayer.getBanDate(player.getName());
			if (date != null) {
				String dateString = date.toString();
				player.kickPlayer("You are banned until " + dateString);
				MLogger.log(Level.INFO, player.getName() + " tried to join but is banned until: " + dateString);
			} else {
				player.kickPlayer("You are banned. Contact staff.");
				MLogger.log(Level.SEVERE, player.getName() + "'s ban date info is corrupt.");
			}
			return;
		}
		
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
		plugin.sendMessageToPlayersInMatch(event.getMessage(), match);
		MLogger.log(Level.INFO, "[Match " + match.hashCode() + "] " + player.getName() + ": " + event.getMessage());
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
		MPlayer mplayer = plugin.getPlayerManager().getMPlayer(player);
		Material material = event.getItem().getItemStack().getType();
		if (mplayer.getPlayerClass() == MPlayerClass.MURDERER
				&& material.equals(MPlayerClass.MATERIAL_GUN))
			return;
		if (mplayer.getPlayerClass() == MPlayerClass.GUNNER)
			return;

		// Remove drop and play sound
		event.getItem().remove();
		player.getLocation().getWorld()
				.playSound(player.getLocation(), Sound.ITEM_PICKUP, 1, 1);
		
		if (material.equals(MPlayerClass.MATERIAL_GUN)) {
			player.sendMessage(ChatContext.MESSAGE_PICKEDUPGUN);
			MPlayerClass.giveGun(player.getInventory());
		} else if (material.equals(MPlayerClass.MATERIAL_GUNPART)) {
			String message = ChatContext.PREFIX_PLUGIN + "You picked up scrap. ";
			message += ChatContext.COLOR_HIGHLIGHT + "(" + MPlayerClass.getGunPartCount(player.getInventory()) + "/" + Murder.CRAFTGUNPARTS_COUNT + ")";
			player.sendMessage(message);
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!plugin.isStarted())
			return;
		
		Player player = event.getPlayer();
		MPlayer mPlayer = plugin.getMPlayer(player);
		ItemStack itemInHand = player.getItemInHand();
		
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
			plugin.sendMessageToPlayersInMatch(ChatContext.PREFIX_PLUGIN + ChatContext.COLOR_MURDERER + "The Murderer" + ChatContext.COLOR_LOWLIGHT + " used the teleportation device.", mPlayer.getMatch());
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