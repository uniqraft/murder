package net.minecraftmurder.matches;

import net.minecraftmurder.main.Arena;
import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.main.MPlayerClass;
import net.minecraftmurder.main.Murder;
import net.minecraftmurder.main.Spawn;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
	public void onPlayerJoin(MPlayer mPlayer) {
		mPlayer.switchPlayerClass(MPlayerClass.LOBBYMAN);
		Player player = mPlayer.getPlayer();
		
		if (player.hasPermission("murder.vip")) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 72000, 2), true);
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 72000, 1), true);
		}
		
		if (Murder.getInstance().isDevMode() || arena == null) return;
		Spawn spawn = arena.getRandomSpawn("player");
		if (spawn == null) return;
		player.teleport(spawn.getLocation());
	}

	@Override
	public void onPlayerQuit(MPlayer player) {}

	@Override
	public void onPlayerDeath(MPlayer player) {}
}