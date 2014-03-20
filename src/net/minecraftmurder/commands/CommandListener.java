package net.minecraftmurder.commands;

import java.util.ArrayList;
import java.util.List;

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
		String[] newArgs = new String[args.length-2];
		String returnedMessage = null;
		// Loop through registered commands
		for (MCommand mCommand : mCommands) {
			if (mCommand.getLabel().equalsIgnoreCase(name)) {
				switch (mCommand.exectute(newArgs)) {
				case SUCCESS:
					returnedMessage = ChatColor.YELLOW.toString();
					if (mCommand.getCustomMessage() != null)
						returnedMessage += mCommand.getCustomMessage();
					break;
				case FAIL_ARGUMENTS:
					returnedMessage = ChatColor.GOLD.toString() + 
					"Invalid arguments.";
					break;
				case FAIL_PEMISSIONS:
					returnedMessage = ChatColor.RED.toString() +
					"You don't have permission to use this command.";
					break;
				case FAIL_CUSTOM:
					returnedMessage = ChatColor.RED.toString();
					if (mCommand.getCustomMessage() != null)
						returnedMessage += mCommand.getCustomMessage();
					else
						throw new NullPointerException("Command " + mCommand.getLabel() + " returned FAIL_CUSTOM, but didn't set custom message.");
					break;
				}
				break;
			}	
		}
		return true;
	}
}