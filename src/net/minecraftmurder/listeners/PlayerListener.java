package net.minecraftmurder.listeners;

import java.util.List;
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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Item;
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
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.setJoinMessage(null);
		
		Player player = event.getPlayer();
		if (player.hasPermission("murder.owner"))
			player.setDisplayName(ChatColor.RED + "[Owner] " + ChatColor.WHITE + player.getName());
		else if (player.hasPermission("murder.admin"))
			player.setDisplayName(ChatColor.RED + "[Admin] " + ChatColor.WHITE + player.getName());
		else if (player.hasPermission("murder.mod"))
			player.setDisplayName(ChatColor.DARK_BLUE + "[Mod] " + ChatColor.WHITE + player.getName());
		
		PlayerManager.onPlayerJoin(player);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		PlayerManager.onPlayerQuit(event.getPlayer());
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (Murder.getInstance().isStarted()) {
			// Only drop iron ingots
			event.setCancelled(!(event.getItemDrop().getItemStack().getType() == MPlayerClass.MATERIAL_GUNPART));
		}
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		if (!Murder.getInstance().isStarted()) return;
		
		event.setCancelled(true);
		
		Player player = event.getPlayer();
		MPlayer mPlayer = PlayerManager.getMPlayer(player);
		if (mPlayer == null) return;
		Match match = mPlayer.getMatch();
		if (match == null) return;

		if (mPlayer.getPlayerClass() == MPlayerClass.SPECTATOR) {
			String grayMessage = ChatColor.GRAY + ChatColor.stripColor(player.getName() + event.getMessage());
			for (MPlayer other: match.getMPlayers()) {
				if (other.getPlayerClass() == MPlayerClass.SPECTATOR) {
					other.getPlayer().sendMessage(grayMessage);
				}
			}
			MLogger.log(Level.INFO, "(Match " + match.hashCode() + ") *DEAD* " + player.getName() + ": " + event.getMessage());
		} else {
			match.sendMessage(player.getDisplayName() + ": " + event.getMessage());
			MLogger.log(Level.INFO, "(Match " + match.hashCode() + ") " + player.getName() + ": " + event.getMessage());
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		MLogger.log(Level.SEVERE, "A player died! " + event.getDeathMessage());
		((Player)event.getEntity()).kickPlayer("If you see this, report it to staff!");
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if (!Murder.getInstance().isStarted())
			return;
		
		event.setCancelled(true);
		
		Player player = event.getPlayer();
		MPlayer mPlayer = PlayerManager.getMPlayer(player);
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
		
		Item item = event.getItem();
		Material material = item.getItemStack().getType();
		
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
		} else if (MPlayerClass.isKnife(material)) {
			if (mPlayer.getPlayerClass() == MPlayerClass.MURDERER) {
				if (!item.isInsideVehicle()) {
					player.getInventory().setItem(1, item.getItemStack());
					// Remove drop and play sound
					event.getItem().remove();
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.ITEM_PICKUP, 1, 1);
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
		
		// Spectators can't interact
		if (mPlayer.getPlayerClass() == MPlayerClass.SPECTATOR) {
			event.setCancelled(true);
			return;
		}
		
		boolean rightClicked = (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK);

		// If clicked a block
		if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (event.getClickedBlock().getState() instanceof Sign) {
				Sign sign = (Sign) event.getClickedBlock().getState();
				if (SignManager.existsSigns(sign.getLocation())) {
					SignManager.getMSign(sign.getLocation()).onInteract(mPlayer);
				}
			}
		}
		
		if (MPlayerClass.isKnife(itemInHand.getType()) && rightClicked) {			
			Arrow arrow = player.launchProjectile(Arrow.class);
			arrow.setVelocity(player.getEyeLocation().getDirection().multiply(1.5));
			event.setCancelled(true);
			player.getWorld().playSound(player.getLocation(), Sound.WITHER_SHOOT, 1, 1.5f);
			
			Item item = player.getWorld().dropItem(player.getLocation(), itemInHand);
			arrow.setPassenger(item);
			
			// Remove gun
			player.setItemInHand(new ItemStack(Material.AIR));
		} else if (itemInHand.getType() == MPlayerClass.MATERIAL_GUN && rightClicked) {
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
			Bukkit.getScheduler().scheduleSyncDelayedTask(Murder.getInstance(), new Runnable() {
				@Override
				public void run() {
					for (MPlayer mPlayer: playersInMatch) {
						Player player = mPlayer.getPlayer();
						player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, .5f);
					}
				}
			}, 1);
		} else if (itemInHand.getType() == MPlayerClass.MATERIAL_INVENTORY && rightClicked) {
			mPlayer.getMInventory().openInventorySelectionScreen();
		}
	}
}