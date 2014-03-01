package net.minecraftmurder.main;

import java.util.logging.Level;

import net.minecraftmurder.commands.ArenaCommand;
import net.minecraftmurder.commands.CoinCommand;
import net.minecraftmurder.commands.MurderCommand;
import net.minecraftmurder.commands.SpawnCommand;
import net.minecraftmurder.commands.WarnCommand;
import net.minecraftmurder.listeners.BlockListener;
import net.minecraftmurder.listeners.EntityListener;
import net.minecraftmurder.listeners.PlayerListener;
import net.minecraftmurder.managers.ArenaManager;
import net.minecraftmurder.managers.MatchManager;
import net.minecraftmurder.managers.PlayerManager;
import net.minecraftmurder.managers.SignManager;
import net.minecraftmurder.matches.Match;
import net.minecraftmurder.tools.MLogger;
import net.minecraftmurder.tools.TeleportFix;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Murder extends JavaPlugin {
	public static final int GUNBAN_TIME = 60;
	public static final int CRAFTGUNPARTS_COUNT = 5;
	public static final float ARROW_SPEED = 4;
	
	private PlayerManager playerManager;
	private MatchManager matchManager;
	private ArenaManager arenaManager;
	private SignManager signManager;
	
	private static Murder instance;
	
	private boolean started = false;
	private boolean devMode = false;
	
	@Override
	public void onEnable () {
		instance = this;
		
		ArenaManager.initialize();
		MatchManager.initialize();
		PlayerManager.initialize();
		SignManager.initialize();
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlayerListener(this), this);
		pm.registerEvents(new EntityListener(this), this);
		pm.registerEvents(new BlockListener(this), this);
		pm.registerEvents(new TeleportFix(this), this);
		
		// Tell player manager about all already connected players
		for (Player player: getServer().getOnlinePlayers()) {
			getPlayerManager();
			PlayerManager.onPlayerJoin(player);
		}
		
		getCommand("arena").setExecutor(new ArenaCommand(this));
		getCommand("murder").setExecutor(new MurderCommand(this));
		getCommand("spawn").setExecutor(new SpawnCommand(this));
		getCommand("coins").setExecutor(new CoinCommand(this));
		getCommand("warn").setExecutor(new WarnCommand(this));
	}
	
	@Deprecated
	public PlayerManager getPlayerManager () {
		return playerManager;
	}
	@Deprecated
	public MatchManager getMatchManager () {
		return matchManager;
	}
	@Deprecated
	public ArenaManager getArenaManager () {
		return arenaManager;
	}
	@Deprecated
	public SignManager getSignManager () {
		return signManager;
	}
	
	// Reference these methods here for easier usage
	public MPlayer getMPlayer (String player) {
		getPlayerManager();
		return PlayerManager.getMPlayer(player);
	}
	public MPlayer getMPlayer (Player player) {
		getPlayerManager();
		return PlayerManager.getMPlayer(player);
	}
	
	public void start () {
		if (devMode) {
			MLogger.log(Level.SEVERE, "Can't start Murder, plugin in dev mode.");
			return;
		}
		
		if (isStarted())
			return;
		started = true;

		for (MPlayer mplayer: PlayerManager.getMPlayers()) {
			mplayer.setMatch(MatchManager.getLobbyMatch());
		}
		
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new MainLoop(this), 0, 1);
		MLogger.log(Level.INFO, "Murder started");
	}
	public void activateDevMode () {
		if (devMode) return;
		devMode = true;
		MLogger.log(Level.INFO, "Murder is now in dev mode.");
	}
	public boolean isStarted () {
		return started;
	}
	public boolean isDevMode () {
		return devMode;
	}
	
	public static Murder getInstance () {
		return instance;
	}
	
	@Deprecated
	public void sendMessageToPlayersInMatch (String message, Match match) {
		for (MPlayer mPlayer: PlayerManager.getMPlayers()) {
			if (mPlayer.getMatch() == match) {
				mPlayer.getPlayer().sendMessage(message);
			}
		}
	}
}