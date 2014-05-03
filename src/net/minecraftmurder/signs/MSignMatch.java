package net.minecraftmurder.signs;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.managers.MatchManager;
import net.minecraftmurder.matches.PlayMatch;
import net.minecraftmurder.tools.ChatContext;
import net.minecraftmurder.tools.MLogger;

public class MSignMatch extends MSign {
	private int index;
	
	public MSignMatch (Location location, int index) {
		super(location);
		this.index = index;
		
		if (!MatchManager.createPlayMatch(index)) {
			MLogger.log(Level.WARNING, "Sign couldn't create match.");
		}
		
		update();
	}
	
	public int getIndex () {
		return index;
	}
	
	public void update () {
		if (checkIfValid()) {
			final Sign sign = getSign();
			PlayMatch match = MatchManager.getPlayMatch(index);
			sign.setLine(0, ChatColor.BLACK + "Match " + ChatColor.DARK_RED + "#" + index);
			sign.setLine(1, ChatColor.DARK_PURPLE + "" + match.getMPlayers().size() + "/" + PlayMatch.MAX_PLAYERS + " players");
			sign.setLine(2, (match.isPlaying() == true ? ChatColor.RED + "" + match.getAliveMPlayers().size() + " alive" : ChatColor.AQUA + "IN LOBBY"));
			sign.setLine(3, ChatColor.DARK_GREEN + "Click to join.");
			sign.update();
		} else {
			MLogger.log(Level.WARNING, "MSignMath with index " + index + " is not valid.");
		}
	}

	@Override
	public void onInteract(MPlayer mPlayer) {
		PlayMatch playMatch = MatchManager.getPlayMatch(index);
		// If full
		if (playMatch.getMPlayers().size() >= PlayMatch.MAX_PLAYERS) {
			Player player = mPlayer.getPlayer();
			// If not VIP
			if (!player.hasPermission("murder.joinfull")) {
				player.sendMessage(ChatContext.COLOR_WARNING +
						"This match is full. Only VIP and Ultra players can join full matches.");
				return;
			}
		}
		mPlayer.setMatch(playMatch);
	}
}