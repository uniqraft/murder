package net.minecraftmurder.inventory;

import org.bukkit.Material;

public enum MItem {
	// == Swords ==
	WOOD_SWORD
	("wood-knife", "Wooden Knife", 0, Material.WOOD_SWORD),
	STONE_SWORD		
	("stone-knife", "Stone Knife", 1000, Material.STONE_SWORD),
	GOLD_SWORD		
	("gold-knife", "Golden Knife", 2500, Material.GOLD_SWORD),
	IRON_SWORD		
	("iron-knife", "Iron Knife", 5000, Material.IRON_SWORD),
	DIAMOND_SWORD	
	("diamond-knife", "Diamond Knife", 10000, Material.DIAMOND_SWORD),
	SHINY_SWORD_EFFECT
	("shiny-knife", "Shiny Knife Effect", 7500, Material.EYE_OF_ENDER),
	
	// == Armor ==
	// Leather
	ARMOR_LEATHER_BOOTS
	("armor-leather-boots", "Leather Boots", 100, Material.LEATHER_BOOTS),
	ARMOR_LEATHER_PANTS
	("armor-leather-pants", "Leather Leggings", 500, Material.LEATHER_LEGGINGS),
	ARMOR_LEATHER_CHESTPLATE
	("armor-leather-chest", "Leather Chestplate", 800, Material.LEATHER_CHESTPLATE),
	ARMOR_LEATHER_HELMET
	("armor-leather-helmet", "Leather Helmet", 250, Material.LEATHER_HELMET),
	// Gold
	ARMOR_GOLD_BOOTS
	("armor-gold-boots", "Golden Boots", 200, Material.GOLD_BOOTS),
	ARMOR_GOLD_PANTS
	("armor-gold-pants", "Golden Leggings", 1000, Material.GOLD_LEGGINGS),
	ARMOR_GOLD_CHESTPLATE
	("armor-gold-chest", "Golden Chestplate", 1600, Material.GOLD_CHESTPLATE),
	ARMOR_GOLD_HELMET
	("armor-gold-helmet", "Golden Helmet", 500, Material.GOLD_HELMET),
	// Iron
	ARMOR_IRON_BOOTS
	("armor-iron-boots", "Iron Boots", 400, Material.IRON_BOOTS),
	ARMOR_IRON_PANTS
	("armor-iron-pants", "Iron Leggings", 2000, Material.IRON_LEGGINGS),
	ARMOR_IRON_CHESTPLATE
	("armor-iron-chest", "Iron Chestplate", 3200, Material.IRON_CHESTPLATE),
	ARMOR_IRON_HELMET
	("armor-iron-helmet", "Iron Helmet", 1000, Material.IRON_HELMET),
	// Diamond
	ARMOR_DIAMOND_BOOTS
	("armor-diamond-boots", "Diamond Boots", 800, Material.DIAMOND_BOOTS),
	ARMOR_DIAMOND_PANTS
	("armor-diamond-pants", "Diamond Leggings", 4000, Material.DIAMOND_LEGGINGS),
	ARMOR_DIAMOND_CHESTPLATE
	("armor-diamond-chest", "Diamond Chestplate", 6400, Material.DIAMOND_CHESTPLATE),
	ARMOR_DIAMOND_HELMET
	("armor-diamond-helmet", "Diamond Helmet", 2000, Material.DIAMOND_HELMET);
	
	// == Lists ==
	/**
	 * Armor type, Material type
	 */
	public static final MItem[][] ARMOR = {
		{ARMOR_LEATHER_BOOTS, ARMOR_GOLD_BOOTS, ARMOR_IRON_BOOTS, ARMOR_DIAMOND_BOOTS},
		{ARMOR_LEATHER_PANTS, ARMOR_GOLD_PANTS, ARMOR_IRON_PANTS, ARMOR_DIAMOND_PANTS},
		{ARMOR_LEATHER_CHESTPLATE, ARMOR_GOLD_CHESTPLATE, ARMOR_IRON_CHESTPLATE, ARMOR_DIAMOND_CHESTPLATE},
		{ARMOR_LEATHER_HELMET, ARMOR_GOLD_HELMET, ARMOR_IRON_HELMET, ARMOR_DIAMOND_HELMET}
		};
	public static final int ARMOR_BOOTS 		= 0;
	public static final int ARMOR_PANTS 		= 1;
	public static final int ARMOR_CHESTPLATE 	= 2;
	public static final int ARMOR_HELMET 		= 3;
	public static final int ARMOR_LEATHER 		= 0;
	public static final int ARMOR_GOLD	 		= 1;
	public static final int ARMOR_IRON		 	= 2;
	public static final int ARMOR_DIAMOND 		= 3;
	
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