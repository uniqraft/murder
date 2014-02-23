package net.minecraftmurder.matches;

import java.util.ArrayList;
import java.util.List;

import net.minecraftmurder.main.Arena;
import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.main.Murder;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class Match {
	protected Arena arena;
	protected Murder plugin;
	
	public abstract void update ();
	
	public abstract void onPlayerJoin (Player player);
	public abstract void onPlayerQuit (Player player);
	public abstract void onPlayerDeath (Player player);
	
	public Match (Murder plugin) {
		this.plugin = plugin;
	}
	
	public Arena getArena () {
		return arena;
	}
	
	public Location getRandomSpawn () {
		return arena.getRandomSpawn ("player").getLocation();
	}
	
	public List<MPlayer> getMPlayers () {
		List<MPlayer> allMPlayers = plugin.getPlayerManager().getMPlayers();
		List<MPlayer> myMPlayers = new ArrayList<MPlayer>();
		for (MPlayer mPlayer: allMPlayers) {
			if (mPlayer.getMatch() == this)
				myMPlayers.add(mPlayer);
		}
		return myMPlayers;
	}
}