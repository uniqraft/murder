package net.minecraftmurder.signs;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import net.minecraftmurder.inventory.MItem;
import net.minecraftmurder.main.MPlayer;

public class MSignBuy extends MSign {
	private MItem mItem;
	
	public MSignBuy (Location location, MItem mItem) {
		super(location);
		this.mItem = mItem;
		
		update();
	}
	
	public void update () {
		if (checkIfValid()) {
			final Sign sign = getSign();
			sign.setLine(0, ChatColor.GREEN + mItem.getReadableName());
			sign.setLine(1, ChatColor.YELLOW + "" + mItem.getCost() + " coins");
			sign.setLine(2, "");
			sign.setLine(3, ChatColor.GREEN + "Click to buy!");
			sign.update(true);
		}
	}
	
	public Sign getSign () {
		Block block = location.getBlock();
		if (block.getState() instanceof Sign) {
			return (Sign) block.getState();
		}
		return null;
	}
	
	public MItem getMItem () {
		return mItem;
	}

	@Override
	public void onInteract(MPlayer mPlayer) {
		mPlayer.getMInventory().buyMItem(mItem);
	}
}