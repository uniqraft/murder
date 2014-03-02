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
	
	public MSign (Location location) {
		this.location = location;

		checkIfValid();
	}
	
	public Location getLocation () {
		return location;
	}
	
	public abstract void onInteract (MPlayer mPlayer);
	public abstract void update ();
	
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
		} else if (this instanceof MSignBuy) {
			MSignBuy mSignBuy = (MSignBuy) this;
			s += "shop ";
			s += location.getWorld().getName() + " ";
			s += location.getX() + " ";
			s += location.getY() + " ";
			s += location.getZ() + " ";
			s += mSignBuy.getMItem().getName();
		}
		return s;
	}
}