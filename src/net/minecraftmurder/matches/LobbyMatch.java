package net.minecraftmurder.matches;

import org.bukkit.entity.Player;

import net.minecraftmurder.main.Arena;
import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.main.MPlayerClass;
import net.minecraftmurder.main.Spawn;
import net.minecraftmurder.managers.PlayerManager;

public class LobbyMatch extends Match {
	public LobbyMatch (Arena arena) {
		super();
		this.arena = arena;
		arena.setActive(true);
	}

	@Override
	public void update() {
		
	}

	@Override
	public void onPlayerJoin(Player player) {
		MPlayer mPlayer = PlayerManager.getMPlayer(player);
		mPlayer.switchPlayerClass(MPlayerClass.LOBBYMAN);
		
		if (arena == null) return;
		Spawn spawn = arena.getRandomSpawn("player");
		if (spawn == null) return;
		player.teleport(spawn.getLocation());
		
	}

	@Override
	public void onPlayerQuit(Player player) {}

	@Override
	public void onPlayerDeath(Player player) {}
}