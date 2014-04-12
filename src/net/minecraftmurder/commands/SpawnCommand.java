package net.minecraftmurder.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraftmurder.commands.MCommandResult.Result;
import net.minecraftmurder.main.Arena;
import net.minecraftmurder.main.Spawn;
import net.minecraftmurder.managers.ArenaManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand extends MCommand {
private final List<MCommand> mCommands;
	
	public SpawnCommand(String label) {
		super(label);
		mCommands = new ArrayList<MCommand>();
		// Register commands
		mCommands.add(new AddCommand("add"));
		mCommands.add(new NearestCommand("near"));
		mCommands.add(new RemoveCommand("remove"));
		mCommands.add(new DensityCommand("density"));
		mCommands.add(new AutoAddCommand("autoadd"));
	}
	
	@Override
public MCommandResult exectute(CommandSender sender, String[] args) {
		// Player
		if (!(sender instanceof Player)) {
			return new MCommandResult(this, Result.FAIL_NOTPLAYER, null);
		}
		// Permission
		else if (!sender.hasPermission("murder.admin")) {
			return new MCommandResult(this, Result.FAIL_PERMISSIONS, null);
		}
		// Arguments
		else if (args.length < 1) {
			return new MCommandResult(this, Result.FAIL_ARGUMENTS, null);
		}
		// Execute
		for (MCommand mCommand : mCommands) {
			if (mCommand.getLabel().equalsIgnoreCase(args[0])) {
				String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
				return mCommand.exectute(sender, newArgs);
			}
		}
		return new MCommandResult(this, Result.FAIL_ARGUMENTS, null);
	}
	@Override
	public String getHelp() {
		String help = ChatColor.GOLD + "Actions:\n" + ChatColor.YELLOW;
		for (MCommand mCommand : mCommands) {
			help += "/" + mCommand.getUsage() + "\n";
		}
		return help;
	}
	@Override
	public String getUsage() {
		return getLabel() + " <action>";
	}
	
	class AddCommand extends MCommand { 
		public AddCommand(String label) {
			super(label);
		}
		@Override
		public MCommandResult exectute(CommandSender sender, String[] args) {
			Player pPlayer = (Player) sender;
			Arena arena;
			// Check arguments
			if (args.length != 2) {
				return new MCommandResult(this, Result.FAIL_ARGUMENTS);
			}
			// Does arena exist?
			else if ((arena = ArenaManager.getArenaByPathname(args[0])) == null) {
				return new MCommandResult(this, Result.FAIL_CUSTOM,
						"The arena " + args[0] + " could not be found.");
			}
			// Valid spawn type?
			else if (!Spawn.TYPES.contains(args[1].toLowerCase())) {
				return new MCommandResult(this, Result.FAIL_CUSTOM,
						args[1] + " is not a valid spawn type.");
			}
			// Try to add spawn
			else if (arena.addSpawn(new Spawn(pPlayer.getLocation(), args[1]), true)) {
				return new MCommandResult(this, Result.SUCCESS,
						"Spawn added.");
			} 
			// Couldn't add spawn
			else {
				return new MCommandResult(this, Result.FAIL_CUSTOM,
						"Could not add spawn.");
			}
		}
		@Override
		public String getUsage() {
			return SpawnCommand.this.getLabel() +  " " + getLabel() + " <arena> <type>";
		}
		@Override
		public String getHelp() {
			return "Adds a spawn to an arena.";
		}
	}
	
	class NearestCommand extends MCommand { 
		public NearestCommand(String label) {
			super(label);
		}
		@Override
		public MCommandResult exectute(CommandSender sender, String[] args) {
			Player pPlayer = (Player) sender;
			// Check arguments
			if (args.length != 2)
				return new MCommandResult(this, Result.FAIL_ARGUMENTS);
			// Does arena exist?
			Arena arena = ArenaManager.getArenaByPathname(args[0]);
			if (arena == null)
				return new MCommandResult(this, Result.FAIL_CUSTOM,
						"The arena " + args[0] + " could not be found.");
			// Valid spawn type?
			if (!Spawn.TYPES.contains(args[1].toLowerCase()))
				return new MCommandResult(this, Result.FAIL_CUSTOM,
						args[1] + " is not a valid spawn type.");
			// Try to get spawn
			Spawn spawn = arena.getNearestSpawn(pPlayer.getLocation(), args[1]);
			if (spawn == null)
				return new MCommandResult(this, Result.FAIL_CUSTOM,
						"Couldn't find a spawn.");
			pPlayer.teleport(spawn.getLocation());
			return new MCommandResult(this, Result.SUCCESS);
		}
		@Override
		public String getUsage() {
			return SpawnCommand.this.getLabel() +  " " + getLabel() + " <arena> <type>";
		}
		@Override
		public String getHelp() {
			return "Teleports you to the nearest spawn.";
		}
	}
	
	class RemoveCommand extends MCommand { 
		public RemoveCommand(String label) {
			super(label);
		}
		@Override
		public MCommandResult exectute(CommandSender sender, String[] args) {
			Player pPlayer = (Player) sender;
			// Check arguments
			if (args.length != 2)
				return new MCommandResult(this, Result.FAIL_ARGUMENTS);
			// Does arena exist?
			Arena arena = ArenaManager.getArenaByPathname(args[0]);
			if (arena == null)
				return new MCommandResult(this, Result.FAIL_CUSTOM,
						"The arena " + args[0] + " could not be found.");
			// Valid spawn type?
			if (!Spawn.TYPES.contains(args[1].toLowerCase()))
				return new MCommandResult(this, Result.FAIL_CUSTOM,
						args[1] + " is not a valid spawn type.");
			// Try to get spawn
			Spawn spawn = arena.getNearestSpawn(pPlayer.getLocation(), args[1]);
			if (spawn == null)
				return new MCommandResult(this, Result.FAIL_CUSTOM,
						"Couldn't find a spawn.");
			if (arena.removeSpawn(spawn, true))
				return new MCommandResult(this, Result.SUCCESS,
						"Spawn removed.");
			else
				return new MCommandResult(this, Result.FAIL_CUSTOM,
						"Couldn't remove spawn.");
			
		}
		@Override
		public String getUsage() {
			return SpawnCommand.this.getLabel() +  " " + getLabel() + " <arena> <type>";
		}
		@Override
		public String getHelp() {
			return "Removes the nearest spawn.";
		}
	}
	
	class DensityCommand extends MCommand { 
		public DensityCommand(String label) {
			super(label);
		}
		@Override
		public MCommandResult exectute(CommandSender sender, String[] args) {
			Player pPlayer = (Player) sender;
			// Check arguments
			if (args.length != 3)
				return new MCommandResult(this, Result.FAIL_ARGUMENTS);
			// Does arena exist?
			Arena arena = ArenaManager.getArenaByPathname(args[0]);
			if (arena == null)
				return new MCommandResult(this, Result.FAIL_CUSTOM,
						"The arena " + args[0] + " could not be found.");
			// Valid spawn type?
			if (!Spawn.TYPES.contains(args[1].toLowerCase()))
				return new MCommandResult(this, Result.FAIL_CUSTOM,
						args[1] + " is not a valid spawn type.");
			// Try to convert to integer
			int radius;
			try {
				radius = Integer.parseInt(args[2]);
			} catch (Exception e) {
				return new MCommandResult(this, Result.FAIL_CUSTOM,
						args[2] + " is not a valid number");
			}
			return new MCommandResult(this, Result.SUCCESS,
					Math.round(arena.getSpawnDensity(pPlayer.getLocation(), args[1], radius)*10000.0)/10.0 + " spawns/dm³");
		}
		@Override
		public String getUsage() {
			return SpawnCommand.this.getLabel() +  " " + getLabel() + " <arena> <type> <radius>";
		}
		@Override
		public String getHelp() {
			return "Calculates the density of spawns.";
		}
	}
	
	class AutoAddCommand extends MCommand { 
		public AutoAddCommand(String label) {
			super(label);
		}
		@Override
		public MCommandResult exectute(CommandSender sender, String[] args) {
			Player pPlayer = (Player) sender;
			Arena arena;
			// Check arguments
			if (args.length != 2) {
				return new MCommandResult(this, Result.FAIL_ARGUMENTS);
			}
			// Does arena exist?
			if ((arena = ArenaManager.getArenaByPathname(args[0])) == null) {
				return new MCommandResult(this, Result.FAIL_CUSTOM,
						"The arena " + args[0] + " could not be found.");
			}
			// Try to convert to integer
			int range;
			try {
				range = Integer.parseInt(args[1]);
			} catch (Exception e) {
				return new MCommandResult(this, Result.FAIL_CUSTOM,
						args[1] + " is not a valid number");
			}
			// Try to add spawns
			if (arena.autoAddSpawns(range)) {
				return new MCommandResult(this, Result.SUCCESS,
						"Spawns added.");
			} 
			else {
				return new MCommandResult(this, Result.FAIL_CUSTOM,
						"Could not add spawns.");
			}
		}
		@Override
		public String getUsage() {
			return SpawnCommand.this.getLabel() +  " " + getLabel() + " <arena> <range>";
		}
		@Override
		public String getHelp() {
			return "Adds a spawn to an arena.";
		}
	}

	/*@Override
	public boolean onCommand(CommandSender sender, Command command, String label,
			String[] args) {
		if (args[0].equalsIgnoreCase("types")) {
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
	}*/
}