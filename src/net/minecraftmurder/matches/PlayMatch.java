package net.minecraftmurder.matches;

import java.security.SecureRandom;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import net.minecraftmurder.main.Arena;
import net.minecraftmurder.main.MLogger;
import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.main.MPlayerClass;
import net.minecraftmurder.main.MPlayerClass.PlayerClass;
import net.minecraftmurder.main.Murder;
import net.minecraftmurder.main.Spawn;
import net.minecraftmurder.tools.ChatContext;
import net.minecraftmurder.tools.Tools;

public class PlayMatch extends Match {
	public static final int MATCH_TIME = 60;
	public static final int COUNTDOWN_TIME = 20;
	public static final int MATCHEND_TIME = 10;
	public static final int MIN_PLAYERS = 2;
	public static final int MAX_PLAYERS = 12;
	public static final int MIN_PLAYERS_RANKED = 2;
	
	private boolean isPlaying;
	private boolean isRanked;
	private int countdown;
	
	private String murderer;
	private String murdererKiller;
	
	public PlayMatch (Murder plugin) {
		super(plugin);
		
		isPlaying = false;
		isRanked = false;
		countdown = COUNTDOWN_TIME;
		
		switchArena();
	}
	public void switchArena() {
		// Set current arena to null
		if (arena != null)
			arena.setActive(false);
		
		// Select new arena
		Arena newArena = plugin.getArenaManager().getRandomAvailableArena(); 
		if (newArena == null) {
			MLogger.log(Level.SEVERE, "PlayMatch could not change arena.");
			return;
		}
		
		// Set new arena to active
		arena = newArena;
		arena.setActive(true);
	}
	
	public Location getNearestInnocentLocation (Location from) {
		Location location = null;
		double distance = Double.POSITIVE_INFINITY;
		for (MPlayer mPlayer: getMPlayers()) {
			// Skip if not innocent or gunner
			if (mPlayer.getPlayerClass() != PlayerClass.INNOCENT && mPlayer.getPlayerClass() != PlayerClass.GUNNER)
				continue;
			
			Location playerLocation = mPlayer.getPlayer().getLocation();
			// Skip if not same world
			if (from.getWorld() != playerLocation.getWorld())
				continue;
			
			double d = from.distance(playerLocation);
			if (d < distance) {
				location = playerLocation;
				distance = d;
			}
		}
		return location;
	}
	
	@Override
	public void update() {
		countdown--;
		if (isPlaying) {
			MPlayer mMurderer = null;
			// Loop through all players in this match
			for (MPlayer mPlayer: getMPlayers()) {
				Player player = mPlayer.getPlayer();
				
				// If outside the world, kill the player
				if (player.getLocation().getBlockY() < 0)
					mPlayer.onDeath();
				
				// Find murderer
				if (mPlayer.getPlayerClass() == PlayerClass.MURDERER) {
					mMurderer = mPlayer;
					break;
				}
			}
			// If there is a murderer
			if (mMurderer != null) {
				// Point compass at nearest innocent
				Player player = mMurderer.getPlayer();
				Location to = getNearestInnocentLocation(player.getLocation());
				if (to != null)
					player.setCompassTarget(to);
			}
		} else {
			// If the match hasn't started
			List<MPlayer> mPlayers = getMPlayers();
			int count = mPlayers.size();
			// If empty, reset count down
			if (count < 1) {
				countdown = COUNTDOWN_TIME;
			} 
			// If count down reaches 0
			if (countdown <= 0) {
				if (count < MIN_PLAYERS) {
					// If not enough players
					plugin.sendMessageToPlayersInMatch(ChatContext.MESSAGE_NOTENOUGHPLAYERS, this);
					countdown = COUNTDOWN_TIME-1;
					System.out.println("Not enough players to startch Match " + this.hashCode() + ".");
				} else {
					// If enough players
					System.out.println("Trying to start Match " + this.hashCode() + ".");
					start();
				}
			} else if (countdown % 10 == 0 || (countdown >= 1 && countdown <= 3)) {
				plugin.sendMessageToPlayersInMatch(ChatContext.PREFIX_PLUGIN + "Match starts in " + ChatContext.COLOR_HIGHLIGHT + countdown + " seconds" + ChatContext.COLOR_LOWLIGHT + "!", this);
			}
		}
	}
	
