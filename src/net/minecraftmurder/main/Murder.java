package net.minecraftmurder.main;

import java.lang.reflect.Field;
import java.util.logging.Level;

import net.minecraftmurder.commands.CommandListener;
import net.minecraftmurder.commands.SpawnCommand;
import net.minecraftmurder.listeners.BlockListener;
import net.minecraftmurder.listeners.EntityListener;
import net.minecraftmurder.listeners.InventoryListener;
import net.minecraftmurder.listeners.PlayerListener;
import net.minecraftmurder.listeners.VotifierListener;
import net.minecraftmurder.managers.ArenaManager;
import net.minecraftmurder.managers.MatchManager;
import net.minecraftmurder.managers.PlayerManager;
import net.minecraftmurder.managers.SignManager;
import net.minecraftmurder.tools.MLogger;
import net.minecraftmurder.tools.TeleportFix;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Murder extends JavaPlugin {
	public static final int GUNBAN_TIME = 60;
	public static final int CRAFTGUNPARTS_COUNT = 5;
	public static final float ARROW_SPEED = 4;
	public static EmptyEnchantment emptyEnchantment;
	
	public static int VIP_SLOTS, MAX_PLAYERS;
	public static int COINS_INNOCENT_KILL, COINS_INNOCENT_SURVIVE, COINS_MURDERER_KILL, COINS_MURDERER_WIN;

	private static Murder instance;
	
	private boolean started = false;
	private boolean devMode = false;
	
	@Override
	public void onEnable () {
		instance = this;
		
		// Create custom enchantment
		emptyEnchantment = new EmptyEnchantment(121);
		try {
		    Field f = Enchantment.class.getDeclaredField("acceptingNew");
		    f.setAccessible(true);
		    f.set(null, true);
		} catch (Exception e) {
			MLogger.log(Level.SEVERE, e.getLocalizedMessage());
		}
		try {
			EnchantmentWrapper.registerEnchantment(emptyEnchantment);
		} catch (IllegalArgumentException e){
			MLogger.log(Level.SEVERE, e.getLocalizedMessage());
		}
		
		// Initialize all managers
		ArenaManager.initialize();
		MatchManager.initialize();
		PlayerManager.initialize();
		SignManager.initialize();
		
		// Register all events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlayerListener(), this);
		pm.registerEvents(new EntityListener(), this);
		pm.registerEvents(new BlockListener(), this);
		pm.registerEvents(new TeleportFix(), this);
		pm.registerEvents(new InventoryListener(), this);
		pm.registerEvents(new VotifierListener(), this);
		
		// Instantiate command listener
		CommandListener listener = new CommandListener();
		// Register all commands to the listener
		getCommand("arena").setExecutor(listener);
		getCommand("murder").setExecutor(listener);
		getCommand("spawn").setExecutor(new SpawnCommand());
		getCommand("coins").setExecutor(listener);
		getCommand("warn").setExecutor(listener);
		
		// Setup config
		saveDefaultConfig();
		getConfig().options().copyDefaults(true);
		// Load from config
		COINS_INNOCENT_KILL		= getConfig().getInt("coins.innocent.kill", 0);
		COINS_INNOCENT_SURVIVE	= getConfig().getInt("coins.innocent.survive", 0);
		COINS_MURDERER_KILL		= getConfig().getInt("coins.murderer.kill", 0);
		COINS_MURDERER_WIN		= getConfig().getInt("coins.murderer.win", 0);
		MAX_PLAYERS				= getConfig().getInt("server.max-players", 0);
		VIP_SLOTS				= getConfig().getInt("server.vip-slots", 0);
		
		// Tell player manager about all already connected players
		for (Player player: getServer().getOnlinePlayers()) {
			PlayerManager.onPlayerJoin(player);
		}
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
		
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new MainLoop(), 0, 1);
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
}