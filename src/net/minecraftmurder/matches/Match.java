package net.minecraftmurder.matches;

import java.util.ArrayList;
import java.util.List;

import net.minecraftmurder.main.Arena;
import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.main.MPlayerClass;
import net.minecraftmurder.managers.PlayerManager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class Match {
	protected Arena arena;
	
	public abstract void update ();
	
	public abstract void onPlayerJoin (MPlayer player);
	public abstract void onPlayerQuit (MPlayer player);
	public abstract void onPlayerDeath (MPlayer player);
	
	/**
	 * Handle default player interact with item behavior for matches. May be
	 * overriden to change default behavior.
	 * @param itemStack
	 * The itemstack the player interacted with.
	 * @param mPlayer
	 * The player that interacted with the itemstack.
	 * @return
	 * Whether or not the interact was handled.
	 */
	public boolean onPlayerInteractItem(ItemStack itemStack, MPlayer mPlayer) {
		Player pPlayer = mPlayer.getPlayer();
		// Knife
		if (MPlayerClass.isKnife(itemStack.getType())) {
			// Spawn arrow
			Arrow arrow = pPlayer.launchProjectile(Arrow.class);
			arrow.setVelocity(pPlayer.getEyeLocation().getDirection().multiply(1.2));
			// Make knife ride arrow
			pPlayer.getWorld().playSound(pPlayer.getLocation(), Sound.WITHER_SHOOT, 1, 1.5f);
			arrow.setPassenger(pPlayer.getWorld().dropItem(pPlayer.getLocation(), itemStack));
			// Remove item from inventory
			pPlayer.setItemInHand(new ItemStack(Material.AIR));
			return true;
		}
		// Gun
		if (itemStack.getType().equals(MPlayerClass.MATERIAL_GUN)) {
			
		}
		return false;
	}
	
	public Arena getArena () {
		if (arena == null) {
			throw new NullPointerException("Arena null.");
		}
		return arena;
	}
	
	public Location getRandomSpawn () {
		return getArena().getRandomSpawn ("player").getLocation();
	}
	
	public List<MPlayer> getMPlayers () {
		List<MPlayer> allMPlayers = PlayerManager.getMPlayers();
		List<MPlayer> myMPlayers = new ArrayList<MPlayer>();
		for (MPlayer mPlayer: allMPlayers) {
			if (mPlayer.getMatch().equals(this))
				myMPlayers.add(mPlayer);
		}
		return myMPlayers;
	}
	
	/**
	 * Sends a message to all players in this match.
	 * @param message
	 * The message to be sent.
	 */
	public void sendMessage (String message) {
		for (MPlayer mPlayer : getMPlayers())
			mPlayer.getPlayer().sendMessage(message);
	}
}