	private void start() {
		if (isPlaying) {
			Tools.sendMessageAll(ChatContext.PREFIX_CRITICAL + "Match tried to start, but is already started.");
			return;
		}
		isPlaying = true;
		
		// Reset variables
		murderer = "";
		murdererKiller = "";
		
		List<MPlayer> mPlayers = getMPlayers();
		int count = mPlayers.size();
		System.out.println(count + " players in Match " + this.hashCode() + ".");
		if (count < MIN_PLAYERS_RANKED) {
			// If not enough players to play ranked
			plugin.sendMessageToPlayersInMatch(ChatContext.MESSAGE_NOTENOUGHPLAYERSRANKED, this);
			plugin.sendMessageToPlayersInMatch(ChatContext.MESSAGE_NOTENOUGHPLAYERSRANKED_2, this);
			isRanked = false;
		} else {
			isRanked = true;
		}
		
		// Select a murderer and a gunner
		SecureRandom random = new SecureRandom();
		int m = random.nextInt(count);
		int g;
		do {
			g = random.nextInt(count);
		} while (g == m);
		
		// Equip murderer
		MPlayer mMurderer = mPlayers.get(m);
		mMurderer.switchClass(PlayerClass.MURDERER);
		// Equip gunner
		MPlayer mGunner = mPlayers.get(g);
		mGunner.switchClass(PlayerClass.GUNNER);
		
		murderer = mMurderer.getName();
		
		System.out.println(mMurderer.getName() + " is the murderer in Match " + this.hashCode() + ".");
		System.out.println(mGunner.getName() + " is the original gunner in Match " + this.hashCode() + ".");
		
		// Tell all players what role they play
		for (MPlayer mPlayer: mPlayers) {
			if (mPlayer == mMurderer) {
				mPlayer.getPlayer().sendMessage(ChatContext.PREFIX_PLUGIN + ChatContext.COLOR_LOWLIGHT + "You are " + ChatContext.COLOR_MURDERER + "The Murderer" + ChatContext.COLOR_LOWLIGHT + "!");
			} else if (mPlayer == mGunner) {
				mPlayer.getPlayer().sendMessage(ChatContext.PREFIX_PLUGIN + ChatContext.COLOR_LOWLIGHT + "You are " + ChatContext.COLOR_INNOCENT + "a Gunner" + ChatContext.COLOR_LOWLIGHT + "!");
			} else {
				mPlayer.getPlayer().sendMessage(ChatContext.PREFIX_PLUGIN + ChatContext.COLOR_LOWLIGHT + "You are " + ChatContext.COLOR_INNOCENT + "an Innocent" + ChatContext.COLOR_LOWLIGHT + "!");
				mPlayer.switchClass(PlayerClass.INNOCENT);
			}
			mPlayer.getPlayer().setGameMode(GameMode.ADVENTURE);
		}
		System.out.println("Match " + this.hashCode() + " started.");
	}
	private void end () {
		isPlaying = false;
		countdown = COUNTDOWN_TIME + MATCHEND_TIME;
		final PlayMatch playMatch = this;
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				System.out.println("Match " + playMatch.hashCode() + " is switching arena.");
				switchArena();
				
				for (MPlayer mPlayer: playMatch.getMPlayers()) {
					// Act like if the player reconnected
					playMatch.onPlayerQuit(mPlayer.getPlayer());
					playMatch.onPlayerJoin(mPlayer.getPlayer());
				}
			}
		}, 20 * MATCHEND_TIME);
	}
	private void checkForEnd () {
		if (!isPlaying)
			return;
		
		MPlayer mMurderer = null;
		int innocentCount = 0;
		for (MPlayer mPlayer: getMPlayers()) {
			if (mPlayer.getPlayerClass() == PlayerClass.MURDERER) {
				mMurderer = mPlayer;
			} else if (mPlayer.getPlayerClass() == PlayerClass.INNOCENT || mPlayer.getPlayerClass() == PlayerClass.GUNNER) {
				innocentCount++;
			}
		}
		if (mMurderer == null) {
			plugin.sendMessageToPlayersInMatch(ChatContext.PREFIX_PLUGIN + ChatContext.COLOR_INNOCENT + "The Innocents" + ChatContext.COLOR_HIGHLIGHT + " wins the match!", this);
			// If someone killed the murderer
			if (murdererKiller != null && !"".equalsIgnoreCase(murdererKiller)) {
				plugin.sendMessageToPlayersInMatch(ChatContext.PREFIX_PLUGIN + ChatContext.COLOR_MURDERER + "The Murderer" + ChatContext.COLOR_LOWLIGHT + ", " + ChatContext.COLOR_HIGHLIGHT + murderer + ChatContext.COLOR_LOWLIGHT + ", was killed by " + ChatContext.COLOR_INNOCENT + murdererKiller + ChatContext.COLOR_LOWLIGHT + "!", this);
			}
			end();
			return;
		}
		if (innocentCount <= 0) {
			plugin.sendMessageToPlayersInMatch(ChatContext.PREFIX_PLUGIN + ChatContext.COLOR_MURDERER + "The Murderer" + ChatContext.COLOR_HIGHLIGHT + " wins the match!", this);
			plugin.sendMessageToPlayersInMatch(ChatContext.PREFIX_PLUGIN + ChatContext.COLOR_HIGHLIGHT + murderer + ChatContext.COLOR_LOWLIGHT + " was " + ChatContext.COLOR_MURDERER + "The Murderer" + ChatContext.COLOR_LOWLIGHT + "!", this);
			// Reward the murderer for winning
			if (isRanked)
				MPlayer.addCoins(murderer, 10, true, plugin);
			
			end();
			return;
		}
	}
	
	@Override
	public void onPlayerJoin(Player player) {
		MPlayer mPlayer = plugin.getMPlayer(player);
		mPlayer.switchClass(isPlaying ? PlayerClass.SPECTATOR : PlayerClass.PREGAMEMAN);
		
		if (arena == null) return;
		Spawn spawn = arena.getRandomSpawn("player");
		if (spawn == null) return;
		player.teleport(spawn.getLocation());
	}
	@Override
	public void onPlayerQuit(Player player) {
		checkForEnd();
	}
	@Override
	public void onPlayerDeath(Player player) {
		player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1);
		
		MPlayer mKilled = plugin.getMPlayer(player);
		String killer = mKilled.getKillerName();
		
		// If the murderer was killed
		if (mKilled.getPlayerClass() == PlayerClass.MURDERER) {
			
			murdererKiller = killer;
			// If there was a killer, reward him
			if (killer != null && !"".equalsIgnoreCase(killer) && isRanked)
				MPlayer.addCoins(killer, 5, true, plugin);
		} else {
			MPlayer.addCoins(murderer, 1, true, plugin);
		}
		// Change class into a spectator and check if the match is over
		mKilled.switchClass(PlayerClass.SPECTATOR);
		checkForEnd();
	}
}