package net.minecraftmurder.main;

import java.lang.reflect.Field;
import java.util.logging.Level;

import net.minecraftmurder.commands.ArenaCommand;
import net.minecraftmurder.commands.CoinCommand;
import net.minecraftmurder.commands.MurderCommand;
import net.minecraftmurder.commands.SpawnCommand;
import net.minecraftmurder.commands.WarnCommand;
import net.minecraftmurder.listeners.BlockListener;
import net.minecraftmurder.listeners.EntityListener;
import net.minecraftmurder.listeners.InventoryListener;
import net.minecraftmurder.listeners.PlayerListener;
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
	public static final int MAX_PLAYERS = 2;
	public static final int GUNBAN_TIME = 60;
	public static final int CRAFTGUNPARTS_COUNT = 5;
	public static final float ARROW_SPEED = 4;
	public static EmptyEnchantment emptyEnchantment;

	private static Murder instance;
	
	private boolean started = false;
	private boolean devMode = false;
	
	@Override
	public void onEnable () {
		instance = this;
		
		emptyEnchantment = new EmptyEnchantment(120);
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
		
		ArenaManager.initialize();
		MatchManager.initialize();
		PlayerManager.initialize();
		SignManager.initialize();
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlayerListener(), this);
		pm.registerEvents(new EntityListener(), this);
		pm.registerEvents(new BlockListener(), this);
		pm.registerEvents(new TeleportFix(), this);
		pm.registerEvents(new InventoryListener(), this);
		
		// Tell player manager about all already connected players
		for (Player player: getServer().getOnlinePlayers()) {
			PlayerManager.onPlayerJoin(player);
		}
		
		getCommand("arena").setExecutor(new ArenaCommand());
		getCommand("murder").setExecutor(new MurderCommand());
		getCommand("spawn").setExecutor(new SpawnCommand());
		getCommand("coins").setExecutor(new CoinCommand());
		getCommand("warn").setExecutor(new WarnCommand());
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