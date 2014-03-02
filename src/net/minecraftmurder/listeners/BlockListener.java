package net.minecraftmurder.listeners;

import java.util.logging.Level;

import net.minecraftmurder.inventory.MItem;
import net.minecraftmurder.main.Murder;
import net.minecraftmurder.managers.MatchManager;
import net.minecraftmurder.managers.SignManager;
import net.minecraftmurder.signs.MSignBuy;
import net.minecraftmurder.signs.MSignMatch;
import net.minecraftmurder.tools.ChatContext;
import net.minecraftmurder.tools.MLogger;
import net.minecraftmurder.tools.Tools;

import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;

public class BlockListener implements Listener {
	@EventHandler
	public void onHangingBreak (HangingBreakEvent event) {
		if (!Murder.getInstance().isDevMode()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockBreak (BlockBreakEvent event) {
		if (!Murder.getInstance().isDevMode()) {
			event.setCancelled(true);
		}
		Location location = event.getBlock().getLocation();
		if (SignManager.existsSigns(location)) {
			SignManager.removeSign(location);
		}
	}
	
	@EventHandler
	public void onBlockPlace (BlockPlaceEvent event) {
		if (!Murder.getInstance().isDevMode()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onSignChange (SignChangeEvent event) {
		if (!Murder.getInstance().isDevMode()) return;
		
		Sign sign = (Sign) event.getBlock().getState();
		if (event.getLine(0).equalsIgnoreCase("[murder]")) {
			// Is the second line [match]
			if (event.getLine(1).equalsIgnoreCase("[match]")) {
				int index;
				
				try {
					index = Integer.parseInt(event.getLine(2));
				} catch (NumberFormatException e) {
					index = -1;
				}
				
				if (index >= MatchManager.MAX_MATCHES) {
					Tools.sendMessageAll(ChatContext.PREFIX_WARNING + event.getLine(2) + " is higher than max matches.");
				} else if (index >= 0) {
					SignManager.addMSign(new MSignMatch(sign.getLocation(), index));
				} else {
					Tools.sendMessageAll(ChatContext.PREFIX_WARNING + event.getLine(2) + " is not a valid number.");
				}
			} else if (event.getLine(1).equalsIgnoreCase("[shop]")) {
				MItem mItem = MItem.getItem(event.getLine(2));
				if (mItem == null) {
					MLogger.log(Level.WARNING, event.getLine(2) + " is not a valid item.");
					return;
				}
				SignManager.addMSign(new MSignBuy(sign.getLocation(), mItem));
			} else {
				Tools.sendMessageAll(ChatContext.PREFIX_WARNING + event.getLine(1) + " is not a valid sign command.");
			}
		}
	}
}