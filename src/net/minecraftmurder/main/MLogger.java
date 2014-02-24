package net.minecraftmurder.main;

import java.util.logging.Level;

import net.minecraftmurder.tools.ChatContext;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class MLogger {
	private static Murder plugin;
	
	public static void setMurderPlugin (Murder plugin) {
		MLogger.plugin = plugin;
	}
	
	/**
	 * Used for logging information. Will write to console and log.
	 * @see log(Level, String)
	 * @param message
	 * The message to log.
	 * @return
	 * False if setMurderPlugin hasn't been called.
	 */
	public static boolean log (String message) {
		return log (Level.INFO, message);
	}
	/**
	 * Used for logging information. Will write to console and log, and if level is 
	 * higher than INFO the message will be sent to all online moderators.
	 * @param level
	 * Level for logging. Refer to JavaDocs. Anything higher than 800 will be sent to online moderators.
	 * @param message
	 * The message to log.
	 * @return
	 * False if setMurderPlugin hasn't been called.
	 */
	public static boolean log (Level level, String message) {
		if (MLogger.plugin == null)
			return false;
		
		Bukkit.getLogger().log(level, message);
		// If important, tell all online mods
		if (level.intValue() > Level.INFO.intValue()) {
			for (Player player: Bukkit.getOnlinePlayers()) {
				if (player.hasPermission("murder.mod"))
					player.sendMessage(level.intValue()>=Level.SEVERE.intValue()?ChatContext.PREFIX_CRITICAL:ChatContext.PREFIX_WARNING + message);
			}
		}
		return true;
	}
}