package net.minecraftmurder.listeners;

import java.util.logging.Level;

import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.tools.ChatContext;
import net.minecraftmurder.tools.MLogger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

public class VotifierListener implements Listener {
	@EventHandler
	public void onVotifier (VotifierEvent event) {
		Vote vote = event.getVote();
		Player player = Bukkit.getPlayer(vote.getUsername());
		if (player == null) {
			MLogger.log(Level.INFO, "Offline player " + vote.getUsername() + " casted a vote.");
		} else {
			player.sendMessage(ChatContext.COLOR_HIGHLIGHT + "Thank you for voting! " + ChatColor.RED + "<3");
			MLogger.log(Level.INFO, "Online player " + vote.getUsername() + " casted a vote.");
		}
		MPlayer.addCoins(vote.getUsername(), 40, true);
	}
}