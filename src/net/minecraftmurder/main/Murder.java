package net.minecraftmurder.main;

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
	
	private boolean started = false;
	
	@Override
	public void onEnable () {
		arenaManager = new ArenaManager(this);
		matchManager = new MatchManager(this);
		playerManager = new PlayerManager(this);
		signManager = new SignManager(this);
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlayerListener(this), this);
		pm.registerEvents(new EntityListener(this), this);
		pm.registerEvents(new BlockListener(this), this);
		pm.registerEvents(new TeleportFix(this), this);
		
		// Tell player manager about all already connected players
		for (Player player: getServer().getOnlinePlayers()) {
			getPlayerManager().onPlayerJoin(player);
		}
		
		getCommand("arena").setExecutor(new ArenaCommand(this));
		getCommand("murder").setExecutor(new MurderCommand(this));
		getCommand("spawn").setExecutor(new SpawnCommand(this));
		getCommand("coins").setExecutor(new CoinCommand(this));
		getCommand("warn").setExecutor(new WarnCommand(this));
	}
	
	public PlayerManager getPlayerManager () {
		return playerManager;
	}
	public MatchManager getMatchManager () {
		return matchManager;
	}
	public ArenaManager getArenaManager () {
		return arenaManager;
	}
	public SignManager getSignManager () {
		return signManager;
	}
	
	// Reference these methods here for easier usage
	public MPlayer getMPlayer (String player) {
		return getPlayerManager().getMPlayer(player);
	}
	public MPlayer getMPlayer (Player player) {
		return getPlayerManager().getMPlayer(player);
	}
	
	public void start () {
		if (isStarted())
			return;
		
		started = true;

		for (MPlayer mplayer: getPlayerManager().getMPlayers()) {
			mplayer.setMatch(getMatchManager().getLobbyMatch());
		}
		
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new MainLoop(this), 0, 1);
	}
	public boolean isStarted () {
		return started;
	}
	
	public void sendMessageToPlayersInMatch (String message, Match match) {
		for (MPlayer mPlayer: getPlayerManager().getMPlayers()) {
			if (mPlayer.getMatch() == match) {
				mPlayer.getPlayer().sendMessage(message);
			}
		}
	}
}