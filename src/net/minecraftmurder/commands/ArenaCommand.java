package net.minecraftmurder.commands;

import net.minecraftmurder.main.Arena;
import net.minecraftmurder.main.Murder;
import net.minecraftmurder.tools.ChatContext;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ArenaCommand implements CommandExecutor {
	private Murder plugin;
	
	public ArenaCommand (Murder plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label,
			String[] args) {
		// arena <action> [name] [info-type] [info]
		
		if (args.length < 1) {
			sender.sendMessage(ChatContext.ERROR_ARGUMENTS);
			return false;
		}
		
		if (args[0].equalsIgnoreCase("add")) {
			if (args.length != 2) {
				sender.sendMessage(ChatContext.ERROR_ARGUMENTS);
				return false;
			}
			if (plugin.getArenaManager().createArena(args[1])) {
				sender.sendMessage(ChatContext.PREFIX_PLUGIN + "Arena created.");
			} else {
				sender.sendMessage(ChatContext.PREFIX_WARNING + "Couldn't create arena!");
			}
			return true;
		} else if (args[0].equalsIgnoreCase("set")) {
			if (args.length != 4) {
				sender.sendMessage(ChatContext.ERROR_ARGUMENTS);
				return false;
			}
			Arena arena = plugin.getArenaManager().getArenaByPathname(args[1]);
			if (arena != null) {
				if (Arena.INFO_TYPES.contains(args[2].toLowerCase())) {
					if (arena.setInfo(args[2], args[3], true)) {
						sender.sendMessage(ChatContext.PREFIX_PLUGIN + args[1] + "'s " + args[2] + " was set to " + args[3] + ".");
					} else {
						sender.sendMessage(ChatContext.PREFIX_CRITICAL + "Couldn't save!");
					}
				} else {
					sender.sendMessage(ChatContext.PREFIX_WARNING + args[2] + " is not a valid info type.");
				}
			} else {
				sender.sendMessage(ChatContext.PREFIX_WARNING + "Arena " + args[1] + " couldn't be found!");
			}
			return true;
		} else if (args[0].equalsIgnoreCase("get")) {
			if (args.length != 3) {
				sender.sendMessage(ChatContext.ERROR_ARGUMENTS);
				return false;
			}
			Arena arena = plugin.getArenaManager().getArenaByPathname(args[1]);
			if (arena != null) {
				if (Arena.INFO_TYPES.contains(args[2].toLowerCase())) {
					sender.sendMessage(ChatContext.PREFIX_PLUGIN + arena.getInfo(args[2], true));
				} else {
					sender.sendMessage(ChatContext.PREFIX_WARNING + args[2] + " is not a valid info type.");
				}
			} else {
				sender.sendMessage(ChatContext.PREFIX_WARNING + "Arena " + args[1] + " couldn't be found!");
			}
			return true;
		}
		return false;
	}
}