package net.minecraftmurder.main;

import net.minecraftmurder.managers.MatchManager;
import net.minecraftmurder.managers.PlayerManager;
import net.minecraftmurder.managers.SignManager;
import net.minecraftmurder.matches.PlayMatch;

public class MainLoop implements Runnable {
	private int tick = 0;
	
	@Override
	public void run() {
		// Decrease all players reload
		for (MPlayer mPlayer: PlayerManager.getMPlayers()) {
			if (mPlayer.getReloadTime() > 0) {
				mPlayer.addReloadTime(-1);
			}
		}
		// Every ½ second
		if (tick % 10 == 0) {
			SignManager.updateSigns();
		}
		// Every second
		if (tick % 20 == 0) {
			runGunBan();
			
			// Update all matches
			for (PlayMatch playMatch: MatchManager.getPlayMatches()) {
				playMatch.update();
			}
			
			MatchManager.getLobbyMatch().update();
		}
		tick++;
	}
	
	void runGunBan () {
		for (MPlayer mplayer: PlayerManager.getMPlayers()) {
			mplayer.decreaseGunBanTime();
		}
	}
}