package net.minecraftmurder.main;

import java.util.Arrays;

import net.minecraftmurder.tools.ChatContext;
import net.minecraftmurder.tools.Tools;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class MPlayerClass {
	public static final Material MATERIAL_GUN = Material.BOW;
	public static final Material MATERIAL_GUNPART = Material.IRON_INGOT;
	// For future implementation of vanity items, not really necessary to implement right now.
	public static final Material[] MATERIAL_PROJECTILES = 
		{ Material.ARROW };
	// Might want to load this from a file, but it's really just a list of weaponry...
	public static final Material[] MATERIAL_KNIVES = 
		{ 
			Material.WOOD_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.GOLD_SWORD, Material.DIAMOND_SWORD,
			Material.WOOD_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLD_AXE, Material.DIAMOND_AXE
		};
	public static final Material MATERIAL_DETECTOR = Material.COMPASS;
	public static final Material MATERIAL_TELEPORTER = Material.ENDER_PEARL;
	
	public enum PlayerClass {
		LOBBYMAN, PREGAMEMAN, MURDERER, GUNNER, INNOCENT, SPECTATOR
	}
	
	public static void setFoodLevel (MPlayer mplayer) {
		// TODO Clean
		if (mplayer.getPlayerClass() == PlayerClass.MURDERER
				|| mplayer.getPlayerClass() == PlayerClass.LOBBYMAN
				|| mplayer.getPlayerClass() == PlayerClass.SPECTATOR) {
			mplayer.getPlayer().setFoodLevel(20);
		} else {
			mplayer.getPlayer().setFoodLevel(20);
		}
	}
	public static void setDefaultClassInventory (Inventory inventory, PlayerClass playerClass) {
		inventory.clear();
		
		switch (playerClass) {
		case LOBBYMAN:
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
			// Need to figure out a way to fix this.
			giveKnife(inventory);
			giveCompass(inventory);
			giveTeleporter(inventory);
			break;

		default:
			Tools.sendMessageAll(ChatContext.PREFIX_WARNING + "PlayerClass " + playerClass + " doesn't exist!");
			break;
		}
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
	
	public static void giveKnife (Inventory inventory, MPlayer player) {
		ItemStack item = new ItemStack (player.getKnife());
		Tools.setItemStackName(item, "Knife", Arrays.asList("Kill innocents with this.", "Left-Click to murderize."));
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
}