package net.minecraftmurder.tools;

import java.util.logging.Level;

import net.minecraftmurder.main.Murder;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class MLogger {
	private static Murder plugin;
	
	public static void setMurderPlugin (Murder plugin) {
		MLogger.plugin = plugin;
	}
	
	/**
	 * Used for logging information. Will write to console and log.
	 * If Level is higher than INFO, or dev mode is activate and level equals to INFO,
	 * the message will be sent to all mods.
	 * @param level
	 * Level for logging. Refer to JavaDocs. Anything higher than 800 (or equal to 800, if in dev mode) will be sent to online moderators.
	 * @param message
	 * The message to log.
	 * @return
	 * False if setMurderPlugin hasn't been called.
	 */
	public static boolean log (Level level, String message) {
		if (MLogger.plugin == null)
			return false;
		
		Bukkit.getLogger().log(level, message);
		// Send to all relevant players
		if ((level.intValue() > Level.INFO.intValue()) || (plugin.isDevMode() && level.intValue() >= Level.INFO.intValue())) {
			for (Player player: Bukkit.getOnlinePlayers()) {
				if (player.hasPermission("murder.mod")) {
					String prefix = "";
					if (level.intValue()>=Level.SEVERE.intValue())
						prefix = ChatContext.PREFIX_CRITICAL;
					else if (level.intValue()>=Level.WARNING.intValue())
						prefix = ChatContext.PREFIX_WARNING;
					else
						prefix = ChatContext.PREFIX_DEBUG;
					player.sendMessage(prefix + message);
				}
			}
		}
		return true;
	}
}