package net.minecraftmurder.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.minecraft.server.v1_7_R3.ChatSerializer;
import net.minecraft.server.v1_7_R3.IChatBaseComponent;
import net.minecraft.server.v1_7_R3.PacketPlayOutChat;
import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.matches.Match;
import net.minecraftmurder.tools.ChatContext;
import net.minecraftmurder.tools.MLogger;
import net.minecraftmurder.tools.Paths;
import net.minecraftmurder.tools.SimpleFile;
import net.minecraftmurder.tools.Tools;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public final class PlayerManager {	
	private static List<MPlayer> mPlayers = new ArrayList<MPlayer>();
	
	public static void initialize () {}
	
	public static void onPlayerJoin (Player player) {
		boolean firstJoin = !SimpleFile.exists(Paths.FOLDER_PLAYERS + player.getName() + ".yml");
		MLogger.log(Level.INFO, player.getName() + " joined.");
		
		// Greet player
		if (firstJoin) {
			player.sendMessage(ChatContext.COLOR_LOWLIGHT + "Welcome back to Murder!");
		} else {
			player.sendMessage(ChatContext.COLOR_LOWLIGHT + "Welcome to Murder!");
		}
		Tools.sendMessageAll(ChatColor.GRAY + player.getName() + " joined the server.", player);
		// Send a clickable link to the player
		IChatBaseComponent comp = ChatSerializer
				.a("{\"text\":\"§2[MURDER] \", \"extra\":[{\"text\":\"§bClick to visit our website!\", \"hoverEvent\":{\"action\":\"show_text\", \"value\":\"§cwww.minecraft-murder.net\"}, \"clickEvent\":{\"action\":\"open_url\",\"value\":\"http://www.minecraft-murder.net/\"}}]}");
		PacketPlayOutChat packet = new PacketPlayOutChat(comp, true);
		((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
		
		// Add player
		MPlayer mplayer = new MPlayer (player.getName()); 
		mPlayers.add(mplayer);
		
		// Move player to lobby
		mplayer.setMatch(MatchManager.getLobbyMatch());
	}
	
	public static void onPlayerQuit (MPlayer mPlayer) {
		// Save and remove
		mPlayers.remove(mPlayer);
		Match match = mPlayer.getMatch();
		if (match == null) {
			throw new NullPointerException("MPlayer " + mPlayer.getName() + " wasn't part of a match, but left the game.");
		} else {
			match.onPlayerQuit(mPlayer); // Tell player's match that this player left
		}
	}
	
	public static MPlayer getMPlayer (Player player) {
		return getMPlayer(player.getName());
	}
	public static MPlayer getMPlayer (String playerName) {
		for (MPlayer mp: mPlayers) {
			if (mp.getName().equals(playerName)) {
				return mp;
			}
		}
		return null;
	}
	public static Player getPlayer(MPlayer mplayer) {
		return getPlayer(mplayer.getName());
	}
	public static Player getPlayer (String mplayerName) {
		for (MPlayer mp: mPlayers) {
			if (mp.getName().equals(mplayerName)) {
				return Bukkit.getPlayer(mplayerName);
			}
		}
		return null;
	}

	public static List<MPlayer> getMPlayers() {
		return mPlayers;
	}
}