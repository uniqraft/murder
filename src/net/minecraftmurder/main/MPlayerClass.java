package net.minecraftmurder.main;

import java.util.Arrays;
import java.util.logging.Level;

import net.minecraftmurder.inventory.MInventory;
import net.minecraftmurder.inventory.MItem;
import net.minecraftmurder.tools.MLogger;
import net.minecraftmurder.tools.Tools;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public enum MPlayerClass {
	LOBBYMAN, PREGAMEMAN, MURDERER, GUNNER, INNOCENT, SPECTATOR;

	public static final int TICKET_COST = 100;
	public static final Material MATERIAL_GUN = Material.BOW;
	public static final Material MATERIAL_GUNPART = Material.IRON_INGOT;
	public static final Material MATERIAL_INVENTORY = Material.NETHER_STAR;
	public static final Material MATERIAL_SPEEDBOOST = Material.SUGAR;
	public static final Material MATERIAL_TICKET = Material.PAPER;
	public static final Material MATERIAl_LEAVE = Material.SLIME_BALL;
	public static final MItem[] ITEM_KNIVES = { MItem.WOOD_SWORD,
			MItem.STONE_SWORD, MItem.IRON_SWORD, MItem.GOLD_SWORD,
			MItem.DIAMOND_SWORD };
	public static final Material MATERIAL_DETECTOR = Material.COMPASS;
	public static final Material MATERIAL_TELEPORTER = Material.ENDER_PEARL;

	public static void setFoodLevel(MPlayer mPlayer) {
		if (mPlayer.getPlayerClass() == MPlayerClass.MURDERER
				|| mPlayer.getPlayerClass() == MPlayerClass.LOBBYMAN
				|| mPlayer.getPlayerClass() == MPlayerClass.SPECTATOR) {
			mPlayer.getPlayer().setFoodLevel(20);
		} else {
			mPlayer.getPlayer().setFoodLevel(2);
		}
	}

	@SuppressWarnings("deprecation")
	public static void setDefaultClassInventory(MPlayer mPlayer,
			MPlayerClass playerClass) {
		Player player = mPlayer.getPlayer();
		player.closeInventory();	// Force player to close inventory
		player.setFlying(false);
		PlayerInventory inventory = player.getInventory();
		inventory.setHeldItemSlot(0);
		inventory.clear();
		inventory.setBoots(null);
		inventory.setLeggings(null);
		inventory.setChestplate(null);
		inventory.setHelmet(null);

		switch (playerClass) {
		case LOBBYMAN:
			giveInventorySelect(inventory);
			break;
		case PREGAMEMAN:
			giveTicket(inventory);
			giveArmor(mPlayer);
			giveLeaveItem(inventory);
			break;
		case INNOCENT:
			giveArmor(mPlayer);
			break;
		case SPECTATOR:
			giveLeaveItem(inventory);
			giveSpecCompass(inventory);
			break;
		case GUNNER:
			giveGun(inventory);
			giveArmor(mPlayer);
			break;
		case MURDERER:
			giveKnife(mPlayer);
			giveArmor(mPlayer);
			giveCompass(inventory);
			giveTeleporter(inventory);
			giveSpeedboost(inventory);
			giveFakeGunpart(inventory);
			break;
		default:
			// Literally impossible, but ok.
			MLogger.log(Level.SEVERE, "PlayerClass " + playerClass
					+ " doesn't exist!");
			break;
		}

		player.updateInventory();
	}

	public static boolean isKnife(Material material) {
		for (int i = 0; i < ITEM_KNIVES.length; i++) {
			if (ITEM_KNIVES[i].getMaterial().equals(material))
				return true;
		}
		return false;
	}

	public static int getGunPartCount(Inventory inventory) {
		ItemStack item = inventory.getItem(8);
		if (item == null || !item.getType().equals(MATERIAL_GUNPART)) {
			return 0;
		} else {
			return item.getAmount();
		}
	}

	public static void giveGun(Inventory inventory) {
		ItemStack item = new ItemStack(MATERIAL_GUN);
		Tools.setItemStackName(item, "Gun", Arrays.asList(
				"Shoot the murderer with this.",
				"Your XP bar is the reload time.", "Right-Click to fire."));
		inventory.setItem(1, item);
	}

	public static void giveGunPart(Inventory inventory) {
		ItemStack item = inventory.getItem(8);
		if (item == null || !item.getType().equals(MATERIAL_GUNPART)) {
			ItemStack newItem = new ItemStack(MATERIAL_GUNPART);
			Tools.setItemStackName(newItem, "Scrap",
					Arrays.asList("Collect 5 to craft a gun."));
			inventory.setItem(8, newItem);
		} else {
			item.setAmount(item.getAmount() + 1);
		}
	}

	public static void giveKnife(MPlayer mPlayer) {
		Player player = mPlayer.getPlayer();
		Inventory inventory = player.getInventory();
		ItemStack item = new ItemStack(mPlayer.getMInventory()
				.getSelectedKnife().getMaterial());
		Tools.setItemStackName(item, "Knife", Arrays.asList(
				"Kill innocents with this.", "Left-Click to swing.",
				"Right-Click to throw."));
		if (mPlayer.getMInventory().getShinyKnife())
			item.addEnchantment(Murder.emptyEnchantment, 1);
		inventory.setItem(1, item);
	}

	public static void giveCompass(Inventory inventory) {
		ItemStack item = new ItemStack(MATERIAL_DETECTOR);
		Tools.setItemStackName(item, "Detector",
				Arrays.asList("Points towards the closet innocent."));
		inventory.setItem(2, item);
	}

	public static void giveTeleporter(Inventory inventory) {
		ItemStack item = new ItemStack(MATERIAL_TELEPORTER);
		Tools.setItemStackName(item, "Teleporter", Arrays.asList(
				"Teleports all innocent to random locations.",
				"Maximum confusion."));
		inventory.setItem(3, item);
	}

	public static void giveSpeedboost(Inventory inventory) {
		ItemStack item = new ItemStack(MATERIAL_SPEEDBOOST);
		Tools.setItemStackName(item, "Speed Boost",
				Arrays.asList("Gives you a boost!", "Right-Click to use."));
		inventory.setItem(4, item);
	}
	
	public static void giveFakeGunpart(Inventory inventory) {
		ItemStack item = new ItemStack(MATERIAL_GUNPART);
		Tools.setItemStackName(item, "Fake Gun Piece",
				Arrays.asList("Use this to trick the innocent into thinking you're one of them!"));
		inventory.setItem(8, item);
	}

	public static void giveInventorySelect(Inventory inventory) {
		ItemStack item = new ItemStack(MATERIAL_INVENTORY);
		Tools.setItemStackName(item, "Inventory",
				Arrays.asList("Select your gear!", "Right-Click to use."));
		inventory.setItem(8, item);
	}

	public static void giveTicket(Inventory inventory) {
		ItemStack item = new ItemStack(MATERIAL_TICKET);
		Tools.setItemStackName(item, ChatColor.AQUA + "Murderer Ticket", Arrays
				.asList(ChatColor.YELLOW
						+ "Increase your chances of becoming the murderer!",
						ChatColor.YELLOW + "Click to buy for "
								+ ChatColor.GREEN + TICKET_COST
								+ ChatColor.YELLOW + " coins!"));
		inventory.setItem(7, item);
	}

	public static void giveLeaveItem(Inventory inventory) {
		ItemStack item = new ItemStack(MATERIAl_LEAVE);
		Tools.setItemStackName(item, ChatColor.RED + "Leave Match", Arrays
				.asList("Takes you back to the lobby.", "Right-Click to use."));
		inventory.setItem(8, item);
	}

	public static void giveArmor(MPlayer mPlayer) {
		PlayerInventory inventory = mPlayer.getPlayer().getInventory();
		MInventory mInventory = mPlayer.getMInventory();
		MItem boots = mInventory.getSelectedArmor(MItem.ARMOR_BOOTS);
		MItem pants = mInventory.getSelectedArmor(MItem.ARMOR_PANTS);
		MItem chestplate = mInventory.getSelectedArmor(MItem.ARMOR_CHESTPLATE);
		MItem helmet = mInventory.getSelectedArmor(MItem.ARMOR_HELMET);
		if (boots != null)
			inventory.setBoots(new ItemStack(boots.getMaterial()));
		if (pants != null)
			inventory.setLeggings(new ItemStack(pants.getMaterial()));
		if (chestplate != null)
			inventory.setChestplate(new ItemStack(chestplate.getMaterial()));
		if (helmet != null)
			inventory.setHelmet(new ItemStack(helmet.getMaterial()));
	}
	
	public static void giveSpecCompass(Inventory inventory) {
		ItemStack item = new ItemStack(MATERIAL_DETECTOR);
		Tools.setItemStackName(item, "Spectator Menu", Arrays.asList("Right-click to open spectator menu."));
		inventory.setItem(7, item);
	}
	
	public static void giveInstructionalBooklet(Inventory inventory) {
		ItemStack item = new InstructionalBooklet();
		// This might break Birk's implementation on books, idk?
		// Commented out for now
		// Tools.setItemStackName(item, ChatColor.AQUA + "Instructions", Arrays.asList("How to play", "Rules"));
		inventory.setItem(7, item);
	}
}