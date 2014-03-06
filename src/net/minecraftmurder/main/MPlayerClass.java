package net.minecraftmurder.main;

import java.util.Arrays;
import java.util.logging.Level;

import net.minecraftmurder.inventory.MItem;
import net.minecraftmurder.tools.MLogger;
import net.minecraftmurder.tools.Tools;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public enum MPlayerClass {
	LOBBYMAN, PREGAMEMAN, MURDERER, GUNNER, INNOCENT, SPECTATOR;
	
	public static final Material MATERIAL_GUN = Material.BOW;
	public static final Material MATERIAL_GUNPART = Material.IRON_INGOT;
	public static final Material MATERIAL_INVENTORY = Material.NETHER_STAR; 
	public static final Material MATERIAL_SPEEDBOOST = Material.SUGAR;
	public static final MItem[] ITEM_KNIFES = {
		MItem.WOOD_SWORD, MItem.STONE_SWORD, MItem.GOLD_SWORD, MItem.IRON_SWORD, MItem.DIAMOND_SWORD};
	public static final Material MATERIAL_DETECTOR = Material.COMPASS;
	public static final Material MATERIAL_TELEPORTER = Material.ENDER_PEARL;
	
	public static void setFoodLevel (MPlayer mPlayer) {
		if (mPlayer.getPlayerClass() == MPlayerClass.MURDERER
				|| mPlayer.getPlayerClass() == MPlayerClass.LOBBYMAN
				|| mPlayer.getPlayerClass() == MPlayerClass.SPECTATOR) {
			mPlayer.getPlayer().setFoodLevel(20);
		} else {
			mPlayer.getPlayer().setFoodLevel(2);
		}
	}
	public static void setDefaultClassInventory (MPlayer mPlayer, MPlayerClass playerClass) {
		Player player = mPlayer.getPlayer();
		Inventory inventory = player.getInventory();
		inventory.clear();
		
		switch (playerClass) {
		case LOBBYMAN:
			giveInventorySelect(inventory);
			break;
		case PREGAMEMAN:
			break;
		case INNOCENT:
			break;
		case SPECTATOR:
			break;
		case GUNNER:
			giveGun(inventory);
			break;
		case MURDERER:
			giveKnife(mPlayer);
			giveCompass(inventory);
			giveTeleporter(inventory);
			giveSpeedbost(inventory);
			break;

		default:
			MLogger.log(Level.SEVERE, "PlayerClass " + playerClass + " doesn't exist!");
			break;
		}
	}
	
	public static boolean isKnife (Material material) {
		for (int i = 0; i < ITEM_KNIFES.length; i++) {
			if (ITEM_KNIFES[i].getMaterial().equals(material))
				return true;
		}
		return false;
	}
	
	public static int getGunPartCount (Inventory inventory) {
		ItemStack item = inventory.getItem(8);
		if (item == null || !item.getType().equals(MATERIAL_GUNPART)) {
			return 0;
		} else {
			return item.getAmount();
		}
	}
	
	public static void giveGun (Inventory inventory) {
		ItemStack item = new ItemStack (MATERIAL_GUN);
		Tools.setItemStackName(item, "Gun", Arrays.asList("Shoot the murderer with this.", "Your XP bar is the reload time.", "Right-Click to fire."));
		inventory.setItem(1, item);
	}
	public static void giveGunPart (Inventory inventory) {
		ItemStack item = inventory.getItem(8);
		if (item == null || !item.getType().equals(MATERIAL_GUNPART)) {
			ItemStack newItem = new ItemStack(MATERIAL_GUNPART);
			Tools.setItemStackName(newItem, "Gun Part", Arrays.asList("Collect 5 to craft a gun."));
			inventory.setItem(8, newItem);
		} else {
			item.setAmount(item.getAmount() + 1);
		}
	}
	public static void giveKnife (MPlayer mPlayer) {
		Player player = mPlayer.getPlayer();
		Inventory inventory = player.getInventory();
		ItemStack item = new ItemStack (mPlayer.getMInventory().getSelectedKnife().getMaterial());
		Tools.setItemStackName(item, "Knife", Arrays.asList("Kill innocents with this.", "Left-Click to swing.", "Right-Click to throw."));
		if (mPlayer.getMInventory().getShinyKnife())
			item.addEnchantment(Murder.emptyEnchantment, 1);
		inventory.setItem(1, item);
	}
	public static void giveCompass (Inventory inventory) {
		ItemStack item = new ItemStack (MATERIAL_DETECTOR);
		Tools.setItemStackName(item, "Detector", Arrays.asList("Points towards the closet innocent."));
		inventory.setItem(2, item);
	}
	public static void giveTeleporter (Inventory inventory) {
		ItemStack item = new ItemStack (MATERIAL_TELEPORTER);
		Tools.setItemStackName(item, "Teleporter", Arrays.asList("Teleports all innocent to random locations.", "Maximum confusion."));
		inventory.setItem(3, item);
	}
	public static void giveSpeedbost (Inventory inventory) {
		ItemStack item = new ItemStack (MATERIAL_SPEEDBOOST);
		Tools.setItemStackName(item, "Speed Boost", Arrays.asList("Gives you a boost!", "Right-Click to use."));
		inventory.setItem(4, item);
	}
	public static void giveInventorySelect (Inventory inventory) {
		ItemStack item = new ItemStack (MATERIAL_INVENTORY);
		Tools.setItemStackName(item, "Inventory", Arrays.asList("Select your gear!", "Right-Click to use."));
		inventory.setItem(8, item);
	}
}