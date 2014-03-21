package net.minecraftmurder.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.minecraftmurder.commands.MCommandResult.Result;
import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.main.Murder;
import net.minecraftmurder.managers.PlayerManager;
import net.minecraftmurder.tools.ChatContext;
import net.minecraftmurder.tools.MLogger;

public class MurderCommand extends MCommand {
	private final List<MCommand> mCommands;
	
	public MurderCommand(String label) {
		super(label);
		mCommands = new ArrayList<MCommand>();
		// Register commands
		mCommands.add(new StartCommand("start"));
		mCommands.add(new DevCommand("dev"));
		mCommands.add(new ListPlayersCommand("list"));
		mCommands.add(new KillCommand("kill"));
	}
	
	@Override
	public MCommandResult exectute(CommandSender sender, String[] args) {
		if (args.length < 1)
			return new MCommandResult(this, Result.FAIL_ARGUMENTS, null);
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
		return "[action]";
	}

	class StartCommand extends MCommand { 
		public StartCommand(String label) {
			super(label);
		}
		@Override
		public MCommandResult exectute(CommandSender sender, String[] args) {
			// Arguments
			if (args.length != 0)
				return new MCommandResult(this, Result.FAIL_ARGUMENTS);
			// Permission
			if (!sender.hasPermission("murder.admin"))
				return new MCommandResult(this, Result.FAIL_PERMISSIONS, null);
			
			// Check if not already started
			if (!Murder.getInstance().isStarted()) {
				MLogger.log(Level.INFO, sender.getName() + " activated gameplay mode.");
				return new MCommandResult(this, Result.SUCCESS, "Murder started.");
			} else {
				return new MCommandResult(this, Result.FAIL_CUSTOM, "Murder has already been started.");
			}
		}
		@Override
		public String getUsage() {
			return MurderCommand.this.getUsage() +  " [ + " + getLabel() + " + ]";
		}
		@Override
		public String getHelp() {
			return "This will put Murder in gameplay mode.";
		}
	}
	class DevCommand extends MCommand { 
		public DevCommand(String label) {
			super(label);
		}
		@Override
		public MCommandResult exectute(CommandSender sender, String[] args) {
			// Arguments
			if (args.length != 0)
				return new MCommandResult(this, Result.FAIL_ARGUMENTS);
			// Permission
			if (!sender.hasPermission("murder.admin"))
				return new MCommandResult(this, Result.FAIL_PERMISSIONS, null);
			// Check if already started
			if (!Murder.getInstance().isDevMode())
				return new MCommandResult(this, Result.FAIL_CUSTOM, "Murder has already been started.");
		
			// Put Murder in gameplay mode
			Murder.getInstance().activateDevMode();
			MLogger.log(Level.INFO, sender.getName() + " activated dev mode.");
			return new MCommandResult(this, Result.SUCCESS, "Murder put in dev mode.");
		}
		@Override
		public String getUsage() {
			return MurderCommand.this.getUsage() +  " [ + " + getLabel() + " + ]";
		}
		@Override
		public String getHelp() {
			return "This will put Murder in dev mode.";
		}
	}
	class ListPlayersCommand extends MCommand { 
		public ListPlayersCommand(String label) {
			super(label);
		}
		@Override
		public MCommandResult exectute(CommandSender sender, String[] args) {
			// Arguments
			if (args.length != 0)
				return new MCommandResult(this, Result.FAIL_ARGUMENTS);
			// Permission
			if (!sender.hasPermission("murder.admin"))
				return new MCommandResult(this, Result.FAIL_PERMISSIONS, null);
			
			// List players
			for (MPlayer mPlayer: PlayerManager.getMPlayers()) {
				sender.sendMessage(
						ChatContext.COLOR_HIGHLIGHT + ">" + mPlayer.getName()
						+ ChatContext.COLOR_LOWLIGHT + "|" + mPlayer.getPlayerClass().toString());
			}
			
			return new MCommandResult(this, Result.SUCCESS);
		}
		@Override
		public String getUsage() {
			return MurderCommand.this.getUsage() +  " [ + " + getLabel() + " + ]";
		}
		@Override
		public String getHelp() {
			return "Lists all players.";
		}
	}
	class KillCommand extends MCommand { 
		public KillCommand(String label) {
			super(label);
		}
		@Override
		public MCommandResult exectute(CommandSender sender, String[] args) {
			// Arguments
			if (args.length != 1)
				return new MCommandResult(this, Result.FAIL_ARGUMENTS);
			// Permission
			if (!sender.hasPermission("murder.admin"))
				return new MCommandResult(this, Result.FAIL_PERMISSIONS, null);
			
			// Get targeted player
			Player player = Bukkit.getPlayer(args[0]);
			if (player == null)
				return new MCommandResult(this, Result.FAIL_CUSTOM, "Couldn't find player " + args[0] + "!");
			
			// Find and kill player
			MPlayer mPlayer = PlayerManager.getMPlayer(player);
			mPlayer.onDeath();
			
			return new MCommandResult(this, Result.SUCCESS);
		}
		@Override
		public String getUsage() {
			return MurderCommand.this.getUsage() +  " [ + " + getLabel() + " + ]";
		}
		@Override
		public String getHelp() {
			return "Kills a player.";
		}
	}
}