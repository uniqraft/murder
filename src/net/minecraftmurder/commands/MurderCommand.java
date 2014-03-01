package net.minecraftmurder.commands;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.minecraftmurder.main.Arena;
import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.main.Murder;
import net.minecraftmurder.managers.ArenaManager;
import net.minecraftmurder.managers.MatchManager;
import net.minecraftmurder.managers.PlayerManager;
import net.minecraftmurder.matches.PlayMatch;
import net.minecraftmurder.tools.ChatContext;
import net.minecraftmurder.tools.MLogger;

public class MurderCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label,
			String[] args) {
		// murder <action>
		if (args.length != 1) {
			sender.sendMessage(ChatContext.ERROR_ARGUMENTS);
			return false;
		}
		
		if (args[0].equalsIgnoreCase("start")) {
			Murder.getInstance().start();
			return true;
		} else if (args[0].equalsIgnoreCase("dev")) {
			Murder.getInstance().activateDevMode();
			MLogger.log(Level.INFO, sender.getName() + " activated dev mode.");
			return true;
		} else if (args[0].equalsIgnoreCase("matches")) {
			String s = "Matches: ";
			for (int i = 0; i < MatchManager.MAX_MATCHES; i++) {
				PlayMatch playMatch = MatchManager.getPlayMatch(i);
				if (playMatch != null) {
					if (i > 0)
						s += ", ";
					s += i;
				}
			}
			sender.sendMessage(ChatContext.PREFIX_PLUGIN + s);
			return true;
		} else if (args[0].equalsIgnoreCase("arenas")) {
			List<Arena> arenas = ArenaManager.getAllArenas();
			if (arenas != null && arenas.size() > 0) {
				for (Arena arena: arenas) {
					sender.sendMessage(ChatContext.PREFIX_PLUGIN + "Path: " + arena.getPath() + ", Name: " + arena.getInfo("name") + ", By: " + arena.getInfo("author"));
				}
			} else {
				sender.sendMessage(ChatContext.PREFIX_PLUGIN + "No arenas.");
			}
			return true;
		} else if (args[0].equalsIgnoreCase("mplayers") || args[0].equalsIgnoreCase("players")) {
			for (MPlayer mplayer: PlayerManager.getMPlayers()) {
				sender.sendMessage(ChatContext.PREFIX_PLUGIN + mplayer.getName());
			}
			return true;
		} else if (args[0].equalsIgnoreCase("kill")) {
			MPlayer mPlayer = PlayerManager.getMPlayer(sender.getName());
			if (mPlayer != null)
				mPlayer.onDeath();
			return true;
		}
		return false;
	}
}