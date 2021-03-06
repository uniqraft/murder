package net.minecraftmurder.listeners;

import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.main.MPlayerClass;
import net.minecraftmurder.main.Murder;
import net.minecraftmurder.managers.PlayerManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EntityListener implements Listener {
	@EventHandler
	public void onCreatureSpawn (CreatureSpawnEvent event) {
		if (event.getSpawnReason() != SpawnReason.CUSTOM)
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onFoodLevelChangeEvent (FoodLevelChangeEvent event) {
		MPlayerClass.setFoodLevel(PlayerManager.getMPlayer((Player) event.getEntity()));
		event.setCancelled(true);
	}
	
	@EventHandler
    public void itemFrameItemRemoval(EntityDamageEvent event) {
		if (Murder.getInstance().isDevMode()) return;
		
        if (event.getEntity() instanceof ItemFrame) {
        	event.setCancelled(true);
        }
    }
	
	@EventHandler
	public void onProjectileHit (ProjectileHitEvent event) {
		if (event.getEntityType() != EntityType.ARROW) return;
		Arrow arrow = (Arrow) event.getEntity();
		if (arrow.getPassenger() != null && arrow.getPassenger().getType() == EntityType.DROPPED_ITEM)
			arrow.remove();
	}
	
	@EventHandler
	public void onEntityDamage (EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			event.setCancelled(true);
			Player player = (Player) event.getEntity();
			final MPlayer mPlayer = PlayerManager.getMPlayer(player);
			// Reset killer 1 tick later.
			Bukkit.getScheduler().scheduleSyncDelayedTask(Murder.getInstance(), new Runnable() {
				@Override
				public void run() {
					mPlayer.setKiller("");
				}
			}, 1);
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity (EntityDamageByEntityEvent event) {
		// Return if not player
		if (!(event.getEntity() instanceof Player)) return;
		
		Player damaged = (Player) event.getEntity();
		MPlayer mDamaged = PlayerManager.getMPlayer(damaged);
		
		if (mDamaged.getPlayerClass() == MPlayerClass.SPECTATOR) {
			event.setCancelled(true);
			return;
		}
		
		Entity entityDamager = (Entity) event.getDamager();
		
		// If damaged was hit by an arrow
		if (entityDamager.getType() == EntityType.ARROW) {
			Arrow arrow = (Arrow) entityDamager;
			
			// Spectator was hit by arrow
			if (mDamaged.getPlayerClass() == MPlayerClass.SPECTATOR) {
				damaged.teleport(damaged.getLocation().add(0, 5, 0));
				Arrow newArrow = damaged.getLocation().getWorld().spawnArrow(arrow.getLocation(), arrow.getVelocity(), (float) arrow.getVelocity().length(), 0);
				newArrow.setShooter(arrow.getShooter());
				newArrow.setBounce(false);
				arrow.remove();
				return;
			// Murderer was hit by arrow
			} else if (mDamaged.getPlayerClass() == MPlayerClass.MURDERER) {
				// Is this the knife?
				if (arrow.getPassenger() != null) {
					event.setCancelled(true);
					return;
				}
			}
			
			// If damaged was shot by a player
			if (arrow.getShooter() instanceof Player) {				
				Player shooter = (Player) arrow.getShooter();
				MPlayer mShooter = PlayerManager.getMPlayer(shooter);
				// If damaged is not the murderer and was shot by a gunner
				if (mDamaged.getPlayerClass() != MPlayerClass.MURDERER && mShooter.getPlayerClass() == MPlayerClass.GUNNER) {
						// Make him drop the gun
						mShooter.gunBan();
						shooter.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20 * 60, 1, false), true);
						shooter.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 60, 3, false), true);
				}
				// Mark player as dead
				mDamaged.setKiller(mShooter.getName());
				mDamaged.onDeath();
			}
		} else if (entityDamager.getType() == EntityType.PLAYER) {
			// Damaged by another player
			Player damager = (Player) event.getDamager();
			
			// Was the player hit by a knife
			if (MPlayerClass.isKnife(damager.getItemInHand().getType())) {
				// If spectator was hit
				if (mDamaged.getPlayerClass() == MPlayerClass.SPECTATOR) {
					damaged.teleport(damaged.getLocation().add(0, 10, 0));
					return;
				}
				// Mark player as dead
				mDamaged.setKiller(damager);
				mDamaged.onDeath();
			}
		}
	}
}