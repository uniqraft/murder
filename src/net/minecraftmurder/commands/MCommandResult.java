package net.minecraftmurder.commands;

import org.bukkit.ChatColor;

public final class MCommandResult {
	public enum Result {
		SUCCESS,
		FAIL_ARGUMENTS,
		FAIL_PERMISSIONS,
		FAIL_NOTPLAYER,
		FAIL_CUSTOM;
	}
	private final MCommand mCommand;
	private final Result result;
	private final String message;
	public MCommandResult (MCommand mCommand, Result result) {
		this(mCommand, result, null);
	}
	public MCommandResult (MCommand mCommand, Result result, String message) {
		this.mCommand = mCommand;
		this.result = result;
		this.message = message;
	}
	public Result getResult () {
		return result;
	}
	public MCommand getExecutedMCommand() {
		return mCommand;
	}
	public String getMessage () {
		switch (result) {
		case SUCCESS:
			return ChatColor.YELLOW + (message != null ? message : "");
		case FAIL_ARGUMENTS:
			return ChatColor.GOLD + "Invalid arguments.";
		case FAIL_PERMISSIONS:
			return ChatColor.RED + "You don't have permission to use this command.";
		case FAIL_NOTPLAYER:
			return ChatColor.RED + "Only players can execute this command.";
		case FAIL_CUSTOM:
			if (message == null)
				throw new NullPointerException("Message was set to null.");
			return ChatColor.GOLD + message;
		}
		return null;
	}
}