package net.minecraftmurder.matches;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import net.minecraftmurder.main.Arena;
import net.minecraftmurder.main.MPlayer;
import net.minecraftmurder.main.MPlayerClass;
import net.minecraftmurder.main.Murder;
import net.minecraftmurder.main.Spawn;
import net.minecraftmurder.managers.ArenaManager;
import net.minecraftmurder.managers.PlayerManager;
import net.minecraftmurder.tools.ChatContext;
import net.minecraftmurder.tools.MLogger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class PlayMatch extends Match {
	public static final int MATCH_TIME = 60 * 5;
	public static final int COUNTDOWN_TIME = 20;
	public static final int MATCHEND_PREVOTE_TIME = 5;
	public static final int MATCHEND_TIME = 8;
	public static final int MIN_PLAYERS = 2;
	public static final int MAX_PLAYERS = 10;
	public static final int MIN_PLAYERS_RANKED = 4;

	private boolean isPlaying;
	private boolean isRanked;
	/** If this is true, join/quit messages will be hidden */
	private boolean isReloading = false;

	public boolean isReloading() {
		return isReloading;
	}

	public void setReloading(boolean isReloading) {
		this.isReloading = isReloading;
	}

	private int countdown;

	private String murderer;
	private String murdererKiller;

	private List<MPlayer> murdererTicketUsers;
	private List<MPlayer> gunnerTicketUsers;

	private Arena[] voteArenas;
	private int[] voteCounts;

	public PlayMatch() {
		murdererTicketUsers = new ArrayList<MPlayer>();
		gunnerTicketUsers = new ArrayList<MPlayer>();

		isPlaying = false;
		isRanked = false;
		countdown = COUNTDOWN_TIME;

		switchArena();
	}

	public void switchArena() {
		// Mark current arena as inactive
		if (arena != null)
			arena.setActive(false);

		// Select new arena
		Arena newArena = ArenaManager.getRandomAvailableArena();
		if (newArena == null) {
			MLogger.log(Level.SEVERE, "PlayMatch could not change arena.");
			return;
		}

		// Mark the new arena as active
		arena = newArena;
		arena.setActive(true);
	}

	public Location getNearestInnocentLocation(Location from) {
		Location location = null;
		double distance = Double.POSITIVE_INFINITY;
		for (MPlayer mPlayer : getMPlayers()) {
			// Skip if not innocent or gunner
			if (mPlayer.getPlayerClass() != MPlayerClass.INNOCENT
					&& mPlayer.getPlayerClass() != MPlayerClass.GUNNER)
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

	public boolean isPlaying() {
		return isPlaying;
	}

	public boolean isRanked() {
		return isRanked;
	}

	public List<MPlayer> getAliveMPlayers() {
		List<MPlayer> alive = new ArrayList<MPlayer>();
		for (MPlayer mPlayer : getMPlayers()) {
			MPlayerClass c = mPlayer.getPlayerClass();
			if (c != MPlayerClass.SPECTATOR)
				alive.add(mPlayer);
		}
		return alive;
	}

	@Override
	public void update() {
		if (arena != null && getMPlayers().size() == 0) {
			MLogger.log(Level.INFO, "PlayMatch " + hashCode()
					+ " unloaded arena, as match is empty.");
			arena.setActive(false);
			arena = null;
		}

		countdown--;
		if (isPlaying) {
			MPlayer mMurderer = null;
			// Loop through all players in this match
			for (MPlayer mPlayer : getMPlayers()) {
				Player player = mPlayer.getPlayer();

				// If outside the world, kill the player
				if (player.getLocation().getY() <= getArena().getMinY()) {
					mPlayer.onDeath();
				}

				// Find murderer
				if (mPlayer.getPlayerClass() == MPlayerClass.MURDERER) {
					mMurderer = mPlayer;
				}
			}
			if (countdown <= 0) {
				sendMessage(ChatContext.COLOR_MURDERER + "The Murderer, "
						+ ChatContext.COLOR_HIGHLIGHT + mMurderer.getName()
						+ ChatContext.COLOR_LOWLIGHT + ", ran out of time.");
				mMurderer.onDeath();
			}
			if (countdown % 8 == 0) {
				spawnScrap();
			}
			if (countdown % 60 == 0) {
				sendMessage(ChatContext.COLOR_HIGHLIGHT
						+ (int) (countdown / 60) + ChatContext.COLOR_LOWLIGHT
						+ " minute" + (countdown != 60 ? "s " : " ")
						+ "left of the match.");
			}
			World world = arena.getWorld();
			if (world != null) {
				for (Entity entity : world.getEntities()) {
					if (entity.getType() == EntityType.ARROW) {
						if (entity.getPassenger() != null) {
							world.playEffect(entity.getLocation(),
									Effect.SMOKE, 0);
						}
					}
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

			// If empty, reset count down and cancel
			if (count < 1) {
				countdown = COUNTDOWN_TIME;
				return;
			}

			// Clear all drops
			if (arena != null) {
				World world = arena.getWorld();
				if (world != null) {
					for (Entity e : arena.getWorld().getEntities()) {
						if (e.getType() == EntityType.DROPPED_ITEM
								|| e.getType() == EntityType.ARROW)
							e.remove();
					}
				}
			}
			// If count down reaches 0
			if (countdown <= 0) {
				if (count < MIN_PLAYERS) {
					// If not enough players
					sendMessage(ChatContext.MESSAGE_NOTENOUGHPLAYERS);
					countdown = COUNTDOWN_TIME - 1;
					System.out.println("Not enough players to startch Match "
							+ this.hashCode() + ".");
				} else {
					// If enough players
					System.out.println("Trying to start Match "
							+ this.hashCode() + ".");
					start();
				}
			} else if (countdown % 10 == 0 || (countdown == 3)) {
				sendMessage(ChatContext.COLOR_LOWLIGHT + "Match starts in "
						+ ChatContext.COLOR_HIGHLIGHT + countdown + " second"
						+ (countdown != 1 ? "s" : "")
						+ ChatContext.COLOR_LOWLIGHT + "!");
			}
		}
	}

	private void spawnScrap() {
		Location location = arena.getRandomSpawn("scrap").getLocation();
		location.getWorld().dropItem(location,
				new ItemStack(MPlayerClass.MATERIAL_GUNPART));
	}

	private void start() {
		if (isPlaying) {
			MLogger.log(Level.WARNING,
					"Match tried to start but is already started.");
			return;
		}
		isPlaying = true;

		countdown = MATCH_TIME;

		// Reset variables
		murderer = "";
		murdererKiller = "";

		List<MPlayer> mPlayers = getMPlayers();
		int count = mPlayers.size();
		MLogger.log(Level.INFO, count + " players in Match " + this.hashCode()
				+ ".");
		if (count < MIN_PLAYERS_RANKED) {
			// If not enough players to play ranked
			sendMessage(ChatContext.MESSAGE_NOTENOUGHPLAYERSRANKED);
			sendMessage(ChatContext.MESSAGE_NOTENOUGHPLAYERSRANKED_2);
			isRanked = false;
		} else {
			isRanked = true;
		}

		List<MPlayer> murdererEntires = new ArrayList<MPlayer>();
		List<MPlayer> gunnerEntires = new ArrayList<MPlayer>();
		for (MPlayer mPlayer : getMPlayers()) {
			murdererEntires.add(mPlayer);
			gunnerEntires.add(mPlayer);
		}

		// Increase chance for ticket users
		for (MPlayer ticketUser : murdererTicketUsers) {
			for (int i = 0; i < getMPlayers().size() + 1; i++)
				murdererEntires.add(ticketUser);
		}
		for (MPlayer ticketUser : gunnerTicketUsers) {
			for (int i = 0; i < getMPlayers().size() + 1; i++)
				gunnerEntires.add(ticketUser);
		}

		SecureRandom random = new SecureRandom();

		// Select murderer
		MPlayer mMurderer = murdererEntires.get(random.nextInt(murdererEntires
				.size()));
		gunnerEntires.removeAll(Collections.singleton(mMurderer)); // This
																	// player
																	// can't
																	// become
																	// gunner
		// Select gunner
		MPlayer mGunner = gunnerEntires
				.get(random.nextInt(gunnerEntires.size()));

		// Clear list of players who've used a ticket
		murdererTicketUsers.clear();
		gunnerTicketUsers.clear();

		// Switch their classes
		mMurderer.switchPlayerClass(MPlayerClass.MURDERER);
		mGunner.switchPlayerClass(MPlayerClass.GUNNER);

		murderer = mMurderer.getName();

		// Log
		MLogger.log(Level.INFO, mMurderer.getName() + " is the murderer in "
				+ toString() + ".");
		MLogger.log(Level.INFO, mGunner.getName()
				+ " is the original gunner in " + toString() + ".");

		// Tell all players what role they play
		mMurderer.getPlayer().sendMessage(ChatContext.MESSAGE_CHOSEN_MURDERER);
		mGunner.getPlayer().sendMessage(ChatContext.MESSAGE_CHOSEN_GUNNER);
		for (MPlayer mPlayer : mPlayers) {
			mPlayer.getPlayer().setGameMode(GameMode.ADVENTURE);
			if (mPlayer != mMurderer && mPlayer != mGunner) {
				mPlayer.getPlayer().sendMessage(
						ChatContext.MESSAGE_CHOSEN_INNOCENT);
				mPlayer.switchPlayerClass(MPlayerClass.INNOCENT);
			}
		}
		MLogger.log(Level.INFO, toString() + " started.");
		sendMessage(ChatContext.COLOR_HIGHLIGHT + getMPlayers().size()
				+ ChatContext.COLOR_LOWLIGHT + " out of "
				+ ChatContext.COLOR_HIGHLIGHT + MAX_PLAYERS
				+ ChatContext.COLOR_LOWLIGHT + " players are playing.");
	}

	private void end(boolean murdererWon) {
		MLogger.log(Level.INFO, "Match " + this.hashCode() + " ended.");
		isPlaying = false;
		countdown = COUNTDOWN_TIME + MATCHEND_TIME;

		// What is the point of this line?
		final Arena a = arena;
		final Color color = (murdererWon ? Color.RED : Color.BLUE);

		final int fireworkTask = Bukkit.getScheduler()
				.scheduleSyncRepeatingTask(Murder.getInstance(),
						new Runnable() {
							@Override
							public void run() {
								Random r = new Random();

								// Spawn the Firework, get the FireworkMeta.
								double range = 8d;
								Location spawn = a
										.getRandomSpawn("scrap")
										.getLocation()
										.clone()
										.add(-range + 2 * range
												* r.nextDouble(),
												-range + 2 * range
														* r.nextDouble(),
												-range + 2 * range
														* r.nextDouble());
								Firework fw = (Firework) spawn
										.getWorld()
										.spawnEntity(spawn, EntityType.FIREWORK);
								FireworkMeta fwm = fw.getFireworkMeta();

								Type type = null;
								switch (r.nextInt(3)) {
								case 0:
									type = Type.BALL;
									break;
								case 1:
									type = Type.BURST;
									break;
								case 2:
									type = Type.STAR;
									break;
								default:
									// Should now be impossible.
									type = Type.CREEPER;
									MLogger.log(Level.WARNING,
											"Invalid firework");
									break;
								}

								// Create our effect with this
								FireworkEffect effect = FireworkEffect
										.builder().flicker(r.nextBoolean())
										.withColor(color).withFade(color)
										.with(type).trail(r.nextBoolean())
										.build();

								// Then apply the effect to the meta
								fwm.addEffect(effect);

								// Generate some random power and set it
								fwm.setPower(r.nextInt(2) + 1);

								// Then apply this to our rocket
								fw.setFireworkMeta(fwm);
							}
						}, 0L, 8L);

		final PlayMatch playMatch = this;
		Bukkit.getScheduler().scheduleSyncDelayedTask(Murder.getInstance(),
				new Runnable() {
					@Override
					public void run() {
						MLogger.log(Level.INFO, "Match " + playMatch.hashCode()
								+ " swiches arena.");
						switchArena();

						Bukkit.getScheduler().cancelTask(fireworkTask);

						// Reconnect each player to the match
						isReloading = true;
						for (MPlayer mPlayer : playMatch.getMPlayers()) {
							playMatch.onPlayerQuit(mPlayer);
							playMatch.onPlayerJoin(mPlayer);
						}
						isReloading = false;
					}
				}, 20 * MATCHEND_TIME);
	}

	private void checkForEnd() {
		if (!isPlaying)
			return;

		MPlayer mMurderer = null;
		int innocentCount = 0;
		for (MPlayer mPlayer : getMPlayers()) {
			if (mPlayer.getPlayerClass() == MPlayerClass.MURDERER) {
				mMurderer = mPlayer;
			} else if (mPlayer.getPlayerClass() == MPlayerClass.INNOCENT
					|| mPlayer.getPlayerClass() == MPlayerClass.GUNNER) {
				innocentCount++;
			}
		}
		if (mMurderer == null) {
			sendMessage(ChatContext.COLOR_INNOCENT
					+ "The Innocent" + ChatContext.COLOR_HIGHLIGHT
					+ " win the match!");
			// If someone killed the murderer
			if (murdererKiller != null && !"".equalsIgnoreCase(murdererKiller)) {
				sendMessage(ChatContext.COLOR_MURDERER + "The Murderer"
						+ ChatContext.COLOR_LOWLIGHT + ", "
						+ ChatContext.COLOR_HIGHLIGHT + murderer
						+ ChatContext.COLOR_LOWLIGHT + ", was killed by "
						+ ChatContext.COLOR_INNOCENT + murdererKiller
						+ ChatContext.COLOR_LOWLIGHT + "!");
			}
			for (MPlayer p : getMPlayers()) {
				// Give surviving innocent money.
				if ((p.getPlayerClass() == MPlayerClass.INNOCENT || p
						.getPlayerClass() == MPlayerClass.GUNNER) && isRanked) {
					int scrapCount = MPlayerClass.getGunPartCount(p.getPlayer()
							.getInventory());
					if (scrapCount > 0) {
						MPlayer.addCoins(p.getName(), scrapCount * 3, true);
					}
					
					MPlayer.addCoins(p.getName(),
							Murder.COINS_INNOCENT_SURVIVE, true);
					if (p.getPlayerClass() == MPlayerClass.GUNNER) {
						MPlayer.addCoins(p.getName(), 3*5, true);
					}
				}
			}
			end(false);
			return;
		}
		if (innocentCount <= 0) {
			sendMessage(ChatContext.COLOR_MURDERER + "The Murderer"
					+ ChatContext.COLOR_LOWLIGHT + ", "
					+ ChatContext.COLOR_HIGHLIGHT + murderer
					+ ChatContext.COLOR_LOWLIGHT + ", won the match!");
			// Reward the murderer for winning
			if (isRanked)
				MPlayer.addCoins(murderer, Murder.COINS_MURDERER_WIN, true);

			end(true);
			return;
		}
	}

	@Override
	public void onPlayerJoin(MPlayer mPlayer) {
		if (!isReloading)
			sendMessage(mPlayer.getPlayer().getDisplayName() + ChatColor.WHITE
					+ " joined your match.", mPlayer.getPlayer());

		Player pPlayer = mPlayer.getPlayer();
		mPlayer.switchPlayerClass(isPlaying ? MPlayerClass.SPECTATOR
				: MPlayerClass.PREGAMEMAN);

		mPlayer.usedRetrieval = false;

		if (arena == null) {
			switchArena();
		}

		Spawn spawn = arena.getRandomSpawn("player");
		if (spawn == null) {
			throw new NullPointerException("No Spawn");
		}
		Location spawnLocation = spawn.getLocation();
		if (spawnLocation == null) {
			throw new NullPointerException("No Spawn Location");
		}
		pPlayer.teleport(spawnLocation);
		pPlayer.setFireTicks(0);
		// Give player slowness, to prevent falling out of the
		// world, etc, after teleporting
		pPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2, 10),
				true);
	}

	@Override
	public void onPlayerQuit(MPlayer mPlayer) {
		// Remove all this players entries from the list of ticket users
		murdererTicketUsers.removeAll(Collections.singleton(mPlayer));
		gunnerTicketUsers.removeAll(Collections.singleton(mPlayer));
		if (isPlaying && mPlayer.getName().equals(murderer)) {
			sendMessage(ChatContext.COLOR_MURDERER + "The Murderer"
					+ ChatContext.COLOR_LOWLIGHT + ", "
					+ ChatContext.COLOR_HIGHLIGHT + murderer
					+ ChatContext.COLOR_LOWLIGHT + ", left the game.");
			end(false);
			return;
		}
		if (!isReloading)
			sendMessage(mPlayer.getPlayer().getDisplayName() + ChatColor.WHITE
					+ " left your match.", mPlayer.getPlayer());
		checkForEnd();
	}

	@Override
	public boolean onPlayerInteractItem(ItemStack itemStack, MPlayer mPlayer) {
		// Handle default actions
		return super.onPlayerInteractItem(itemStack, mPlayer);
	}

	@Override
	public void onPlayerDeath(MPlayer mKilled) {
		Player pKilled = mKilled.getPlayer();

		pKilled.playSound(pKilled.getLocation(), Sound.HURT_FLESH, 1.5f, 1);
		pKilled.setVelocity(new Vector(0, 2, 0));

		String killer = mKilled.getKillerName();

		// If player who died was a gunner
		if (mKilled.getPlayerClass() == MPlayerClass.GUNNER) {
			// Drop a gun
			pKilled.getWorld().dropItem(pKilled.getLocation(),
					new ItemStack(MPlayerClass.MATERIAL_GUN));
		}

		if (isRanked()) {
			// If the murderer was killed
			if (mKilled.getPlayerClass() == MPlayerClass.MURDERER) {
				murdererKiller = killer;
				// If there was a killer, reward them
				if (killer != null && !"".equalsIgnoreCase(killer) && isRanked) {
					MPlayer.addCoins(killer, Murder.COINS_INNOCENT_KILL, true);
				}
			// If an innocent died
			} else if (mKilled.getPlayerClass() == MPlayerClass.INNOCENT
					|| mKilled.getPlayerClass() == MPlayerClass.GUNNER) {
				int scrapCount = MPlayerClass.getGunPartCount(mKilled
						.getPlayer().getInventory());
				for (int i = 0; i < scrapCount; i++) {
					spawnScrap();
				}
				if (scrapCount > 0) {
					MPlayer.addCoins(mKilled.getName(), scrapCount * 5, true);
				}
				// Award the murderer coins for kills, + those by deception,
				// but only if they're not dead.
				if (PlayerManager.getMPlayer(murderer).getPlayerClass() != MPlayerClass.SPECTATOR) {
					MPlayer.addCoins(murderer, Murder.COINS_MURDERER_KILL, true);
				}
			}
		}

		// Change class into a spectator and check if the match is over
		mKilled.switchPlayerClass(MPlayerClass.SPECTATOR);
		pKilled.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,
				40, 1), true);
		checkForEnd();
	}

	public void addMurdererTicketUser(MPlayer mPlayer) {
		murdererTicketUsers.add(mPlayer);
	}

	public void addGunnerTicketUser(MPlayer mPlayer) {
		gunnerTicketUsers.add(mPlayer);
	}
}