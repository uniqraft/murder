package net.minecraftmurder.matches;

import java.util.ArrayList;
import java.util.List;

import net.minecraftmurder.main.Arena;
import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.managers.PlayerManager;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class Match {
	protected Arena arena;
	
	public abstract void update ();
	
	public abstract void onPlayerJoin (Player player);
	public abstract void onPlayerQuit (Player player);
	public abstract void onPlayerDeath (Player player);
	
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
			if (mPlayer.getMatch() == this)
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