package net.minecraftmurder.main;

import java.util.Arrays;
import java.util.List;

import net.minecraftmurder.tools.ChatContext;
import net.minecraftmurder.tools.Tools;

import org.bukkit.Location;

public class Spawn {
	public static final List<String> TYPES = Arrays.asList("player", "scrap");
	
	private Location location;
	private String type;
	
	public Spawn (Location location, String type) {
		this.location = location;
		if (location == null) 
			throw new NullPointerException("Spawn location is null");
		if (TYPES.contains(type))
			this.type = type;
		else
			Tools.sendMessageAll(ChatContext.COLOR_WARNING + type + " is not a valid type for a spawn.");
	}
	
	public String getType () {
		return type;
	}
	public Location getLocation () {
		return location;
	}
	
	@Override
	public String toString() {
		return type + "*" + Tools.locationToString(location);
	}
	
	public static String spawnToString (Spawn spawn) {
		return spawn.toString();
	}
	public static Spawn stringToSpawn (String spawn) {
		try {
			String[] split = spawn.split("\\*");
			String type = split[0];
			Location location = Tools.stringToLocation (split[1]);
			return new Spawn (location, type);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}