package net.minecraftmurder.matches;

import net.minecraftmurder.main.Arena;
import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.main.MPlayerClass.PlayerClass;
import net.minecraftmurder.main.Murder;
import net.minecraftmurder.main.Spawn;

import org.bukkit.entity.Player;

public class LobbyMatch extends Match {
	public LobbyMatch (Arena arena, Murder plugin) {
		super(plugin);
		this.arena = arena;
		arena.setActive(true);
	}

	@Override
	public void update() {
		
	}

	@Override
	public void onPlayerJoin(Player player) {
		MPlayer mPlayer = plugin.getMPlayer(player);
		mPlayer.switchClass(PlayerClass.LOBBYMAN);
		
		if (arena == null) return;
		Spawn spawn = arena.getRandomSpawn("player");
		if (spawn == null) return;
		player.teleport(spawn.getLocation());
		
	}

	@Override
	public void onPlayerQuit(Player player) {
		// Teleport player here
	}

	@Override
	public void onPlayerDeath(Player player) {
		// TODO Auto-generated method stub
		
	}
}