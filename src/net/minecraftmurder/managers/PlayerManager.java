package net.minecraftmurder.managers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import net.minecraft.server.v1_7_R1.ChatSerializer;
import net.minecraft.server.v1_7_R1.IChatBaseComponent;
import net.minecraft.server.v1_7_R1.PacketPlayOutChat;
import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.main.Murder;
import net.minecraftmurder.matches.Match;
import net.minecraftmurder.tools.ChatContext;
import net.minecraftmurder.tools.MLogger;
import net.minecraftmurder.tools.Paths;
import net.minecraftmurder.tools.SimpleFile;
import net.minecraftmurder.tools.Tools;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PlayerManager {	
	private List<MPlayer> mplayers = new ArrayList<MPlayer>();
	private Murder plugin;
	
	public PlayerManager (Murder plugin) {
		this.plugin = plugin;
	}
	
	public void onPlayerJoin (Player player) {
		// Kick if player is banned
		if (MPlayer.isBanned(player.getName())) {
			Date date = MPlayer.getBanDate(player.getName());
			if (date != null) {
				String dateString = date.toString();
				player.kickPlayer("You are banned until " + dateString);
				MLogger.log(Level.INFO, player.getName() + " tried to join but is banned until: " + dateString);
				return;
			} else {
				// If there isn't a date, the file is corrupt
				player.kickPlayer("You are banned. Contact staff.");
				MLogger.log(Level.SEVERE, player.getName() + "'s ban date info is corrupt.");
			}
			return;
		}
		
		// Greet player
		if (SimpleFile.exists(Paths.FOLDER_PLAYERS + player.getName() + ".yml")) {
			player.sendMessage(ChatContext.COLOR_LOWLIGHT + "Welcome back to Murder!");
			Tools.sendMessageAll(player.getDisplayName() + ChatContext.COLOR_MAIN + " joined the game!", player);
		} else {
			player.sendMessage(ChatContext.COLOR_LOWLIGHT + "Welcome to Murder!");
			Tools.sendMessageAll(player.getDisplayName() + ChatContext.COLOR_LOWLIGHT + " joined the game for their first time!", player);
		}
			
		
		// Send a clickable link to the player
		IChatBaseComponent comp = ChatSerializer
				.a("{\"text\":\"§2[MURDER] \", \"extra\":[{\"text\":\"§bClick to visit our website!\", \"hoverEvent\":{\"action\":\"show_text\", \"value\":\"§cwww.minecraft-murder.net\"}, \"clickEvent\":{\"action\":\"open_url\",\"value\":\"http://www.minecraft-murder.net/\"}}]}");
		PacketPlayOutChat packet = new PacketPlayOutChat(comp, true);
		((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
		
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