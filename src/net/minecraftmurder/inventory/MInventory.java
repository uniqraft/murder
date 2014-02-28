package net.minecraftmurder.inventory;

import java.util.HashMap;

import net.minecraftmurder.tools.Paths;
import net.minecraftmurder.tools.SimpleFile;

import org.bukkit.configuration.file.YamlConfiguration;

public class MInventory {
	private static final String CONFIG_PREFIX = "inventory.";
	private static final String CONFIG_ITEMS = "inventory.items.";
	
	private String player;
	private HashMap<MItem, Boolean> mItems;
	
	private MItem selectedSword;
	
	public MInventory (String player) {
		this.player = player;
		load();
	}
	
	public boolean load () {
		mItems = new HashMap<MItem, Boolean>();
		// Load
		YamlConfiguration config = SimpleFile.loadConfig(Paths.FOLDER_PLAYERS + player + ".yml", true);
		// Load info about what items the player owns
		for (int i = 0; i < MItem.values().length; i++) {
			MItem mItem = MItem.values()[i];
			mItems.put(mItem, config.getBoolean(CONFIG_ITEMS + mItem.getName(), false));
		}
		// Override
		mItems.put(MItem.WOOD_SWORD, true);		// Everyone owns a wooden sword :)
		
		// Load selected sword
		setSelectedSword(MItem.values()[config.getInt(CONFIG_PREFIX + "selected-sword", MItem.WOOD_SWORD.ordinal())]);
		return false;
	}
	public boolean save () {
		// Load
		String path = Paths.FOLDER_PLAYERS + player + ".yml";
		YamlConfiguration config = SimpleFile.loadConfig(path, true);
		// Save info about what items the player owns
		if (mItems != null && !mItems.isEmpty()) {
			for (MItem mItem: mItems.keySet()) {
				config.set(CONFIG_ITEMS + mItem.getName(), mItems.get(mItem));
			}
		}
		// Save
		return SimpleFile.saveConfig(config, path);
	}
	
	public void setOwnedMItem (MItem mItem, boolean owned) {
		setOwnedMItem(mItem, owned, true);
	}
	public void setOwnedMItem (MItem mItem, boolean owned, boolean save) {
		mItems.put(MItem.WOOD_SWORD, owned);
		if (save) save();
	}
	
	public boolean ownsMItem (MItem mItem) {
		if (!mItems.containsKey(mItem)) return false;
		return (mItems.get(mItem));
	}

	public MItem getSelectedSword() {
		return selectedSword;
	}
	public void setSelectedSword(MItem selectedSword) {
		setSelectedSword(selectedSword, true);
	}
	public void setSelectedSword(MItem selectedSword, boolean save) {
		this.selectedSword = selectedSword;
		if (save) save();
	}
}