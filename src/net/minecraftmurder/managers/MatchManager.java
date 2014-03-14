package net.minecraftmurder.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import net.minecraftmurder.matches.LobbyMatch;
import net.minecraftmurder.matches.PlayMatch;
import net.minecraftmurder.tools.ChatContext;
import net.minecraftmurder.tools.MLogger;
import net.minecraftmurder.tools.Tools;

public final class MatchManager {	
	public static final String PATH_MATCH = "plugins/Murder/matches.yml";
	public static final int MAX_MATCHES = 32;
	
	private static HashMap<Integer, PlayMatch> playMatchIndex;
	
	private static LobbyMatch lobbyMatch;
	private static List<PlayMatch> playMatches;
	
	public static void initialize () {
		playMatchIndex = new HashMap<Integer, PlayMatch>();
		playMatches = new ArrayList<PlayMatch>();
	}
	
	public static LobbyMatch getLobbyMatch () {
		if (lobbyMatch == null) {
			MLogger.log(Level.INFO, "No Lobby Match, creating new one.");
			return lobbyMatch = new LobbyMatch(ArenaManager.getLobbyArena());
		}

		return lobbyMatch;
	}
	
	public static void setLobbyMatch (LobbyMatch match) {
		lobbyMatch = match;
	}
	public static List<PlayMatch> getPlayMatches () {
		return playMatches;
	}
	
	public static PlayMatch getPlayMatch (int index) {
		if (playMatchIndex.containsKey(index)) {
			return playMatchIndex.get(index);
		}
		return null;
	}
	public static boolean createPlayMatch (int index) {
		if (index >= MAX_MATCHES) {
			Tools.sendMessageAll(ChatContext.PREFIX_WARNING + "Index " + index + " is higher than the max matches count, " + MAX_MATCHES + ".");
			return false;
		}
		
		if (playMatchIndex.containsKey(index)) {
			return false;
		} else {
			PlayMatch playMatch = new PlayMatch();
			playMatches.add(playMatch);
			playMatchIndex.put(index, playMatch);
			return true;
		}
	}
}