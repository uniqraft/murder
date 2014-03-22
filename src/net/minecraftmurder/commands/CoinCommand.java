package net.minecraftmurder.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.minecraftmurder.commands.MCommandResult.Result;
import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.tools.ChatContext;

public class CoinCommand extends MCommand {
private final List<MCommand> mCommands;
	
	public CoinCommand(String label) {
		super(label);
		mCommands = new ArrayList<MCommand>();
		// Register commands
		mCommands.add(new GetCommand("get"));
		mCommands.add(new SetCommand("set"));
		mCommands.add(new AddCommand("add"));
	}
	
	@Override
	public MCommandResult exectute(CommandSender sender, String[] args) {
		// If no arguments were used
		if (args.length < 1) {
			if (!(sender instanceof Player))
				return new MCommandResult(this, Result.FAIL_NOTPLAYER);
			int coins = MPlayer.getCoins(sender.getName());
			sender.sendMessage(
					ChatContext.COLOR_LOWLIGHT + "You have "
					+ ChatContext.COLOR_HIGHLIGHT + coins
					+ ChatContext.COLOR_LOWLIGHT + (coins != 1 ? " coins" : " coin") + "!");
			return new MCommandResult(this, Result.SUCCESS);
		}
		// If sender doesn't have permission
		if (!sender.hasPermission("murder.coins.manage"))
			return new MCommandResult(this, Result.FAIL_ARGUMENTS);
			// Yes, it's supposed to be FAIL_ARGUMENTS.
			// If regular players uses this command we
			// tell them that they may not use arguments
			// on this command.
		
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
		return "Checks how many coins you have.";
	}
	@Override
	public String getUsage() {
		return getLabel();
		// Don't tell regular players about the actions.
		// It's simple anyways, just get, set and add.
	}
	class GetCommand extends MCommand { 
		public GetCommand(String label) {
			super(label);
		}
		@Override
		public MCommandResult exectute(CommandSender sender, String[] args) {
			// Arguments
			if (args.length != 1)
				return new MCommandResult(this, Result.FAIL_ARGUMENTS);
			
			int coins = MPlayer.getCoins(args[0]);
			sender.sendMessage(
					ChatContext.COLOR_LOWLIGHT + args[0] + " has "
					+ ChatContext.COLOR_HIGHLIGHT + coins
					+ ChatContext.COLOR_LOWLIGHT + (coins != 1 ? " coins" : " coin") + "!");
			return new MCommandResult(this, Result.SUCCESS);
		}
		@Override
		public String getUsage() {
			return CoinCommand.this.getUsage() +  " <" + getLabel() + ">" + " <player>";
		}
		@Override
		public String getHelp() {
			return "Checks how many coins a player have.";
		}
	}
	class AddCommand extends MCommand { 
		public AddCommand(String label) {
			super(label);
		}
		@Override
		public MCommandResult exectute(CommandSender sender, String[] args) {
			// Arguments
			if (args.length != 2)
				return new MCommandResult(this, Result.FAIL_ARGUMENTS);
			
			// If argument is an integer
			int addCoins = 0;
			try {
				addCoins = Integer.parseInt(args[1]);
			} catch (Exception e) {
				return new MCommandResult(this, Result.FAIL_CUSTOM,
						args[1] + " cannot be cast into an integer.");
			}
			
			// Give player coins
			MPlayer.addCoins(args[0], addCoins, true, true);
			return new MCommandResult(this, Result.SUCCESS,
					"You gave " + ChatContext.COLOR_HIGHLIGHT + addCoins + ChatContext.COLOR_LOWLIGHT + (addCoins != 1 ? " coins" : " coin") + " to " + args[0] + ".");
		}
		@Override
		public String getUsage() {
			return CoinCommand.this.getUsage() +  " <" + getLabel() + ">" + " <player> <coins>";
		}
		@Override
		public String getHelp() {
			return "Gives a player coins.";
		}
	}
	class SetCommand extends MCommand { 
		public SetCommand(String label) {
			super(label);
		}
		@Override
		public MCommandResult exectute(CommandSender sender, String[] args) {
			// Arguments
			if (args.length != 2)
				return new MCommandResult(this, Result.FAIL_ARGUMENTS);
			
			// If argument is an integer
			int setCoins = 0;
			try {
				setCoins = Integer.parseInt(args[1]);
			} catch (Exception e) {
				return new MCommandResult(this, Result.FAIL_CUSTOM,
						args[1] + " cannot be cast into an integer.");
			}
			
			// Give player coins
			MPlayer.setCoins(args[0], setCoins, true);
			return new MCommandResult(this, Result.SUCCESS,
					"You set " + args[0] + "'s coin count to " + setCoins + ".");
		}
		@Override
		public String getUsage() {
			return CoinCommand.this.getUsage() +  " <" + getLabel() + ">" + " <player> <coins>";
		}
		@Override
		public String getHelp() {
			return "Sets a player's coin count.";
		}
	}
}