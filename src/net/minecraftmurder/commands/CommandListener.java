package net.minecraftmurder.commands;

import java.util.ArrayList;
import java.util.List;

import net.minecraftmurder.commands.MCommandResult.Result;

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
		// TODO Show returnedMessage, format and show help for failed commands
		switch (commandResult.getResult()) {
		case SUCCESS:
			returnedMessage = ChatColor.YELLOW.toString();
			if (executedMCommand.getCustomMessage() != null)
				returnedMessage += executedMCommand.getCustomMessage();
			break;
		case FAIL_ARGUMENTS:
			returnedMessage = ChatColor.GOLD.toString() + 
				"Invalid arguments.";
			break;
		case FAIL_PERMISSIONS:
			returnedMessage = ChatColor.RED.toString() +
				"You don't have permission to use this command.";
			break;
		case FAIL_CUSTOM:
			returnedMessage = ChatColor.RED.toString();
			if (executedMCommand.getCustomMessage() != null)
				returnedMessage += executedMCommand.getCustomMessage();
			else
				throw new NullPointerException("Command " + executedMCommand.getLabel() + " returned FAIL_CUSTOM, but didn't set custom message.");
			break;
		}
		sender.sendMessage(returnedMessage);
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