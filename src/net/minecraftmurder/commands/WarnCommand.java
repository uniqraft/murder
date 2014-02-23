package net.minecraftmurder.commands;

import javax.swing.text.html.HTMLDocument.HTMLReader.PreAction;

import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.main.Murder;
import net.minecraftmurder.tools.ChatContext;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class WarnCommand implements CommandExecutor {
	
	private Murder plugin;
	
	public WarnCommand (Murder plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label,
			String[] args) {
		// warn <player> [level]
		if (args.length != 2) {
			sender.sendMessage(ChatContext.ERROR_ARGUMENTS);
			sender.sendMessage(ChatContext.PREFIX_PLUGIN + "/warn <player> [level]");
			return true;
		}
		int level = 0;
		try {
			level = Integer.parseInt(args[1]);
		} catch (Exception e) {
			sender.sendMessage(ChatContext.PREFIX_WARNING + e.getMessage());
			return true;
		}
		if (level <= 0) {
			sender.sendMessage(ChatContext.PREFIX_WARNING + "Warn level must be higher than 0.");
			return true;
		}
		sender.sendMessage(ChatContext.PREFIX_PLUGIN + "Player warned.");
		MPlayer.addWarnLevel(args[0], level);
		return true;
	}

}
