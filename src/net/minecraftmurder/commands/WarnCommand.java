package net.minecraftmurder.commands;

import java.util.logging.Level;

import net.minecraftmurder.commands.MCommandResult.Result;
import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.tools.ChatContext;
import net.minecraftmurder.tools.MLogger;

import org.bukkit.command.CommandSender;

public class WarnCommand extends MCommand {
	public WarnCommand(String label) {
		super(label);
	}
	
	@Override
	public MCommandResult exectute(CommandSender sender, String[] args) {
		// Permission
		if (!sender.hasPermission("murder.mod"))
			return new MCommandResult(this, Result.FAIL_PERMISSIONS);
		// Arguments
		if (args.length != 2)
			return new MCommandResult(this, Result.FAIL_ARGUMENTS, null);
		// Execute
		int rule = 0;
		try {
			rule = Integer.parseInt(args[1]);
		} catch (Exception e) {
			sender.sendMessage(ChatContext.PREFIX_WARNING + e.getMessage());
			return new MCommandResult(this, Result.FAIL_CUSTOM,
					args[1] + " cannot be casted to an Interger.");
		}
	
		// TODO Rewrite this and put it in the config
		int level = -1;
		switch (rule) {
		case 1:
			level = 1;
			break;
		case 2:
			level = 2;
			break;
		case 3:
			level = 3;
			break;
		case 4:
			level = 1;
			break;
		default:
			return new MCommandResult(this, Result.FAIL_CUSTOM,
					"Rule #" + rule + " doesn't exist.");
		}
		
		MPlayer.addWarnLevel(args[0], level);
		MLogger.log(Level.INFO, sender.getName() + " warned " + args[0] + " for breaking rule #" + rule + ".");
		return new MCommandResult(this, Result.SUCCESS,
				"Player warned.");
	}
	@Override
	public String getHelp() {
		return "See the guidelines document.";
	}
	@Override
	public String getUsage() {
		return "<player> <rule>";
	}
}
