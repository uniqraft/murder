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
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;

public class Arena {
	public static final List<String> INFO_TYPES = Arrays.asList("name", "author", "world");

	private String path;
	private int minY;

	HashMap<String, String> info;
	private List<Spawn> spawns = new ArrayList<Spawn>();

	private boolean reservedForVoting = false;
	private boolean active = false;

	public Arena(String path) {
		this.path = path;
		load();
	}
	
	public void load () {
		MLogger.log(Level.INFO, "Loading: " + path);
		
		// Clear all info stored in arena instance
		info = new HashMap<String, String>();
		spawns.clear();
		
		// Load config
		YamlConfiguration config = SimpleFile.loadConfig(path, true);
		// Load information
		for (String type: INFO_TYPES) {
			info.put(type, config.getString("info." + type, "Default Value"));
		}
		minY = config.getInt("settings.min-y", 0);
		
		// Make sure world exists
		String worldName = info.get("world");
		String worldPath = worldName + "/level.dat";
		if (!new File(worldPath).exists()) {
			MLogger.log(Level.SEVERE, worldPath + " does not exist.");
		} else if (Bukkit.getWorld(worldName) == null) {
			// Load the world
			Bukkit.createWorld(new WorldCreator(worldName));
			MLogger.log(Level.INFO, "Loaded world: " + worldName);
		} else {
			MLogger.log(Level.INFO, "Detected already loaded world: " + worldName);
		}
		
		// Load spawns
		for (String string: config.getStringList("spawns")) {
			spawns.add(Spawn.stringToSpawn(string));
		}
		
		MLogger.log(Level.INFO, "Found " + spawns.size() + " spawns.");
	}
	public boolean save() {
		YamlConfiguration config = new YamlConfiguration();
		// Save information
		for (String type: INFO_TYPES) {
			config.set("info." + type, info.get(type));
		}
		config.set("settings.min-y", minY);
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
	
	public boolean addSpawn(Spawn spawn, boolean save) {
		// Add spawn to list
		spawns.add(spawn);
		// Save
		if (save)
			return save();
		return true;
	}
	public boolean autoAddSpawns(int range) {
		String worldName = getInfo("world");
		World world = Bukkit.getWorld(worldName);
		// Confirm world
		if (world == null) {
			MLogger.log(Level.WARNING, "The world " + worldName + " isn't loaded or doesn't exist.");
			return false;
		}
		MLogger.log(Level.INFO, "Starting auto add process.");
		for (int x = -range; x < range; x++) {
			for (int y = 0; y < world.getMaxHeight() - 1; y++) {
				for (int z = -range; z < range; z++) {
					// Is this block hard clay?
					Block buttomBlock = world.getBlockAt(x, y, z);
					if (buttomBlock.getType().equals(Material.HARD_CLAY)) {
						// Is above block quartz ore?
						Block topBlock = world.getBlockAt(x, y + 1, z);
						if (topBlock.getType().equals(Material.QUARTZ_ORE)) {
							// Found a spawn location!
							// Add spawns, but do not save
							addSpawn(new Spawn(new Location(world, x, y, z), "player"), false);
							addSpawn(new Spawn(new Location(world, x, y, z), "scrap"), false);
							// Remove blocks
							buttomBlock.setType(Material.AIR);
							topBlock.setType(Material.AIR);
						}
					}
				}
			}
		}
		return save();
	}
	public boolean removeSpawn(Spawn spawn, boolean save) {
		if (spawns.contains(spawn))
			spawns.remove(spawn);
		else
			return false;
		
		// Save
		if (save)
			return save();
		return true;
	}
	public int getMinY() {
		return minY;
	}
	public boolean setMinY(int y, boolean save) {
		minY = y;
		if (save)
			return save();
		return false;
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
	public String getInfo(String type) {
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
	public String getPath() {
		return path;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	public boolean isActive() {
		return active;
	}
	
	public void setReservedForVoting(boolean reservedForVoting) {
		this.reservedForVoting = reservedForVoting;
	}
	public boolean isReservedForVoting() {
		return reservedForVoting;
	}
	
	public boolean isAvailable() {
		return (!active && !reservedForVoting);
	}

	public Spawn getNearestSpawn(Location location, String type) {
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
		if (spawns == null) {
			throw new NullPointerException("No spawns could be loaded.");
		}
		return getSpawns(type).get(new SecureRandom().nextInt(spawns.size()));
	}
	
	public double getSpawnDensity (Location location, String type, double radius) {
		double blockCount = (4d/3d)*Math.PI*Math.pow(radius, 3);
		double spawnCount = 0;
		for (Spawn spawn: spawns) {
			// Ignore spawns with a different type
			if (!spawn.getType().equalsIgnoreCase(type))
				continue;
			
			if (spawn.getLocation().distance(location) <= radius)
				spawnCount++;
		}
		if (blockCount == 0)
			return 0;
		return spawnCount / blockCount;
	}
	
	public World getWorld () {
		return Bukkit.getWorld(getInfo("world"));
	}
}