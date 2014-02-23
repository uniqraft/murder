package net.minecraftmurder.main;

import net.minecraftmurder.matches.PlayMatch;

public class MainLoop implements Runnable {
	private Murder plugin;
	private int tick = 0;
	
	public MainLoop(Murder plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		// Decrease all players reload
		for (MPlayer mPlayer: plugin.getPlayerManager().getMPlayers()) {
			if (mPlayer.getReloadTime() > 0) {
				mPlayer.addReloadTime(-1);
			}
		}
		
		// Every ½ second
		if (tick % 10 == 0) {
			plugin.getSignManager().updateSigns();
		}
		// Every second
		if (tick % 20 == 0) {
			runGunBan();
			
			// Update all matches
			for (PlayMatch playMatch: plugin.getMatchManager().getPlayMatches()) {
				playMatch.update();
			}
			
			plugin.getMatchManager().getLobbyMatch().update();
		}
		
		tick++;
	}
	
	void runGunBan () {
		for (MPlayer mplayer: plugin.getPlayerManager().getMPlayers()) {
			mplayer.decreaseGunBanTime();
		}
	}
}