package net.minecraftmurder.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.minecraftmurder.commands.MCommandResult.Result;
import net.minecraftmurder.tools.MLogger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;	
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandListener implements CommandExecutor {
	// String label, MCommand command
	public List<MCommand> mCommands;
	
	public CommandListener () {
		mCommands = new ArrayList<MCommand>();
		// Register commands
		mCommands.add(new ArenaCommand("arena"));
		mCommands.add(new CoinCommand("coins"));
		mCommands.add(new MurderCommand("murder"));
		mCommands.add(new WarnCommand("warn"));
		mCommands.add(new SpawnCommand("spawn"));
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		String name = command.getName();
		// Log that the played tried executing a command
		String log = sender.getName() + ": /" + name;
		for (int i = 0; i < args.length; i++)
			log += " " + args[i];
		MLogger.log(Level.INFO, log);
		// Loop through registered commands
		for (MCommand mCommand : mCommands) {
			if (mCommand.getLabel().equalsIgnoreCase(name)) {
				executeCommand(mCommand, sender, args);
				break;
			}	
		}
		return true;
	}
	
	private void executeCommand (MCommand originalMCommand, CommandSender sender, String[] args) {
		String returnedMessage = null;
		// Execute command
		MCommandResult commandResult = originalMCommand.execute(sender, args);
		MCommand executedMCommand = commandResult.getExecutedMCommand();
		// Handle result
		returnedMessage = commandResult.getMessage();
		if (returnedMessage != null) {
			sender.sendMessage((commandResult.getResult() == Result.SUCCESS ? "" : ChatColor.RED + "> ") + returnedMessage);
			MLogger.log(Level.INFO, "Command outputed: " + returnedMessage);
		} else {
			MLogger.log(Level.INFO, "Command outputed: " + commandResult.getResult().toString());
		}
		
		// If the command was not successful
		if (commandResult.getResult() == Result.FAIL_ARGUMENTS) {
			// Format help
			String[] splitHelp = executedMCommand.getHelp().split("\n");
			String[] help = new String[splitHelp.length];
			for (int i = 0; i < splitHelp.length; i++)
				help[i] = ChatColor.RED + "| " + ChatColor.YELLOW + splitHelp[i];
			// Send usage and help
			sender.sendMessage(ChatColor.RED + "> " + ChatColor.GREEN + "/" + executedMCommand.getUsage());
			sender.sendMessage(help);
		}
	}
}