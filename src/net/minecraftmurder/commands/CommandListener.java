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
		mCommands.add(new MurderCommand("murder"));
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
		// Create arguments for command
		String[] newArgs = new String[args.length - 1];
		for (int i = 0; i < args.length - 1; i++)
			newArgs[i] = args[i-2];
		// Execute command
		MCommandResult commandResult = originalMCommand.exectute(sender, newArgs);
		MCommand executedMCommand = commandResult.getExecutedMCommand();
		// Handle result
		returnedMessage = executedMCommand.getCustomMessage();
		sender.sendMessage(returnedMessage);
		MLogger.log(Level.INFO, "Command outputed: " + returnedMessage);
		// If the command was not successful
		if (commandResult.getResult() != Result.SUCCESS) {
			// Format help
			String[] splitHelp = executedMCommand.getHelp().split("\n");
			String[] help = new String[splitHelp.length];
			for (int i = 0; i < splitHelp.length; i++)
				help[i] = ChatColor.RED + "| " + ChatColor.YELLOW + splitHelp[i];
			// Send usage and help
			sender.sendMessage(ChatColor.RED + "> " + ChatColor.YELLOW + "/" + executedMCommand.getUsage());
			sender.sendMessage(help);
		}
	}
}