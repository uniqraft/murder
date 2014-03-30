package net.minecraftmurder.matches;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraftmurder.main.Arena;
import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.main.MPlayerClass;
import net.minecraftmurder.main.Murder;
import net.minecraftmurder.managers.MatchManager;
import net.minecraftmurder.managers.PlayerManager;
import net.minecraftmurder.tools.ChatContext;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

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
			pPlayer.setItemInHand(null);
			return true;
		}
		// Gun
		else if (itemStack.getType().equals(MPlayerClass.MATERIAL_GUN)) {
			if (mPlayer.getReloadTime() <= 0) {
				// Fire arrow
				Arrow arrow = pPlayer.launchProjectile(Arrow.class);
				arrow.setVelocity(pPlayer.getEyeLocation().getDirection()
						.multiply(Murder.ARROW_SPEED));
				mPlayer.setReloadTime(MPlayer.RELOAD_TIME);
				pPlayer.getWorld().playSound(pPlayer.getLocation(),
						Sound.FIREWORK_BLAST, 1, .8f);
				return true;
			}
		}
		// Teleporter
		else if (itemStack.getType().equals(MPlayerClass.MATERIAL_TELEPORTER)) {
			// Teleport all gunners and innocents in the player's match to a random spawn
			final List<MPlayer> playersInMatch = mPlayer.getMatch().getMPlayers();
			for (MPlayer mP : playersInMatch) {
				if (mP.getPlayerClass() == MPlayerClass.INNOCENT
						|| mP.getPlayerClass() == MPlayerClass.GUNNER)
					mP.getPlayer().teleport(
							mPlayer.getMatch().getArena()
									.getRandomSpawn("player").getLocation());
			}
			mPlayer.getMatch().sendMessage(
					ChatContext.COLOR_MURDERER + "The Murderer"
					+ ChatContext.COLOR_LOWLIGHT + " used the teleportation device.");
			pPlayer.setItemInHand(null);
			// Play sound at each player's new location one tick later
			Bukkit.getScheduler().scheduleSyncDelayedTask(Murder.getInstance(), new Runnable() {
					@Override
					public void run() {
						for (MPlayer mPlayer : playersInMatch) {
							Player player = mPlayer.getPlayer();
							player.playSound(player.getLocation(),
									Sound.ENDERMAN_TELEPORT, 1, .5f);
						}
					}
				}, 1L);
			return true;
		}
		// Inventory
		else if (itemStack.getType().equals(MPlayerClass.MATERIAL_INVENTORY)) {
			mPlayer.getMInventory().openInventorySelectionScreen();
		}
		// Jump boost
		else if (itemStack.getType().equals(MPlayerClass.MATERIAL_SPEEDBOOST)) {
			Vector velocity = pPlayer.getLocation().getDirection();
			velocity.setY(0.1);
			velocity.normalize().multiply(3);
			pPlayer.setVelocity(velocity);
			pPlayer.setItemInHand(null);
			return true;
		}
		// Ticket
		else if (itemStack.getType().equals(MPlayerClass.MATERIAL_TICKET)) {
			pPlayer.sendMessage(ChatContext.COLOR_WARNING
					+ "To buy a ticket, open your inventory and click it!");
			return true;
		}
		// Leave
		else if (itemStack.getType().equals(MPlayerClass.MATERIAl_LEAVE)) {
			mPlayer.setMatch(MatchManager.getLobbyMatch());
			return true;
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
	public void sendMessage(String message) {
		ArrayList<Player> n = null; // We need this null variable to avoid ambiguous call
		sendMessage(message, n);
	}
	public void sendMessage(String message, Player ignore) {
		sendMessage(message, Arrays.asList(ignore));
	}
	public void sendMessage(String message, List<Player> ignore) {
		for (MPlayer mPlayer : getMPlayers()) {
			Player player = mPlayer.getPlayer();
			if (ignore == null || !ignore.contains(player))
				player.sendMessage(message);
		}
	}
	
	@Override
	public String toString() {
		return "Match (" + this.hashCode() + ")";
	}
}