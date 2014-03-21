package net.minecraftmurder.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.minecraftmurder.commands.MCommandResult.Result;
import net.minecraftmurder.commands.MurderCommand.DevCommand;
import net.minecraftmurder.commands.MurderCommand.KillCommand;
import net.minecraftmurder.commands.MurderCommand.ListPlayersCommand;
import net.minecraftmurder.commands.MurderCommand.StartCommand;
import net.minecraftmurder.main.Arena;
import net.minecraftmurder.main.Murder;
import net.minecraftmurder.managers.ArenaManager;
import net.minecraftmurder.tools.ChatContext;
import net.minecraftmurder.tools.MLogger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaCommand extends MCommand {
	private final String INFO_HELP = 
			"Sets information about an arena.\n"
			+ "- name\n"
			+ "The arena's name.\n"
			+ "- world\n"
			+ "The name of the world the arena is located in.\n"
			+ "- author\n"
			+ "The creator(s) of the arena.\n"
			+ "- min-y\n"
			+ "Must be a whole number. If a player falls below this point they'll be killed.";
	private final List<MCommand> mCommands;
	
	public ArenaCommand(String label) {
		super(label);
		mCommands = new ArrayList<MCommand>();
		// Register commands
	}
	
	@Override
	public MCommandResult exectute(CommandSender sender, String[] args) {
		// Permission
		if (!sender.hasPermission("murder.admin"))
			return new MCommandResult(this, Result.FAIL_PERMISSIONS, null);
		// Arguments
		if (args.length < 1)
			return new MCommandResult(this, Result.FAIL_ARGUMENTS, null);
		// Execute
		for (MCommand mCommand : mCommands) {
			if (mCommand.getLabel().equalsIgnoreCase(args[0])) {
				String[] newArgs = new String[args.length - 1];
				for (int i = 0; i < args.length - 1; i++)
					newArgs[i] = args[i - 1];
				return mCommand.exectute(sender, newArgs);
			}
		}
		return new MCommandResult(this, Result.FAIL_ARGUMENTS, null);
	}
	@Override
	public String getHelp() {
		String help = "Actions:\n";
		for (MCommand mCommand : mCommands) {
			help += mCommand.getUsage() + "\n";
		}
		return help;
	}
	@Override
	public String getUsage() {
		return "<action>";
	}
	
	class CreateCommand extends MCommand { 
		public CreateCommand(String label) {
			super(label);
		}
		@Override
		public MCommandResult exectute(CommandSender sender, String[] args) {
			// Player
			if (!(sender instanceof Player))
				return new MCommandResult(this, Result.FAIL_NOTPLAYER);
			Player player = (Player) sender;
			// Arguments
			if (args.length != 2)
				return new MCommandResult(this, Result.FAIL_ARGUMENTS);
			
			// Create arena
			Arena arena = ArenaManager.createArena(args[1]); 
			// If couldn't create
			if (arena == null)
				return new MCommandResult(this, Result.FAIL_CUSTOM,
						"Couldn't create arena!");
			
			arena.setInfo("world", player.getWorld().getName(), false);
			arena.setInfo("name", player.getWorld().getName(), false);
			arena.setInfo("author", player.getName(), true);
			return new MCommandResult(this, Result.SUCCESS,
					"Arena created");
		}
		@Override
		public String getUsage() {
			return ArenaCommand.this.getUsage() +  " < + " + getLabel() + " + >  <name>";
		}
		@Override
		public String getHelp() {
			return "Creates an arena yml file.";
		}
	}
	
	class SetCommand extends MCommand { 
		public SetCommand(String label) {
			super(label);
		}
		@Override
		public MCommandResult exectute(CommandSender sender, String[] args) {
			// Arguments
			if (args.length != 3)
				return new MCommandResult(this, Result.FAIL_ARGUMENTS);
			// Find arena
			Arena arena = ArenaManager.getArenaByPathname(args[0]);
			if  (arena == null)
				return new MCommandResult(this, Result.FAIL_CUSTOM,
						"Couldn't find Arena.");
			
			if (args[1].equalsIgnoreCase("min-y")) {
				try {
					if (!arena.setMinY(Integer.parseInt(args[2]), true)) {
						return new MCommandResult(this, Result.FAIL_CUSTOM,
								"Couldn't set info.");
					}
				} catch (Exception e) {
					return new MCommandResult(this, Result.FAIL_CUSTOM, 
							args[2] + " is not a valid number.");
				}
			} else if (Arena.INFO_TYPES.contains(args[1].toLowerCase())) {
				if (!arena.setInfo(args[1], args[2], true)) {
					return new MCommandResult(this, Result.FAIL_CUSTOM,
							"Couldn't set info.");
				}
			}
			return new MCommandResult(this, Result.SUCCESS,
					args[1] + "'s " + args[2] + " was set to " + args[3] + ".");
		}
		@Override
		public String getUsage() {
			return ArenaCommand.this.getUsage() +  " < + " + getLabel() + " + > <name> <type> <info>";
		}
		@Override
		public String getHelp() {
			return INFO_HELP;
		}
	}
	
	class GetCommand extends MCommand {
		public GetCommand(String label) {
			super(label);
		}
		@Override
		public MCommandResult exectute(CommandSender sender, String[] args) {
			// Arguments
			if (args.length != 2)
				return new MCommandResult(this, Result.FAIL_ARGUMENTS);
			
			// Create arena
			Arena arena = ArenaManager.getArenaByPathname(args[0]);
			if  (arena == null)
				return new MCommandResult(this, Result.FAIL_CUSTOM,
						"Couldn't find Arena.");
			if (args[1].equalsIgnoreCase("min-y")) {
				return new MCommandResult(this, Result.SUCCESS,
						args[0] + "'s min-y is " + arena.getMinY());
			} else if (Arena.INFO_TYPES.contains(args[1].toLowerCase())) {
				return new MCommandResult(this, Result.SUCCESS,
						args[0] + "'s " + args[1] + " is " + arena.getInfo(args[1]));
			}
			return new MCommandResult(this, Result.FAIL_CUSTOM,
					args[1] + " is not a valid type.");
		}
		@Override
		public String getUsage() {
			return ArenaCommand.this.getUsage() +  " < + " + getLabel() + " + > <name> <type>";
		}
		@Override
		public String getHelp() {
			return INFO_HELP;
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label,
			String[] args) {
		// arena <action> [name] [info-type] [info]
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatContext.PREFIX_WARNING + "Only players may execute this command.");
			return false;
		}
		Player player = (Player) sender;
		if (!sender.hasPermission("murder.admin")) {
			return false;
		}
		
		if (args.length < 1) {
			sender.sendMessage(ChatContext.ERROR_ARGUMENTS);
			return false;
		}
		
		if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("create")) {
			
		} else if (args[0].equalsIgnoreCase("set")) {
			if (args.length != 4) {
				sender.sendMessage(ChatContext.ERROR_ARGUMENTS);
				return false;
			}
			Arena arena = ArenaManager.getArenaByPathname(args[1]);
			if (arena != null) {
				if (args[2].equalsIgnoreCase("min-y")) {
					try {
						arena.setMinY(Integer.parseInt(args[3]), true);
						sender.sendMessage(ChatContext.PREFIX_PLUGIN + args[1] + "'s " + args[2] + " was set to " + args[3] + ".");
					} catch (Exception e) {
						sender.sendMessage(args[3] + " is not a valid number.");
					}
				} else if (Arena.INFO_TYPES.contains(args[2].toLowerCase())) {
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
			Arena arena = ArenaManager.getArenaByPathname(args[1]);
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