package net.minecraftmurder.listeners;

import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.main.MPlayerClass;
import net.minecraftmurder.main.Murder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class EntityListener implements Listener {
	
	Murder plugin;
	
	public EntityListener (Murder plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onFoodLevelChangeEvent (FoodLevelChangeEvent event) {
		MPlayerClass.setFoodLevel(plugin.getPlayerManager().getMPlayer((Player) event.getEntity()));
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onEntityDamage (EntityDamageEvent event) {
		/*
		 * As far as I know, EntityDamageEvent is called after EntityDamageByEntityEvent.
		 * Therefore death logic is handled here. OnPlayerDeath will be called for the
		 * player's match and the match takes care of respawning. This method just ensures
		 * that the player never actually dies.
		 */
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			// TODO Log
			final MPlayer mPlayer = plugin.getMPlayer(player);
			if (player.getHealth() - event.getDamage() < 1) {
				player.setHealth(20);	
				mPlayer.getMatch().onPlayerDeath(player);
			}
			// Reset killer 1 tick later.
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() {
					mPlayer.setKiller("");
				}
			}, 1);
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity (EntityDamageByEntityEvent event) {
		// If a player was damaged
		if (event.getEntity() instanceof Player) {
			Player damaged = (Player) event.getEntity();
			MPlayer mDamaged = plugin.getPlayerManager().getMPlayer(damaged);
			
			if (mDamaged.getPlayerClass() == MPlayerClass.SPECTATOR) {
				event.setCancelled(true);
				return;
			}
			
			// If damaged was hit by an arrow
			if (event.getDamager() instanceof Arrow) {
				Arrow arrow = (Arrow) event.getDamager();
				
				// Spectator was hit by arrow
				if (mDamaged.getPlayerClass() == MPlayerClass.SPECTATOR) {
					damaged.teleport(damaged.getLocation().add(0, 5, 0));
					Arrow newArrow = damaged.getLocation().getWorld().spawnArrow(arrow.getLocation(), arrow.getVelocity(), (float) arrow.getVelocity().length(), 0);
					newArrow.setShooter(arrow.getShooter());
					newArrow.setBounce(false);
					arrow.remove();
					return;
				}
				
				// Kill player
				mDamaged.setKiller(arrow.getShooter().getCustomName());
				damaged.setHealth(1);
				
				// Was I shot by a player?
				if (arrow.getShooter() instanceof Player) {
					Player shooter = (Player) arrow.getShooter();
					MPlayer mShooter = plugin.getMPlayer(shooter);
					mDamaged.setKiller(mShooter.getName());
					// If I'm not the murderer
					if (mDamaged.getPlayerClass() != MPlayerClass.MURDERER) {
						// Was I shot by a gunner
						if (mShooter.getPlayerClass() == MPlayerClass.GUNNER) {
							// Make him drop the gun
							mShooter.gunBan();
						}
					}
				}
			} else if (event.getDamager() instanceof Player) {
				Player damager = (Player) event.getDamager();
				if (damager.getItemInHand().getType() == MPlayerClass.MATERIAL_KNIFE) {
					// Spectator was hit
					if (mDamaged.getPlayerClass() == MPlayerClass.SPECTATOR) {
						damaged.teleport(damaged.getLocation().add(0, 10, 0));
						return;
					}
					
					damaged.setHealth(1);
				}
			} 
		}
	}
}