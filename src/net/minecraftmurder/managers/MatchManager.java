package net.minecraftmurder.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraftmurder.main.Murder;
import net.minecraftmurder.matches.LobbyMatch;
import net.minecraftmurder.matches.PlayMatch;
import net.minecraftmurder.tools.ChatContext;
import net.minecraftmurder.tools.Tools;

public class MatchManager {	
	public static final String PATH_MATCH = "plugins/Murder/matches.yml";
	public static final int MAX_MATCHES = 32;
	
	private Murder plugin;
	private HashMap<Integer, PlayMatch> playMatchIndex;
	
	private LobbyMatch lobbyMatch;
	private List<PlayMatch> playMatches;
	
	public MatchManager (Murder plugin) {
		this.plugin = plugin;
		
		playMatchIndex = new HashMap<Integer, PlayMatch>();
		playMatches = new ArrayList<PlayMatch>();
	}
	
	public LobbyMatch getLobbyMatch () {
		if (lobbyMatch == null)
			return new LobbyMatch(plugin.getArenaManager().getLobbyArena(), plugin);

		return lobbyMatch;
	}
	
	public void setLobbyMatch (LobbyMatch match) {
		lobbyMatch = match;
	}
	public List<PlayMatch> getPlayMatches () {
		return playMatches;
	}
	
	public PlayMatch getPlayMatch (int index) {
		if (playMatchIndex.containsKey(index)) {
			return playMatchIndex.get(index);
		}
		return null;
	}
	public boolean createPlayMatch (int index) {
		if (index >= MAX_MATCHES) {
			Tools.sendMessageAll(ChatContext.PREFIX_WARNING + "Index " + index + " is higher than the max matches count, " + MAX_MATCHES + ".");
			return false;
		}
		
		if (playMatchIndex.containsKey(index)) {
			return false;
		} else {
			PlayMatch playMatch = new PlayMatch(plugin);
			playMatches.add(playMatch);
			playMatchIndex.put(index, playMatch);
			return true;
		}
	}
}