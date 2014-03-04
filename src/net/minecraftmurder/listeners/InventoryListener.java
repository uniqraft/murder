package net.minecraftmurder.listeners;

import net.minecraftmurder.inventory.MItem;
import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.main.MPlayerClass;
import net.minecraftmurder.main.Murder;
import net.minecraftmurder.managers.PlayerManager;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

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
			MPlayer mPlayer = PlayerManager.getMPlayer(player);
			
			if (!plugin.isDevMode()) {
				event.setCancelled(true);
				if (mPlayer.getPlayerClass() == MPlayerClass.LOBBYMAN) {
					ItemStack item = event.getCurrentItem();
					if (MPlayerClass.isKnife(item.getType())) {
						mPlayer.getMInventory().setSelectedKnife(MItem.getItem(item.getType()));
					}
					mPlayer.getMInventory().openInventorySelectionScreen();
				} else {
					player.updateInventory();
				}
			}
		}
	}
}