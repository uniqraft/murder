package net.minecraftmurder.commands;

import java.util.logging.Level;

import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.tools.ChatContext;
import net.minecraftmurder.tools.MLogger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class WarnCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label,
			String[] args) {
		if (!sender.hasPermission("murder.mod")) {
			sender.sendMessage(ChatContext.PREFIX_WARNING + "Only mods can use this command.");
			return true;
		}
		
		// warn <player> [rule]
		if (args.length != 2) {
			sender.sendMessage(ChatContext.ERROR_ARGUMENTS);
			sender.sendMessage(ChatContext.PREFIX_PLUGIN + "/warn <player> [rule]");
			return true;
		}
		int rule = 0;
		try {
			rule = Integer.parseInt(args[1]);
		} catch (Exception e) {
			sender.sendMessage(ChatContext.PREFIX_WARNING + e.getMessage());
			return true;
		}
		
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
			sender.sendMessage(ChatContext.PREFIX_WARNING + "Rule #" + rule + " doesn't exist.");
			return true;
		}
		
		sender.sendMessage(ChatContext.PREFIX_PLUGIN + "Player warned.");
		MPlayer.addWarnLevel(args[0], level);
		MLogger.log(Level.INFO, sender.getName() + " warned " + args[0] + " for breaking rule #" + rule + ".");
		return true;
	}

}
