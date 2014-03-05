package net.minecraftmurder.inventory;

import java.util.Arrays;
import java.util.HashMap;

import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.main.MPlayerClass;
import net.minecraftmurder.tools.ChatContext;
import net.minecraftmurder.tools.Paths;
import net.minecraftmurder.tools.SimpleFile;
import net.minecraftmurder.tools.Tools;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MInventory {
	private static final String CONFIG_PREFIX = "inventory.";
	private static final String CONFIG_ITEMS = CONFIG_PREFIX + "items.";
	
	private MPlayer mPlayer;
	private HashMap<MItem, Boolean> mItems;
	
	private MItem selectedKnife;
	private boolean shinyKnife;
	
	public MInventory (MPlayer mPlayer) {
		this.mPlayer = mPlayer;
		load();
	}
	
	public boolean load () {
		mItems = new HashMap<MItem, Boolean>();
		// Load
		YamlConfiguration config = SimpleFile.loadConfig(Paths.FOLDER_PLAYERS + mPlayer.getName() + ".yml", true);
		// Load info about what items the player owns
		for (int i = 0; i < MItem.values().length; i++) {
			MItem mItem = MItem.values()[i];
			mItems.put(mItem, config.getBoolean(CONFIG_ITEMS + mItem.getName(), false));
		}
		// Override
		mItems.put(MItem.WOOD_SWORD, true);		// Everyone owns a wooden sword :)
		
		// Load selected sword
		setSelectedKnife(MItem.values()[config.getInt(CONFIG_PREFIX + "selected-sword", MItem.WOOD_SWORD.ordinal())]);
		shinyKnife = config.getBoolean(CONFIG_PREFIX + "shiny-knife", false);
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
		config.set(CONFIG_PREFIX + "selected-sword", selectedKnife.ordinal());
		config.set(CONFIG_PREFIX + "shiny-knife", shinyKnife);
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
	public void openInventorySelectionScreen () {
		Inventory inventory = Bukkit.createInventory(null, 9*1, "Equipment Selection");
		// == Knifes ==
		for (int i = 0; i < MPlayerClass.ITEM_KNIFES.length; i++) {
			MItem mItem = MPlayerClass.ITEM_KNIFES[i];
			ItemStack item = new ItemStack(mItem.getMaterial(), 1);
			if (getSelectedKnife() == mItem) {
				Tools.setItemStackName(item, ChatColor.AQUA + mItem.getReadableName(), Arrays.asList(ChatColor.GREEN + "Currently equipped!"));
				item.addEnchantment(Enchantment.DURABILITY, 1);
			} else if (ownsMItem(mItem)) {
				Tools.setItemStackName(item, ChatColor.AQUA + mItem.getReadableName(), Arrays.asList(ChatColor.GREEN + "Click to equip!"));
			} else {
				Tools.setItemStackName(item, ChatColor.AQUA + mItem.getReadableName(), Arrays.asList(ChatColor.YELLOW + "Buy for " + ChatColor.BLUE + mItem.getCost() + ChatColor.YELLOW + " coins."));
			}
			inventory.setItem(2 + i, item);
		}
		// == Toggle Shiny ==
		MItem mItemShiny = MItem.SHINY_SWORD_EFFECT;
		ItemStack itemShiny = new ItemStack(mItemShiny.getMaterial(), 1);
		if (shinyKnife)
			itemShiny.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
		if (ownsMItem(mItemShiny))
			Tools.setItemStackName(itemShiny, ChatColor.AQUA + mItemShiny.getReadableName(), Arrays.asList(shinyKnife?(ChatColor.GREEN + "Click to disable!"):(ChatColor.YELLOW + "Click to enable!")));
		else
			Tools.setItemStackName(itemShiny, ChatColor.AQUA + mItemShiny.getReadableName(), Arrays.asList(ChatColor.YELLOW + "Buy for " + ChatColor.BLUE + mItemShiny.getCost() + ChatColor.YELLOW + " coins."));
		inventory.setItem(8, itemShiny);
		
		mPlayer.getPlayer().openInventory(inventory);
	}
	public boolean buyMItem (MItem mItem) {
		if (mItem == null) return false;
		Player player = mPlayer.getPlayer();
		
		if (ownsMItem(mItem)) {
			player.sendMessage(ChatContext.PREFIX_PLUGIN + ChatContext.COLOR_WARNING + "You already own this item!");
			return false;
		}
		int coins = MPlayer.getCoins(mPlayer.getName()); 
		if (coins < mItem.getCost()) {
			player.sendMessage(ChatContext.PREFIX_PLUGIN + ChatContext.COLOR_WARNING + "You can't afford this item!");
			player.sendMessage(ChatContext.PREFIX_PLUGIN + ChatContext.COLOR_LOWLIGHT + "You have " + ChatContext.COLOR_HIGHLIGHT + coins + ChatContext.COLOR_LOWLIGHT + " coins!");
			return false;
		}
		MPlayer.addCoins(mPlayer.getName(), -mItem.getCost(), true);
		setOwnedMItem(mItem, true, true);
		player.sendMessage(ChatContext.PREFIX_PLUGIN + ChatContext.COLOR_LOWLIGHT + "You bought " + ChatContext.COLOR_HIGHLIGHT + mItem.getReadableName() + ChatContext.COLOR_LOWLIGHT + "!");
		return true;
	}
}