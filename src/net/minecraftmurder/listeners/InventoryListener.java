package net.minecraftmurder.listeners;

import net.minecraftmurder.main.Murder;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryListener implements Listener {
	
	Murder plugin;
	
	public InventoryListener (Murder plugin) {
		this.plugin = plugin;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInventoryClickEvent (InventoryClickEvent event) {
		HumanEntity human = event.getWhoClicked();
		if (human instanceof Player) {
			Player player = (Player) human;
			
			if (plugin.isStarted()) {
				event.setCancelled(true);
				player.updateInventory();
			}
		}
	}
}