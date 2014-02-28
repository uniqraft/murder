package net.minecraftmurder.signs;

import java.util.logging.Level;

import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.main.Murder;
import net.minecraftmurder.tools.ChatContext;
import net.minecraftmurder.tools.MLogger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public abstract class MSign {	
	protected Location location;	
	protected Murder plugin;
	
	public MSign (Location location, Murder plugin) {
		this.location = location;
		this.plugin = plugin;

		checkIfValid();
	}
	
	public Location getLocation () {
		return location;
	}
	
	public abstract void onInteract (MPlayer mPlayer);
	public abstract void update ();
	
	/**
	 * Should be called when the sign's text has been updated.
	 * This will update the sign 1 tick later.
	 */
	void updateText () {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				getSign().update(true);
			}
		}, 1);
	}
	
	public Sign getSign () {
		Block block = location.getBlock();
		if (block.getState() instanceof Sign) {
			return (Sign) block.getState();
		}
		MLogger.log(Level.WARNING, "Sign at " + location.toString() + " couldn't be found.");
		return null;
	}
	
	boolean checkIfValid () {
		if (!(location.getWorld().getBlockAt(location).getState() instanceof Sign)) {
			MLogger.log(Level.SEVERE, ChatContext.COLOR_WARNING + "No sign at the location: " + location.toString());
			return false;
		}
		return true;
	}
	
	@Override
	public String toString () {
		String s = "";
		if (this instanceof MSignMatch) {
			MSignMatch mSignMatch = (MSignMatch) this;
			s += "match ";
			s += location.getWorld().getName() + " ";
			s += location.getX() + " ";
			s += location.getY() + " ";
			s += location.getZ() + " ";
			s += mSignMatch.getIndex();
		}
		return s;
	}
}