package net.minecraftmurder.listeners;

import java.util.logging.Level;

import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.tools.ChatContext;
import net.minecraftmurder.tools.MLogger;

import org.bukkit.Bukkit;
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
		MLogger.log(Level.INFO, player.getName() + " casted a vote.");
		player.sendMessage(ChatContext.PREFIX_PLUGIN + ChatContext.COLOR_HIGHLIGHT + "Thank you for voting! <3");
		MPlayer.addCoins(player.getName(), 15, true);
	}
}