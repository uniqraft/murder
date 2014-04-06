package net.minecraftmurder.tools;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class MLogger {
	/**
	 * Used for logging information. Will write to console and log.
	 * If Level is higher than INFO, or dev mode is activate and level equals to INFO,
	 * the message will be sent to all mods.
	 * @param level
	 * Level for logging. Refer to JavaDocs. Anything higher than 800 (or equal to 800, if in dev mode) will be sent to online moderators.
	 * @param message
	 * The message to log.
	 */
	public static void log (Level level, String message) {
		Bukkit.getLogger().log(level, message);
		// Send to all relevant players
		if ((level.intValue() > Level.INFO.intValue())) {
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
	}
}