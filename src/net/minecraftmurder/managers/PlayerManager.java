package net.minecraftmurder.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.main.Murder;
import net.minecraftmurder.matches.Match;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerManager {
	public static final String PATH_PLAYERS = "plugins/Murder/Players";
	
	private List<MPlayer> mplayers = new ArrayList<MPlayer>();
	private Murder plugin;
	
	public PlayerManager (Murder plugin) {
		this.plugin = plugin;
	}
	
	public void onPlayerJoin (Player player) {
		// Add player
		MPlayer mplayer = new MPlayer (player.getName(), plugin); 
		mplayers.add(mplayer);
		
		// Move player to lobby
		mplayer.setMatch(plugin.getMatchManager().getLobbyMatch());
	}
	public void onPlayerQuit (Player player) {
		// Save and remove
		MPlayer mPlayer = getMPlayer(player);
		if (mPlayer == null) {
			Bukkit.getLogger().log(Level.INFO, "Player " + player.getName() + " doesn't have an entry in PlayerManager, but left the game. Most likely this user is banned, but tried to connect.");
			return;
		}
		mPlayer.save();
		mplayers.remove(mPlayer);
		
		Match match = mPlayer.getMatch();
		if (match == null) {
			Bukkit.getLogger().log(Level.WARNING, "MPlayer " + player.getName() + " wasn't part of a match, but left the game.");
			return;
		}
		match.onPlayerQuit(player); // Tell player's match that this player left
	}
	
	public MPlayer getMPlayer (Player player) {
		return getMPlayer(player.getName());
	}
	public MPlayer getMPlayer (String playerName) {
		for (MPlayer mp: mplayers) {
			if (mp.getName().equals(playerName)) {
				return mp;
			}
		}
		return null;
	}
	public Player getPlayer(MPlayer mplayer) {
		return getPlayer(mplayer.getName());
	}
	public Player getPlayer (String mplayerName) {
		for (MPlayer mp: mplayers) {
			if (mp.getName().equals(mplayerName)) {
				return Bukkit.getPlayer(mplayerName);
			}
		}
		return null;
	}

	public List<MPlayer> getMPlayers() {
		return mplayers;
	}

	
}