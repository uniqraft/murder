package net.minecraftmurder.inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.main.MPlayerClass;
import net.minecraftmurder.main.Murder;
import net.minecraftmurder.tools.ChatContext;
import net.minecraftmurder.tools.Paths;
import net.minecraftmurder.tools.SimpleFile;
import net.minecraftmurder.tools.Tools;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MInventory {
	private static final String CONFIG_PREFIX = "inventory.";
	private static final String CONFIG_ITEMS = CONFIG_PREFIX + "items.";
	private static final String CONFIG_GEAR = CONFIG_PREFIX + "gear.";
	
	private MPlayer mPlayer;
	private HashMap<MItem, Boolean> mItems;
	
	private MItem selectedKnife;
	private boolean shinyKnife;
	
	private MItem[] selectedArmor;	// BOOTS, PANTS, CHESTPLATE, HELMET
	
	public MInventory (MPlayer mPlayer) {
		this.mPlayer = mPlayer;
		load();
	}
	
	public boolean load () {
		mItems = new HashMap<MItem, Boolean>();

		YamlConfiguration config = SimpleFile.loadConfig(Paths.FOLDER_PLAYERS + mPlayer.getName() + ".yml", true);
		// Load info about what items the player owns
		for (int i = 0; i < MItem.values().length; i++) {
			MItem mItem = MItem.values()[i];
			mItems.put(mItem, config.getBoolean(CONFIG_ITEMS + mItem.getName(), false));
		}
		// Override
		mItems.put(MItem.WOOD_SWORD, true);		// Everyone owns a wooden sword :)
		
		selectedKnife = MItem.values()[config.getInt(CONFIG_GEAR + "selected-sword", MItem.WOOD_SWORD.ordinal())];
		shinyKnife = config.getBoolean(CONFIG_GEAR + "shiny-knife", false);
		// Armor
		selectedArmor = new MItem[4];
		selectedArmor[MItem.ARMOR_BOOTS]		= MItem.getItem(config.getString(CONFIG_GEAR + "boots", null));
		selectedArmor[MItem.ARMOR_PANTS] 		= MItem.getItem(config.getString(CONFIG_GEAR + "pants", null));
		selectedArmor[MItem.ARMOR_CHESTPLATE] 	= MItem.getItem(config.getString(CONFIG_GEAR + "chestplate", null));
		selectedArmor[MItem.ARMOR_HELMET]		= MItem.getItem(config.getString(CONFIG_GEAR + "helmet", null));
		return false;
	}
	public boolean save () {
		// Load
		String path = Paths.FOLDER_PLAYERS + mPlayer.getName() + ".yml";
		YamlConfiguration config = SimpleFile.loadConfig(path, true);
		// Save info about what items the player owns
		if (mItems != null && !mItems.isEmpty()) {
			for (MItem mItem: mItems.keySet()) {
				config.set(CONFIG_ITEMS + mItem.getName(), mItems.get(mItem));
			}
		}
		config.set(CONFIG_GEAR + "selected-sword", selectedKnife.ordinal());
		config.set(CONFIG_GEAR + "shiny-knife", shinyKnife);
		
		MItem mBoots 		= selectedArmor[MItem.ARMOR_BOOTS];
		MItem mPants 		= selectedArmor[MItem.ARMOR_PANTS];
		MItem mChestplate 	= selectedArmor[MItem.ARMOR_CHESTPLATE];
		MItem mHelmet 		= selectedArmor[MItem.ARMOR_HELMET];
		
		config.set(CONFIG_GEAR + "boots", (mBoots == null) ? null : mBoots.getName());
		config.set(CONFIG_GEAR + "pants", (mPants == null) ? null : mPants.getName());
		config.set(CONFIG_GEAR + "chestplate", (mChestplate == null) ? null : mChestplate.getName());
		config.set(CONFIG_GEAR + "helmet", (mHelmet == null) ? null : mHelmet.getName());
		
		// Save
		return SimpleFile.saveConfig(config, path);
	}
	
	public void setOwnedMItem (MItem mItem, boolean owned) {
		setOwnedMItem(mItem, owned, true);
	}
	public void setOwnedMItem (MItem mItem, boolean owned, boolean save) {
		mItems.put(mItem, owned);
		if (save) save();
	}
	public void setShinyKnife (boolean shiny) {
		setShinyKnife(shiny, true);
	}
	public void setShinyKnife (boolean shiny, boolean save) {
		shinyKnife = shiny;
		if (save) save();
	}
	public boolean getShinyKnife () {
		return shinyKnife;
	}
	
	public boolean ownsMItem (MItem mItem) {
		if (!mItems.containsKey(mItem)) return false;
		return (mItems.get(mItem));
	}

	public MItem getSelectedKnife () {
		return selectedKnife;
	}
	public void setSelectedKnife(MItem selectedKnife) {
		setSelectedKnife(selectedKnife, true);
	}
	public void setSelectedKnife(MItem selectedKnife, boolean save) {
		this.selectedKnife = selectedKnife;
		if (save) save();
	}
	public void setSelectedArmor (int type, MItem armor) {
		this.selectedArmor[type] = armor;
		save();
	}
	public MItem getSelectedArmor (int type) {
		return selectedArmor[type];
	}
	public void openInventorySelectionScreen () {
		Inventory inventory = Bukkit.createInventory(null, 9*6, "Equipment Selection");
		// Knives
		for (int i = 0; i < MPlayerClass.ITEM_KNIVES.length; i++) {
			MItem mItem = MPlayerClass.ITEM_KNIVES[i];
			ItemStack item = new ItemStack(mItem.getMaterial(), 1);
			
			if (getSelectedKnife() == mItem) {
				Tools.setItemStackName(item, ChatColor.AQUA + mItem.getReadableName(), Arrays.asList(ChatColor.GREEN + "Currently equipped!"));
				item.addEnchantment(Murder.emptyEnchantment, 1);
			} else if (ownsMItem(mItem)) {
				Tools.setItemStackName(item, ChatColor.AQUA + mItem.getReadableName(), Arrays.asList(ChatColor.GREEN + "Click to equip!"));
			} else {
				Tools.setItemStackName(item, ChatColor.AQUA + mItem.getReadableName(), Arrays.asList(ChatColor.YELLOW + "Buy for " + ChatColor.BLUE + mItem.getCost() + ChatColor.YELLOW + " coins."));
			}
			inventory.setItem(2 + i, item);
		}
		// Toggle shiny
		MItem mItemShiny = MItem.SHINY_SWORD_EFFECT;
		ItemStack itemShiny = new ItemStack(mItemShiny.getMaterial(), 1);
		if (shinyKnife)
			itemShiny.addUnsafeEnchantment(Murder.emptyEnchantment, 1);
		if (ownsMItem(mItemShiny))
			Tools.setItemStackName(itemShiny, ChatColor.AQUA + mItemShiny.getReadableName(), Arrays.asList(shinyKnife?(ChatColor.GREEN + "Click to disable!"):(ChatColor.YELLOW + "Click to enable!")));
		else
			Tools.setItemStackName(itemShiny, ChatColor.AQUA + mItemShiny.getReadableName(), Arrays.asList(ChatColor.YELLOW + "Buy for " + ChatColor.BLUE + mItemShiny.getCost() + ChatColor.YELLOW + " coins."));
		inventory.setItem(8, itemShiny);
		// == Armor ==
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 5; y++) {
				MItem mItem = MItem.ARMOR[x][y];
				ItemStack is = new ItemStack(mItem.getMaterial(), 1);
				
				if (mItem.equals(selectedArmor[x])) {
					Tools.setItemStackName(is, ChatColor.AQUA + mItem.getReadableName(), Arrays.asList(ChatColor.GREEN + "Click to unequip!"));
					is.addEnchantment(Murder.emptyEnchantment, 1);
				} else if (ownsMItem(mItem)){
					Tools.setItemStackName(is, ChatColor.AQUA + mItem.getReadableName(), Arrays.asList(ChatColor.GREEN + "Click to equip!"));
				} else {
					Tools.setItemStackName(is, ChatColor.AQUA + mItem.getReadableName(), Arrays.asList(ChatColor.YELLOW + "Buy for " + ChatColor.BLUE + mItem.getCost() + ChatColor.YELLOW + " coins."));
				}
				// 2*9			- Line 2
				// +(-x+3)*9	- Different lines for each armor type.
				// +y			- Different slot for each material type
				// +2			- Offset to the right
				inventory.setItem(2*9+(-x+3)*9+y+2, is);
			}
		}
		
		mPlayer.getPlayer().openInventory(inventory);
	}
	public void openSpectatorMenu () {
		List<MPlayer> mPlayers = new ArrayList<MPlayer>();
		for (MPlayer mP : mPlayer.getMatch().getMPlayers()) {
			if (mP.getPlayerClass() != MPlayerClass.SPECTATOR)
				mPlayers.add(mP);
		}
		Inventory inventory = Bukkit.createInventory(null, 9 * (int)Math.ceil((double)mPlayers.size() / 9d), "Spectator Menu");
		for (int i = 0; i < mPlayers.size(); i++) {
			inventory.setItem(i, Tools.setItemStackName(new ItemStack(Material.SKULL_ITEM, 1, (byte) 3), ChatColor.AQUA + mPlayers.get(i).getName(), Arrays.asList("Click to teleport.")));
		}
		mPlayer.getPlayer().openInventory(inventory);
	}
	
	public boolean buyMItem (MItem mItem) {
		if (mItem == null) return false;
		
		Player player = mPlayer.getPlayer();
		
		if (ownsMItem(mItem)) {
			player.sendMessage(ChatContext.COLOR_WARNING + "You already own this item!");
			return false;
		}
		int coins = MPlayer.getCoins(mPlayer.getName()); 
		if (coins < mItem.getCost()) {
			player.sendMessage(ChatContext.COLOR_WARNING + "You can't afford this item!");
			player.sendMessage(ChatContext.COLOR_LOWLIGHT + "You have " + ChatContext.COLOR_HIGHLIGHT + coins + ChatContext.COLOR_LOWLIGHT + " coins!");
			return false;
		}
		MPlayer.addCoins(mPlayer.getName(), -mItem.getCost(), true);
		setOwnedMItem(mItem, true, true);
		player.sendMessage(ChatContext.COLOR_LOWLIGHT + "You bought the " + ChatContext.COLOR_HIGHLIGHT + mItem.getReadableName() + ChatContext.COLOR_LOWLIGHT + "!");
		return true;
	}
	
	public boolean isEquiped (MItem mItem) {
		return (Arrays.asList(selectedArmor).contains(mItem) || selectedKnife.equals(mItem));
	}
}