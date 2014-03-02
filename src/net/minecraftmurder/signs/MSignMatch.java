package net.minecraftmurder.signs;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;

import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.managers.MatchManager;
import net.minecraftmurder.matches.Match;
import net.minecraftmurder.matches.PlayMatch;
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
			Match match = MatchManager.getPlayMatch(index);
			sign.setLine(0, ChatColor.RED + "Match " + index);
			sign.setLine(1, ChatColor.GREEN + "(" + match.getMPlayers().size() + "/" + PlayMatch.MAX_PLAYERS + ") player");
			sign.setLine(2, "Click to join.");
			sign.setLine(3, "");
			sign.update();
		} else {
			MLogger.log(Level.WARNING, "MSignMath with index " + index + " is not valid.");
		}
	}

	@Override
	public void onInteract(MPlayer mPlayer) {
		mPlayer.setMatch(MatchManager.getPlayMatch(index));
	}
}