package net.minecraftmurder.commands;

import org.bukkit.ChatColor;

public final class MCommandResult {
	public enum Result {
		SUCCESS,
		FAIL_ARGUMENTS,
		FAIL_PEMISSIONS,
		FAIL_CUSTOM;
	}
	final Result result;
	final String message;
	public MCommandResult (Result result, String message) {
		this.result = result;
		this.message = message;
	}
	public String getMessage () {
		switch (result) {
		case SUCCESS:
			return ChatColor.YELLOW + (message != null ? message : "");
		case FAIL_ARGUMENTS:
			return ChatColor.GOLD + "Invalid arguments.";
		case FAIL_PEMISSIONS:
			return ChatColor.RED + "You don't have permission to use this command.";
		case FAIL_CUSTOM:
			if (message == null)
				throw new NullPointerException("Message was set to null.");
			return ChatColor.GOLD + message;
		}
		return null;
	}
}