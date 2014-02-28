package net.minecraftmurder.inventory;

import org.bukkit.Material;

public enum MItem {
	WOOD_SWORD
	("wood-knife",		"Wooden Knife",		Material.WOOD_SWORD),
	STONE_SWORD		
	("stone-knife", 	"Stone Knife",		Material.STONE_SWORD),
	GOLD_SWORD		
	("gold-knife",		"Golden Knife",		Material.GOLD_SWORD),
	IRON_SWORD		
	("iron-knife",		"Iron Knife",		Material.IRON_SWORD),
	DIAMOND_SWORD	
	("diamond-knife",	"Diamond Knife",	Material.DIAMOND_SWORD);
	
	private String name;
	private String readable;
	private Material material;
	
	MItem (String name, String readable, Material material) {
		this.name = name;
		this.readable = readable;
		this.material = material;
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
}