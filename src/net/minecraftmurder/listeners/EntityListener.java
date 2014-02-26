package net.minecraftmurder.listeners;

import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.main.MPlayerClass;
import net.minecraftmurder.main.Murder;
import net.minecraftmurder.tools.ChatContext;

import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

// TODO Updating the API broke death handling. Fix
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
		// TODO Rewrite info, this is not longer accurate
		/*
		 * As far as I know, EntityDamageEvent is called after EntityDamageByEntityEvent.
		 * Therefore death logic is handled here. OnPlayerDeath will be called for the
		 * player's match and the match takes care of respawning. This method just ensures
		 * that the player never actually dies.
		 */
		if (event.getEntity() instanceof Player) {
			event.setCancelled(true);
			Player player = (Player) event.getEntity();
			final MPlayer mPlayer = plugin.getMPlayer(player);
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
				
				// Set the player's killer
				mDamaged.setKiller(((LivingEntity) arrow.getShooter()).getCustomName());
				
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
							shooter.sendMessage(ChatContext.PREFIX_PLUGIN + "You shot an innocent player!");
							shooter.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20 * 60, 1, false), true);
							shooter.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 60, 3, false), true);
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
				}
			} 
			// Damage was dealt, the player dies
			if (event.getDamage() > 0) {
				mDamaged.onDeath();
			}
		}
	}
}