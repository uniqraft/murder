package net.minecraftmurder.listeners;

import net.minecraftmurder.main.Murder;
import net.minecraftmurder.managers.MatchManager;
import net.minecraftmurder.signs.MSignMatch;
import net.minecraftmurder.tools.ChatContext;
import net.minecraftmurder.tools.Tools;

import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;

// TODO Make sure operators can't break blocks.

public class BlockListener implements Listener {
	
	private Murder plugin;
	
	public BlockListener (Murder plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onHangingBreak (HangingBreakEvent event) {
		if (plugin.isStarted()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockBreak (BlockBreakEvent event) {
		if (plugin.isStarted() || !event.getPlayer().isOp()) {
			event.setCancelled(true);
		}
		Location location = event.getBlock().getLocation();
		if (plugin.getSignManager().existsSigns(location)) {
			plugin.getSignManager().removeSign(location);
		}
	}
	
	@EventHandler
	public void onBlockPlace (BlockPlaceEvent event) {
		if (plugin.isStarted() || !event.getPlayer().isOp()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onSignChange (SignChangeEvent event) {
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
					plugin.getSignManager().addMSign(new MSignMatch(sign.getLocation(), index, plugin));
				} else {
					Tools.sendMessageAll(ChatContext.PREFIX_WARNING + event.getLine(2) + " is not a valid number.");
				}
			} else {
				Tools.sendMessageAll(ChatContext.PREFIX_WARNING + event.getLine(1) + " is not a valid sign command.");
			}
		}
	}
}