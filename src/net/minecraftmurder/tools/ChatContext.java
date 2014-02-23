package net.minecraftmurder.tools;

import net.minecraftmurder.matches.PlayMatch;

import org.bukkit.ChatColor;


public class ChatContext {
	
	public static final String COLOR_MAIN = ChatColor.WHITE + "";
	public static final String COLOR_LOWLIGHT = ChatColor.YELLOW + "";
	public static final String COLOR_HIGHLIGHT = ChatColor.GREEN + "";
	public static final String COLOR_WARNING = ChatColor.GOLD + "";
	public static final String COLOR_ERROR = ChatColor.RED + "";
	public static final String COLOR_INNOCENT = ChatColor.DARK_AQUA + "";
	public static final String COLOR_MURDERER = ChatColor.RED + "";
	public static final String COLOR_SPECTATOR = ChatColor.GRAY + "";
	
	public static final String PREFIX_PLUGIN = ChatColor.DARK_GREEN + "[Murder] " + COLOR_LOWLIGHT;
	public static final String PREFIX_DEBUG = PREFIX_PLUGIN + ChatColor.GOLD + "[DEBUG] " + COLOR_MAIN;
	public static final String PREFIX_WARNING = PREFIX_PLUGIN + COLOR_WARNING;
	public static final String PREFIX_CRITICAL = PREFIX_PLUGIN + ChatColor.DARK_RED + "[CRITICAL] ";
	
	public static final String ERROR_ARGUMENTS = 
			PREFIX_PLUGIN + COLOR_WARNING + "Invalid arguments.";
	public static final String ERROR_ARENANOTFOUND = 
			PREFIX_PLUGIN + COLOR_WARNING + "Arena not found.";
	public static final String ERROR_NOTPLAYERSENDER = 
			PREFIX_PLUGIN + COLOR_WARNING + "Only a player may execute this command.";
	
	public static final String MESSAGE_SHOTINNOCENT =
			PREFIX_PLUGIN + COLOR_LOWLIGHT + "You shot an innocent and dropped the gun!";
	public static final String MESSAGE_INNOCENTCRAFTGUN =
			PREFIX_PLUGIN + COLOR_INNOCENT + "An innocent" + COLOR_LOWLIGHT + " crafted a gun!";
	public static final String MESSAGE_PICKEDUPGUN =
			PREFIX_PLUGIN + COLOR_LOWLIGHT + "You picked up" + COLOR_HIGHLIGHT + " the gun!";
	public static final String MESSAGE_NOTENOUGHPLAYERS = 
			PREFIX_PLUGIN + COLOR_HIGHLIGHT + "There are not enough players to start the match.";
	public static final String MESSAGE_NOTENOUGHPLAYERSRANKED = 
			PREFIX_PLUGIN + COLOR_HIGHLIGHT + "This match is unranked. You will not gain any coins.";
	public static final String MESSAGE_NOTENOUGHPLAYERSRANKED_2 = 
			PREFIX_PLUGIN + COLOR_LOWLIGHT + "You need at least " + COLOR_HIGHLIGHT + PlayMatch.MIN_PLAYERS_RANKED + " players" + COLOR_LOWLIGHT + " for a ranked match.";	
	
}