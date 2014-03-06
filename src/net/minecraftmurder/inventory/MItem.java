package net.minecraftmurder.inventory;

import org.bukkit.Material;

public enum MItem {
	WOOD_SWORD
	("wood-knife", "Wooden Knife", 0, Material.WOOD_SWORD),
	STONE_SWORD		
	("stone-knife", "Stone Knife", 150, Material.STONE_SWORD),
	GOLD_SWORD		
	("gold-knife", "Golden Knife", 2500, Material.GOLD_SWORD),
	IRON_SWORD		
	("iron-knife", "Iron Knife", 5000, Material.IRON_SWORD),
	DIAMOND_SWORD	
	("diamond-knife", "Diamond Knife", 10000, Material.DIAMOND_SWORD),
	SHINY_SWORD_EFFECT
	("shiny-knife", "Shiny Knife Effect", 7500, Material.EYE_OF_ENDER),
	TICKET
	("ticket", "Murderer Ticket", 100, Material.PAPER);
	
	private String name;
	private String readable;
	private Material material;
	private int cost;
	
	MItem (String name, String readable, int cost, Material material) {
		this.name = name;
		this.readable = readable;
		this.material = material;
		this.cost = cost;
	}
	
	public String getName () {
		return name;
	}
	public String getReadableName () {
		return readable;
	}
	public Material getMaterial () {
		return material;
	}
	public int getCost () {
		return cost;
	}
	
	public static MItem getItem (String name) {
		for (MItem mItem: MItem.values()) {
			if (mItem.getName().equalsIgnoreCase(name)) {
				return mItem;
			}
		}
		return null;
	}
	public static MItem getItem (Material material) {
		for (MItem mItem: MItem.values()) {
			if (mItem.getMaterial().equals(material)) {
				return mItem;
			}
		}
		return null;
	}
}