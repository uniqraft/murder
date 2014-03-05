package net.minecraftmurder.listeners;

import net.minecraftmurder.inventory.MItem;
import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.main.MPlayerClass;
import net.minecraftmurder.main.Murder;
import net.minecraftmurder.managers.PlayerManager;
import net.minecraftmurder.tools.ChatContext;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInventoryClickEvent (InventoryClickEvent event) {
		HumanEntity human = event.getWhoClicked();
		if (human instanceof Player) {
			Player player = (Player) human;
			MPlayer mPlayer = PlayerManager.getMPlayer(player);
			
			if (!Murder.getInstance().isDevMode()) {
				event.setCancelled(true);
				ItemStack item = event.getCurrentItem();
				MItem mItem = MItem.getItem(item.getType());
				// If player is in lobby
				if (mPlayer.getPlayerClass() == MPlayerClass.LOBBYMAN) {
					// If clicked knife
					if (MPlayerClass.isKnife(item.getType())) {
						// If the player owns this item
						if (mPlayer.getMInventory().ownsMItem(mItem) || mPlayer.getMInventory().buyMItem(mItem)) {
							mPlayer.getMInventory().setSelectedKnife(mItem);
							player.sendMessage(
									ChatContext.PREFIX_PLUGIN + ChatContext.COLOR_LOWLIGHT + 
									"You equiped " + ChatContext.COLOR_HIGHLIGHT +
									mItem.getReadableName() + ChatContext.COLOR_LOWLIGHT + ".");
						}
						player.closeInventory();		
					}
					mPlayer.getMInventory().openInventorySelectionScreen();
				}
				player.updateInventory();
			}
		}
	}
}