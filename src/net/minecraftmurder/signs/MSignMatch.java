package net.minecraftmurder.signs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.main.Murder;
import net.minecraftmurder.matches.Match;
import net.minecraftmurder.matches.PlayMatch;
import net.minecraftmurder.tools.ChatContext;
import net.minecraftmurder.tools.Tools;

public class MSignMatch extends MSign {
	private int index;
	
	public MSignMatch (Location location, int index, Murder plugin) {
		super(location, plugin);
		this.index = index;
		
		if (!plugin.getMatchManager().createPlayMatch(index)) {
			Tools.sendMessageAll(ChatContext.PREFIX_WARNING + "Sign couldn't create match.");
		}
		
		update();
	}
	
	public int getIndex () {
		return index;
	}
	
	public void update () {
		if (checkIfValid()) {
			final Sign sign = getSign();
			Match match = plugin.getMatchManager().getPlayMatch(index);
			sign.setLine(0, "Match " + index);
			sign.setLine(1, "(" + match.getMPlayers().size() + "/" + PlayMatch.MAX_PLAYERS + ") player");
			sign.setLine(2, "Click to join.");
			sign.setLine(3, ChatColor.GREEN + "Green?");
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() {
					sign.update(true);
				}
			}, 1);
		}
	}
	
	public Sign getSign () {
		Block block = location.getBlock();
		if (block.getState() instanceof Sign) {
			return (Sign) block.getState();
		}
		return null;
	}

	@Override
	public void onInteract(MPlayer mPlayer) {
		mPlayer.setMatch(plugin.getMatchManager().getPlayMatch(index));
	}
}