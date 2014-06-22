package net.minecraftmurder.commands;

import java.util.logging.Level;

import net.minecraftmurder.commands.MCommandResult.Result;
import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.managers.PlayerManager;
import net.minecraftmurder.matches.PlayMatch;
import net.minecraftmurder.tools.ChatContext;
import net.minecraftmurder.tools.MLogger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReportCommand extends MCommand {
	public ReportCommand(String label) {
		super(label);
	}

	@Override
	public MCommandResult execute(CommandSender sender, String[] args) {
		MPlayer reporter = PlayerManager.getMPlayer((Player) sender);
		MPlayer reported = PlayerManager.getMPlayer(args[0]);
		String message = "";
		
		if (reported == null) {
			sender.sendMessage(ChatContext.COLOR_ERROR + "Can't find player " 
				+ ChatColor.ITALIC + ChatContext.COLOR_HIGHLIGHT + args[0]);
			return new MCommandResult(this, Result.FAIL_NOTPLAYER);
		}
		
		if (!reporter.getMatch().equals(reported.getMatch())) {
			sender.sendMessage(ChatContext.COLOR_ERROR + "Not in same match as player " + ChatColor.ITALIC + reported.getName());
			return new MCommandResult(this, Result.FAIL_CUSTOM);
		}
		
		for (int i = 1; i < args.length; i++) {
			message += args[i] + " ";
		}
		
		// Yes, I'm literally just going to abuse MLogger for this. I dunno a better method and I'm kind of ashamed...
		MLogger.log(Level.WARNING, ChatColor.BOLD + ChatContext.COLOR_LOWLIGHT + "[REPORT] " + ChatColor.RESET 
						+ ChatContext.COLOR_LOWLIGHT + "Player " + ChatContext.COLOR_INNOCENT 
						+ reporter.getName() + ChatContext.COLOR_LOWLIGHT + "reported player " 
						+ ChatContext.COLOR_MURDERER + reported.getName() + ChatContext.COLOR_HIGHLIGHT 
						+ " in " + 
						(reporter.getMatch() instanceof PlayMatch ? " match " + ((PlayMatch) reporter.getMatch()).getIndex() : "lobby") 
						+ ": " + ChatContext.COLOR_MAIN + message);
		return new MCommandResult(this, Result.SUCCESS);
	}

	@Override
	public String getUsage() {
		return getLabel() + "<name> [report]";
	}

	@Override
	public String getHelp() {
		return "Reports a player.";
	}

	
	
}
