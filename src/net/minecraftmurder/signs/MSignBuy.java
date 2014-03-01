package net.minecraftmurder.signs;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import net.minecraftmurder.main.MPlayer;

public class MSignBuy extends MSign {
	public MSignBuy (Location location) {
		super(location);
		
		update();
	}
	
	public void update () {
		if (checkIfValid()) {
			final Sign sign = getSign();
			sign.setLine(0, ChatColor.GREEN + "Click to buy!");
			sign.setLine(1, "");
			sign.setLine(2, "");
			sign.setLine(3, ChatColor.GREEN + "Green?");
			updateText();
		}
	}
	
	public Sign getSign () {
		Block block = location.getBlock();
		if (block.getState() instanceof Sign) {
			return (Sign) block.getState();
		}
		return null;
	}

	@Override
	public void onInteract(MPlayer mPlayer) {
		
	}
}