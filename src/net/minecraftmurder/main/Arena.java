package net.minecraftmurder.main;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.logging.Level;

import net.minecraftmurder.tools.ChatContext;
import net.minecraftmurder.tools.MLogger;
import net.minecraftmurder.tools.SimpleFile;
import net.minecraftmurder.tools.Tools;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.YamlConfiguration;

public class Arena {
	public static final List<String> INFO_TYPES = Arrays.asList("name", "author", "world");

	private String path;

	HashMap<String, String> info;
	private List<Spawn> spawns = new ArrayList<Spawn>();

	private boolean active = false;

	public Arena(String path) {
		this.path = path;
		load();
	}
	
	public void load () {
		// Clear all info stored in arena instance
		info = new HashMap<String, String>();
		spawns.clear();
		
		// Load config
		YamlConfiguration config = SimpleFile.loadConfig(path, true);
		// Load information
		for (String type: INFO_TYPES) {
			info.put(type, config.getString("info." + type, "Default Value"));
		}
		// Load spawns
		for (String string: config.getStringList("spawns")) {
			spawns.add(Spawn.stringToSpawn(string));
		}
		
		// Make sure world exists
		String worldName = info.get("world");
		String worldPath = worldName + "/level.dat";
		if (!new File(worldPath).exists()) {
			MLogger.log(Level.SEVERE, worldPath + " does not exist, but " + path + " implies it does.");
		} else if (Bukkit.getWorld(worldName) == null) {
			// Load the world
			Bukkit.createWorld(new WorldCreator(worldName));
			MLogger.log(Level.INFO, path + " loaded world: " + worldName);
		} else {
			MLogger.log(Level.INFO, path + " detected it's already loaded world: " + worldName);
		}
	}
	public boolean save () {
		YamlConfiguration config = new YamlConfiguration();
		// Save information
		for (String type: INFO_TYPES) {
			config.set("info." + type, info.get(type));
		}
		// Save spawns
		List<String> spawnStrings = new ArrayList<String>();
		if (spawns != null) {
			for (Spawn spawn: spawns) {
				if (spawn != null)
					spawnStrings.add(spawn.toString());
			}
		}
		config.set("spawns", spawnStrings);
		// Save
		return SimpleFile.saveConfig(config, path);
	}
	
	public boolean addSpawn (Spawn spawn, boolean save) {
		// Add spawn to list
		spawns.add(spawn);
		// Save
		if (save)
			return save();
		return true;
	}
	public boolean removeSpawn (Spawn spawn, boolean save) {
		if (spawns.contains(spawn))
			spawns.remove(spawn);
		else
			return false;
		
		// Save
		if (save)
			return save();
		return true;
	}
	
	public boolean setInfo (String type, String info, boolean save) {
		if (INFO_TYPES.contains(type)) {
			this.info.put(type, info);
			if (save)
				return save();
			return true;
		}
		Tools.sendMessageAll(ChatContext.PREFIX_WARNING + type + " is not a valid info type for arena.");
		return false;
	}
	public String getInfo (String type) {
		return getInfo(type, false);
	}
	public String getInfo (String type, boolean forceLoad) {
		if (forceLoad)
			load();
		if (INFO_TYPES.contains(type)) {
			return info.get(type);
		}
		Tools.sendMessageAll(ChatContext.PREFIX_WARNING + type + " is not a valid info type for arena.");
		return null;
	}
	public String getPath () {
		return path;
	}

	public void setActive (boolean active) {
		this.active = active;
	}

	public boolean isActive () {
		return active;
	}

	public Spawn getNearestSpawn (Location location, String type) {
		Spawn nearest = null;
		double nearestDistance = Double.MAX_VALUE;
		for (Spawn spawn: spawns) {
			// Ignore if not same type
			if (!spawn.getType().equalsIgnoreCase(type))
				continue;
			
			double distance = location.distance(spawn.getLocation()); 
			if (distance < nearestDistance) {
				nearest = spawn;
				nearestDistance = distance;
			}
		}
		return nearest;
	}
	public List<Spawn> getSpawns (String type) {
		List<Spawn> spawns = new ArrayList<Spawn>();
		if (this.spawns == null)
			return null;
		for (Spawn spawn: this.spawns) {
			if (spawn.getType().equalsIgnoreCase(type)) {
				spawns.add(spawn);
			}
		}
		return spawns;
	}
	public Spawn getRandomSpawn (String type) {
		List<Spawn> spawns = getSpawns(type);
		if (spawns == null)
			return null;
		return getSpawns(type).get(new SecureRandom().nextInt(spawns.size()));
	}
	
	public double getSpawnDensity (Location location, String type, double radius) {
		int blockCount = Tools.getBlockCountInSphere(location, radius);
		int spawnCount = 0;
		for (Spawn spawn: spawns) {
			// Ignore spawns with a different type
			if (!spawn.getType().equalsIgnoreCase(type))
				continue;
			
			if (spawn.getLocation().distance(location) <= radius)
				spawnCount++;
		}
		if (blockCount == 0)
			return 0;
		return (double) spawnCount / (double) blockCount;
	}
	
	public World getWorld () {
		return Bukkit.getWorld(getInfo("world"));
	}
}