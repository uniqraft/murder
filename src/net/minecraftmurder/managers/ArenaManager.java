package net.minecraftmurder.managers;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

import net.minecraftmurder.main.Arena;
import net.minecraftmurder.main.Murder;
import net.minecraftmurder.tools.ChatContext;
import net.minecraftmurder.tools.SimpleFile;
import net.minecraftmurder.tools.Tools;

public class ArenaManager {
	private List<Arena> playArenas;
	private Arena lobby;
	private Murder plugin;
	
	public static final String PATH_ARENAS = "plugins/Murder/Arenas/";
	
	public ArenaManager (Murder plugin) {
		this.plugin = plugin;
		
		playArenas = new ArrayList<Arena>();
		String[] arenaPaths = SimpleFile.getFilesInPath(PATH_ARENAS, ".yml");
		//List<YamlConfiguration> configs = SimpleFile.getYamlConfigsInPath(PATH_ARENAS); 
		if (arenaPaths == null)  {
			Tools.sendMessageAll(ChatContext.PREFIX_WARNING + "No arenas found.");
		} else {
			for (String arenaPath: arenaPaths) {
				if (arenaPath.equalsIgnoreCase(PATH_ARENAS + "lobby.yml")) {
					lobby = new Arena (arenaPath);
				} else {
					playArenas.add(new Arena(arenaPath));
				}
			}
			if (lobby == null)
				Tools.sendMessageAll(ChatContext.PREFIX_DEBUG + "No lobby arena found.");
		}
	}
	
	public boolean createArena (String pathName) {
		YamlConfiguration config = new YamlConfiguration();
		String path = PATH_ARENAS + pathName + ".yml";
		// Is this the lobby?
		if (pathName.equalsIgnoreCase("lobby")) {
			// Is there already a lobby?
			if (lobby != null) {
				// Cancel
				Tools.sendMessageAll(ChatContext.PREFIX_WARNING + "Can't add lobby arena. One already exists.");
				return false;
			} else {
				// Assign lobby arena
				lobby = new Arena(path);
			}
		} else {
			// Add arena
			playArenas.add(new Arena(path));
		}
		return SimpleFile.saveConfig(config, path);
	}
	
	public Arena getArenaByPathname (String arena) {
		String path = PATH_ARENAS + arena + ".yml";
		for (Arena a: getAllArenas()) {
			if (a.getPath().equalsIgnoreCase(path)) {
				return a;
			}
		}
		return null;
	}
	
	public Arena getLobbyArena () {
		if (lobby == null) {
			if (SimpleFile.exists(PATH_ARENAS + "lobby.yml")) {
				return new Arena(PATH_ARENAS + "lobby.yml");
			}
		}
		return lobby;
	}

	public List<Arena> getAllArenas () {
		List<Arena> allArenas = playArenas;
		if (lobby != null)
			allArenas.add(lobby);
		return allArenas;
	}
	public List<Arena> getAvaiableArenas () {
		List<Arena> available = new ArrayList<Arena>();
		for (Arena arena: playArenas) {
			if (!arena.isActive())
				available.add(arena);
		}
		return available;
	}
	public Arena getRandomAvailableArena () {
		List<Arena> available = getAvaiableArenas();
		if (available == null || available.size() == 0)
			return null;
		
		int r = new SecureRandom().nextInt(available.size());
		return available.get(r);
	}
}