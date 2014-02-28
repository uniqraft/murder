package net.minecraftmurder.inventory;

import org.bukkit.Material;

public enum MItem {
	WOOD_SWORD		("wood-knife",		Material.WOOD_SWORD),
	STONE_SWORD		("stone-knife", 	Material.STONE_SWORD),
	GOLD_SWORD		("gold-knife",		Material.GOLD_SWORD),
	IRON_SWORD		("iron-knife",		Material.IRON_SWORD),
	DIAMOND_SWORD	("diamond-knife",	Material.DIAMOND_SWORD);
	
	private String name;
	private Material material;
	
	MItem (String name, Material material) {
		this.name = name;
		this.material = material;
	}
	
	public String getName () {
		return name;
	}
	public Material getMaterial () {
		return material;
	}
}