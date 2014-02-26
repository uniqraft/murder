package net.minecraftmurder.commands;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.minecraftmurder.main.Arena;
import net.minecraftmurder.main.MLogger;
import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.main.Murder;
import net.minecraftmurder.managers.MatchManager;
import net.minecraftmurder.matches.PlayMatch;
import net.minecraftmurder.tools.ChatContext;

public class MurderCommand implements CommandExecutor {
	private Murder plugin;
	
	public MurderCommand (Murder plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label,
			String[] args) {
		// murder <action>
		if (args.length != 1) {
			sender.sendMessage(ChatContext.ERROR_ARGUMENTS);
			return false;
		}
		
		if (args[0].equalsIgnoreCase("start")) {
			plugin.start();
			return true;
		} else if (args[0].equalsIgnoreCase("dev")) {
			plugin.activateDevMode();
			MLogger.log(Level.INFO, sender.getName() + " activated dev mode.");
			return true;
		} else if (args[0].equalsIgnoreCase("matches")) {
			String s = "Matches: ";
			MatchManager matchManager = plugin.getMatchManager();
			for (int i = 0; i < MatchManager.MAX_MATCHES; i++) {
				PlayMatch playMatch = matchManager.getPlayMatch(i);
				if (playMatch != null) {
					if (i > 0)
						s += ", ";
					s += i;
				}
			}
			sender.sendMessage(ChatContext.PREFIX_PLUGIN + s);
			return true;
		} else if (args[0].equalsIgnoreCase("arenas")) {
			List<Arena> arenas = plugin.getArenaManager().getAllArenas();
			if (arenas != null && arenas.size() > 0) {
				for (Arena arena: arenas) {
					sender.sendMessage(ChatContext.PREFIX_PLUGIN + "Path: " + arena.getPath() + ", Name: " + arena.getInfo("name") + ", By: " + arena.getInfo("author"));
				}
			} else {
				sender.sendMessage(ChatContext.PREFIX_PLUGIN + "No arenas.");
			}
			return true;
		} else if (args[0].equalsIgnoreCase("mplayers") || args[0].equalsIgnoreCase("players")) {
			for (MPlayer mplayer: plugin.getPlayerManager().getMPlayers()) {
				sender.sendMessage(ChatContext.PREFIX_PLUGIN + mplayer.getName());
			}
			return true;
		} else if (args[0].equalsIgnoreCase("kill")) {
			MPlayer mPlayer = plugin.getMPlayer(sender.getName());
			if (mPlayer != null)
				mPlayer.onDeath();
			return true;
		}
		return false;
	}
}