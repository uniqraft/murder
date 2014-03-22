package net.minecraftmurder.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
		// Permission
		if (!sender.hasPermission("murder.admin"))
			return new MCommandResult(this, Result.FAIL_PERMISSIONS, null);
		// Arguments
		if (args.length < 1)
			return new MCommandResult(this, Result.FAIL_ARGUMENTS, null);
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

	class StartCommand extends MCommand { 
		public StartCommand(String label) {
			super(label);
		}
		@Override
		public MCommandResult exectute(CommandSender sender, String[] args) {
			// Arguments
			if (args.length != 0)
				return new MCommandResult(this, Result.FAIL_ARGUMENTS);
			
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
			return MurderCommand.this.getLabel() +  " <" + getLabel() + ">";
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
			// Check if already started
			if (Murder.getInstance().isDevMode())
				return new MCommandResult(this, Result.FAIL_CUSTOM, "Murder is already in dev mode.");
		
			// Put Murder in gameplay mode
			Murder.getInstance().activateDevMode();
			MLogger.log(Level.INFO, sender.getName() + " activated dev mode.");
			return new MCommandResult(this, Result.SUCCESS, "Murder put in dev mode.");
		}
		@Override
		public String getUsage() {
			return MurderCommand.this.getLabel() +  " <" + getLabel() + ">";
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
			
			// List players
			for (MPlayer mPlayer: PlayerManager.getMPlayers()) {
				sender.sendMessage("> "
						+ ChatContext.COLOR_HIGHLIGHT + mPlayer.getName()
						+ ChatContext.COLOR_LOWLIGHT + " | " + mPlayer.getPlayerClass().toString() + " | " + mPlayer.getMatch().hashCode());
			}
			
			return new MCommandResult(this, Result.SUCCESS);
		}
		@Override
		public String getUsage() {
			return MurderCommand.this.getLabel() +  " <" + getLabel() + ">";
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
			
			// Get targeted player
			Player player = Bukkit.getPlayer(args[0]);
			if (player == null)
				return new MCommandResult(this, Result.FAIL_CUSTOM, "Couldn't find player " + args[0] + "!");
			
			// Find and kill player
			MPlayer mPlayer = PlayerManager.getMPlayer(player);
			mPlayer.onDeath();
			
			return new MCommandResult(this, Result.SUCCESS,
					"Player killed!");
		}
		@Override
		public String getUsage() {
			return MurderCommand.this.getLabel() +  " <" + getLabel() + ">" + " <player>";
		}
		@Override
		public String getHelp() {
			return "Kills the specified player.";
		}
	}
}