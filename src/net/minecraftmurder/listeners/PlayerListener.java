package net.minecraftmurder.listeners;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.main.MPlayerClass;
import net.minecraftmurder.main.Murder;
import net.minecraftmurder.managers.MatchManager;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
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
import org.bukkit.util.Vector;

public class PlayerListener implements Listener {
	@EventHandler
	public void onPlayerInteractEntity (PlayerInteractEntityEvent event) {
		if (Murder.getInstance().isDevMode()) return;
		
		Entity entity = event.getRightClicked();
		if (entity instanceof ItemFrame) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onServerListPing (ServerListPingEvent event) {
		event.setMaxPlayers(Murder.MAX_PLAYERS);
	}
	
	@EventHandler
	public void onPlayerLogin (PlayerLoginEvent event) {
		event.setResult(Result.ALLOWED);
		Player player = event.getPlayer();
		
		// Kick if player is banned
		if (MPlayer.isBanned(player.getName())) {
			Date date = MPlayer.getBanDate(player.getName());
			if (date != null) {
				String dateString = date.toString();
				event.setResult(Result.KICK_OTHER);
				event.setKickMessage("You are banned until " + dateString);
				MLogger.log(Level.INFO, player.getName() + " tried to login but is banned until: " + dateString);
			} else {
				// If there isn't a date, the file is corrupt
				event.setResult(Result.KICK_OTHER);
				event.setKickMessage("Player file corrupt. Contact staff.");
				MLogger.log(Level.SEVERE, player.getName() + "'s ban date info is corrupt.");
			}
			return;
		}
		
		// If the server is full
		Player[] onlinePlayers = Bukkit.getOnlinePlayers();
		if (onlinePlayers.length >= Murder.MAX_PLAYERS) {
			if (player.hasPermission("murder.joinfull")) {
				if (onlinePlayers.length > Murder.MAX_PLAYERS+Murder.VIP_SLOTS) {
					event.setResult(Result.KICK_OTHER);
					event.setKickMessage("No empty VIP slots.");
					MLogger.log(Level.INFO, "VIP Player " + player.getName() + " was rejected. No free VIP slots.");
					return;
				}
			} else {
				event.setResult(Result.KICK_OTHER);
				event.setKickMessage("Server is full. Only VIP players can join.");
				MLogger.log(Level.INFO, "Player " + player.getName() + " was rejected. Server full.");
				return;
			}
		}
	}
	
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
		else if (player.hasPermission("murder.vip"))
			player.setDisplayName(ChatColor.AQUA + "[VIP] " + ChatColor.WHITE + player.getName());
		
		PlayerManager.onPlayerJoin(player);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		event.setQuitMessage(event.getPlayer().getDisplayName() + ChatContext.COLOR_LOWLIGHT + " left the game!");
		PlayerManager.onPlayerQuit(event.getPlayer());
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
		if (!Murder.getInstance().isStarted()) return;
		
		event.setCancelled(true);
		
		Player player = event.getPlayer();
		MPlayer mPlayer = PlayerManager.getMPlayer(player);
		if (mPlayer == null) {MLogger.log(Level.WARNING, "MPlayer is null in AsyncPlayerChatEvent."); return;}
		Match match = mPlayer.getMatch();
		if (match == null) {MLogger.log(Level.WARNING, "MPlayer's match is null."); return;}

		if (mPlayer.getPlayerClass() == MPlayerClass.SPECTATOR) {
			String grayMessage = ChatColor.GRAY + ChatColor.stripColor(player.getName() + ": " + event.getMessage());
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
		
		if(event.getAction() == Action.PHYSICAL && event.getClickedBlock().getType() == Material.SOIL)
	        event.setCancelled(true);
		
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
		} else if (itemInHand.getType() == MPlayerClass.MATERIAL_SPEEDBOOST && rightClicked) {
			event.setCancelled(true);
			event.getPlayer().setItemInHand(null);
			Vector velocity = player.getVelocity().add(player.getLocation().getDirection().multiply(3));
			velocity.setY(Math.max(1d, velocity.getY() / 10));
			player.setVelocity(velocity);
		} else if (itemInHand.getType() == MPlayerClass.MATERIAL_TICKET && rightClicked) {
			event.setCancelled(true);
			player.sendMessage(ChatContext.PREFIX_PLUGIN + ChatContext.COLOR_WARNING + 
					"To buy a ticket, open your inventory and click it!");
		} else if (itemInHand.getType() == MPlayerClass.MATERIA_LEAVE && rightClicked) {
			event.setCancelled(true);
			mPlayer.setMatch(MatchManager.getLobbyMatch());
		}
	}
}