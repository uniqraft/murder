package net.minecraftmurder.commands;

import net.minecraftmurder.main.Arena;
import net.minecraftmurder.main.Spawn;
import net.minecraftmurder.managers.ArenaManager;
import net.minecraftmurder.tools.ChatContext;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label,
			String[] args) {
		// spawn <action> [name] [info] [radius]
		if (!sender.hasPermission("murder.admin")) {
			return false;
		}
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatContext.ERROR_NOTPLAYERSENDER);
			return true;
		}
		if (args.length < 1 || args.length > 4) {
			sender.sendMessage(ChatContext.ERROR_ARGUMENTS);
			return true;
		}
		Player player = (Player) sender;
		
		if (args[0].equalsIgnoreCase("add")) {
			// TODO Make this fancy, like the others
			if (args.length != 3) {
				sender.sendMessage(ChatContext.ERROR_ARGUMENTS);
				sender.sendMessage(ChatContext.PREFIX_PLUGIN + "/spawn add <arena> <type>");
				return true;
			}
			Arena arena = ArenaManager.getArenaByPathname(args[1]);
			if (arena != null) {
				if (Spawn.TYPES.contains(args[2])) {
					if (arena.addSpawn(new Spawn(player.getLocation().add(0, 1, 0), args[2]), true)) {
						sender.sendMessage(ChatContext.PREFIX_PLUGIN + "Spawn added.");
					} else {
						sender.sendMessage(ChatContext.PREFIX_CRITICAL + "Something went wrong when adding a spawn.");
					}
				} else {
					sender.sendMessage(ChatContext.PREFIX_WARNING + args[2] + " is not a valid spawn type.");
				}
			} else {
				sender.sendMessage(ChatContext.PREFIX_WARNING + "Couldn't find arena " + args[1]);
			}
			return true;
		} else if (args[0].equalsIgnoreCase("nearest")) {
			// TODO Make this fancy, like the others
			// Teleport sender to nearest spawn
			if (args.length != 3) {
				sender.sendMessage(ChatContext.ERROR_ARGUMENTS);
				sender.sendMessage(ChatContext.PREFIX_PLUGIN + "/spawn nearest <arena> <type>");
				return true;
			}
			Arena arena = ArenaManager.getArenaByPathname(args[1]);
			if (arena != null) {
				if (Spawn.TYPES.contains(args[2])) {
					Spawn spawn = arena.getNearestSpawn(player.getLocation(), args[2]);
					if (spawn != null) {
						player.teleport(spawn.getLocation());
						return true;
					} else {
						sender.sendMessage(ChatContext.PREFIX_WARNING + "Couldn't find spawn");
					}
				} else {
					sender.sendMessage(ChatContext.PREFIX_WARNING + args[2] + " is not a valid spawn type.");
				}
			} else {
				sender.sendMessage(ChatContext.PREFIX_WARNING + "Couldn't find arena " + args[1]);
			}
		} else if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("delete")) {
			// Remove nearest spawn
			if (args.length != 3) {
				sender.sendMessage(ChatContext.ERROR_ARGUMENTS);
				sender.sendMessage(ChatContext.PREFIX_PLUGIN + "/spawn remove <arena> <type>");
				return true;
			}
			// Does arena exist?
			Arena arena = ArenaManager.getArenaByPathname(args[1]);
			if (arena == null) {
				sender.sendMessage(ChatContext.PREFIX_WARNING + "Couldn't find arena " + args[1] + "!");
				return false;
			}
			// Is it a valid spawn type?
			if (!Spawn.TYPES.contains(args[2].toLowerCase())) {
				sender.sendMessage(ChatContext.PREFIX_WARNING + args[2] + " is not a valid spawn type!");
				return false;
			}
			// Can we get a spawn?
			Spawn spawn = arena.getNearestSpawn(player.getLocation(), args[2]);
			if (spawn == null) {
				sender.sendMessage(ChatContext.PREFIX_WARNING + "Couldn't find spawn!");
				return false;
			}
			// Remove spawn
			if (arena.removeSpawn(spawn, true)) {
				sender.sendMessage(ChatContext.PREFIX_PLUGIN + "Spawn removed.");
				return true;
			}
			sender.sendMessage(ChatContext.PREFIX_CRITICAL + "Something went wrong when removing spawn!");
		} else if (args[0].equalsIgnoreCase("density")) {
			// Remove nearest spawn
			if (args.length != 4) {
				sender.sendMessage(ChatContext.ERROR_ARGUMENTS);
				sender.sendMessage(ChatContext.PREFIX_PLUGIN + "/spawn density <arena> <type> <radius>");
				return true;
			}
			// Does arena exist?
			Arena arena = ArenaManager.getArenaByPathname(args[1]);
			if (arena == null) {
				sender.sendMessage(ChatContext.PREFIX_WARNING + "Couldn't find arena " + args[1] + "!");
				return true;
			}
			// Is it a valid spawn type?
			if (!Spawn.TYPES.contains(args[2].toLowerCase())) {
				sender.sendMessage(ChatContext.PREFIX_WARNING + args[2] + " is not a valid spawn type!");
				return true;
			}
			// Is the third argument an integer?
			int radius;
			try {
				radius = Integer.parseInt(args[3]);
			} catch (Exception e) {
				sender.sendMessage(ChatContext.PREFIX_WARNING + args[3] + " is not a valid number!");
				return false;
			}
			sender.sendMessage(ChatContext.PREFIX_PLUGIN + arena.getSpawnDensity(player.getLocation(), args[2], radius)*1000 + " spawns/1000m³");
			return true;
		} else if (args[0].equalsIgnoreCase("types")) {
			sender.sendMessage(ChatContext.PREFIX_PLUGIN + Spawn.TYPES.toString());
			return true;
		} else if (args[0].equalsIgnoreCase("count")) {
			// Remove nearest spawn
			if (args.length != 4) {
				sender.sendMessage(ChatContext.ERROR_ARGUMENTS);
				sender.sendMessage(ChatContext.PREFIX_PLUGIN + "/spawn count <arena> <type>");
				return true;
			}
			// Does arena exist?
			Arena arena = ArenaManager.getArenaByPathname(args[1]);
			if (arena == null) {
				sender.sendMessage(ChatContext.PREFIX_WARNING + "Couldn't find arena " + args[1] + "!");
				return true;
			}
			// Is it a valid spawn type?
			if (!Spawn.TYPES.contains(args[2].toLowerCase())) {
				sender.sendMessage(ChatContext.PREFIX_WARNING + args[2] + " is not a valid spawn type!");
				return true;
			}
			sender.sendMessage(ChatContext.PREFIX_PLUGIN + arena.getSpawns(args[2]).size());
			return true;
		} else {
			sender.sendMessage(ChatContext.PREFIX_WARNING + "Not a valid action. Valid actions are:");
			sender.sendMessage(ChatContext.PREFIX_WARNING + "add/remove/nearest/density/count");
			return true;
		}
		return false;
	}
}