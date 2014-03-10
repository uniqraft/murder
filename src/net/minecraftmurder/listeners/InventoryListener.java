package net.minecraftmurder.listeners;

import net.minecraftmurder.inventory.MItem;
import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.main.MPlayerClass;
import net.minecraftmurder.main.Murder;
import net.minecraftmurder.managers.PlayerManager;
import net.minecraftmurder.matches.PlayMatch;
import net.minecraftmurder.tools.ChatContext;
import net.minecraftmurder.tools.Tools;

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
				if (item == null) {
					player.updateInventory();
					return;
				}
				
				MItem mItem = MItem.getItem(item.getType());
				// If player is in lobby
				if (mPlayer.getPlayerClass() == MPlayerClass.LOBBYMAN) {
					// If clicked knife
					if (MPlayerClass.isKnife(item.getType())) {
						// If the player owns this item
						boolean bought = false;
						if (mPlayer.getMInventory().ownsMItem(mItem) || (bought = mPlayer.getMInventory().buyMItem(mItem))) {
							mPlayer.getMInventory().setSelectedKnife(mItem);
							if (!bought)
								player.sendMessage(
										ChatContext.PREFIX_PLUGIN + ChatContext.COLOR_LOWLIGHT + 
										"You equipped the " + ChatContext.COLOR_HIGHLIGHT +
										mItem.getReadableName() + ChatContext.COLOR_LOWLIGHT + "!");
						}
					} else if (item.getType() == MItem.SHINY_SWORD_EFFECT.getMaterial()) {
						boolean bought = false;
						boolean shiny = mPlayer.getMInventory().getShinyKnife();
						if (mPlayer.getMInventory().ownsMItem(mItem) || (bought = mPlayer.getMInventory().buyMItem(mItem))) {
							mPlayer.getMInventory().setShinyKnife(!shiny);
							if (!bought)
								player.sendMessage(
										ChatContext.PREFIX_PLUGIN + ChatContext.COLOR_LOWLIGHT +
										"Your knife is " + (!shiny?"now shiny!":"no longer shiny!"));
						}
					} else if (Tools.array2DContains(MItem.ARMOR, mItem)) {
						// If the player owns this item
						boolean bought = false;
						if (mPlayer.getMInventory().isEquiped(mItem)) {
							mPlayer.getMInventory().setSelectedArmor(mItem.getArmorType(), null);
							player.sendMessage(
									ChatContext.PREFIX_PLUGIN + ChatContext.COLOR_LOWLIGHT + 
									"You unequipped the " + ChatContext.COLOR_HIGHLIGHT +
									mItem.getReadableName() + ChatContext.COLOR_LOWLIGHT + "!");
						} else if (mPlayer.getMInventory().ownsMItem(mItem) || (bought = mPlayer.getMInventory().buyMItem(mItem))) {
							mPlayer.getMInventory().setSelectedArmor(mItem.getArmorType(), mItem);
							if (!bought)
								player.sendMessage(
										ChatContext.PREFIX_PLUGIN + ChatContext.COLOR_LOWLIGHT + 
										"You equipped the " + ChatContext.COLOR_HIGHLIGHT +
										mItem.getReadableName() + ChatContext.COLOR_LOWLIGHT + "!");
						}
					}
					player.closeInventory();
					mPlayer.getMInventory().openInventorySelectionScreen();
				} else if (mPlayer.getPlayerClass() == MPlayerClass.PREGAMEMAN) {
					if (item.getType().equals(MPlayerClass.MATERIAL_TICKET)) {
						int coins = MPlayer.getCoins(player.getName());
						if (coins >= MPlayerClass.TICKET_COST) {
							MPlayer.addCoins(player.getName(), -MPlayerClass.TICKET_COST, true);
							player.sendMessage(
									ChatContext.PREFIX_PLUGIN + ChatContext.COLOR_LOWLIGHT +
									"Bought ticket! Increased chance of becoming the murderer.");
							((PlayMatch) mPlayer.getMatch()).addTicketUser(mPlayer);
						} else {
							player.sendMessage(ChatContext.PREFIX_PLUGIN + ChatContext.COLOR_WARNING + "You can't afford this item!");
							player.sendMessage(ChatContext.PREFIX_PLUGIN + ChatContext.COLOR_LOWLIGHT + "You have " + ChatContext.COLOR_HIGHLIGHT + coins + ChatContext.COLOR_LOWLIGHT + " coins!");
						}
					}
				}
				player.updateInventory();
			}
		}
	}
}