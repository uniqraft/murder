package net.minecraftmurder.signs;

import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.main.Murder;
import net.minecraftmurder.tools.ChatContext;
import net.minecraftmurder.tools.Tools;

import org.bukkit.Location;
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
	
	boolean checkIfValid () {
		if (!(location.getWorld().getBlockAt(location).getState() instanceof Sign)) {
			Tools.sendMessageAll(ChatContext.PREFIX_PLUGIN + ChatContext.COLOR_WARNING + "No sign at the location: " + location.toString());
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