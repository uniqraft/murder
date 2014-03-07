package net.minecraftmurder.signs;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.managers.MatchManager;
import net.minecraftmurder.matches.Match;
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
			Match match = MatchManager.getPlayMatch(index);
			sign.setLine(0, ChatColor.BLACK + "Match " + ChatColor.DARK_RED + "#" + index);
			sign.setLine(1, ChatColor.DARK_PURPLE + "(" + match.getMPlayers().size() + "/" + PlayMatch.MAX_PLAYERS + ") player");
			sign.setLine(2, ChatColor.DARK_GREEN + "Click to join.");
			sign.setLine(3, "");
			sign.update();
		} else {
			MLogger.log(Level.WARNING, "MSignMath with index " + index + " is not valid.");
		}
	}

	@Override
	public void onInteract(MPlayer mPlayer) {
		Match match = MatchManager.getPlayMatch(index);
		if (match instanceof PlayMatch) {
			PlayMatch playMatch = (PlayMatch) match;
			// If full
			if (playMatch.getMPlayers().size() >= PlayMatch.MAX_PLAYERS) {
				Player player = mPlayer.getPlayer();
				// If VIP
				if (player.hasPermission("murder.joinfull")) {
					if (!playMatch.kickLastNonVIP()) {
						player.sendMessage(ChatContext.PREFIX_PLUGIN + ChatContext.COLOR_WARNING +
								"Match full of VIP players, can't join.");
						return;
					}
				} else {
					player.sendMessage(ChatContext.PREFIX_PLUGIN + ChatContext.COLOR_WARNING +
							"Match full. Only VIPs can join full matches.");
					return;
				}
			}
			mPlayer.setMatch(playMatch);
		}
	}
}