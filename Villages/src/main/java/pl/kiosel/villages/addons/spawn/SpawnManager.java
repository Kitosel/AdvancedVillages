package pl.kiosel.villages.addons.spawn;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import pl.kiosel.core.configuration.Config;
import pl.kiosel.villages.AdvancedVillages;

import java.util.*;

public class SpawnManager {

	private static final long MAX_TELEPORT_DELAY_SECONDS = 3600L;

	private final AdvancedVillages plugin;

	private final Config spawnFile;
	private final Map<UUID, BukkitTask> teleportTasks = new HashMap<>();
	private final Set<UUID> teleportingPlayers = new HashSet<>();
	private final Map<UUID, Long> cooldowns = new HashMap<>();
	private final Map<UUID, Integer> chargedCosts = new HashMap<>();

	@Getter private long teleportCooldown;
	@Getter private long teleportDelay;
	@Getter private int cost;
	@Getter private boolean cancelOnMove;
	@Getter private boolean message;
	@Getter private boolean messageTitle;
	@Getter private boolean messageTitleAnimated;

	public SpawnManager(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.spawnFile = plugin.getSpawnFile();
		reload();
	}

	public void reload() {
		this.teleportCooldown = Math.min(
				Long.MAX_VALUE / 1000L,
				Math.max(0L, this.spawnFile.getLong("cooldown", 60L))
		);
		this.teleportDelay = Math.min(
				MAX_TELEPORT_DELAY_SECONDS,
				Math.max(0L, this.spawnFile.getLong("delay", 5L))
		);
		this.cancelOnMove = this.spawnFile.getBoolean("cancel-on-move", true);
		this.cost = Math.max(0, this.spawnFile.getInt("cost", 5));
		this.message = this.spawnFile.getBoolean("messages.message", true);
		this.messageTitle = this.spawnFile.getBoolean("messages.title", true);
		this.messageTitleAnimated = this.spawnFile.getBoolean("messages.animated-title", true);
	}

	public Optional<Location> getSpawn() {
		String worldName = this.spawnFile.getString("spawn.world");
		if (worldName == null || worldName.trim().isEmpty()) {
			return Optional.empty();
		}

		World world = Bukkit.getWorld(worldName);
		if (world == null) {
			return Optional.empty();
		}

		double x = this.spawnFile.getDouble("spawn.x");
		double y = this.spawnFile.getDouble("spawn.y");
		double z = this.spawnFile.getDouble("spawn.z");
		if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
			return Optional.empty();
		}

		float yaw = (float) this.spawnFile.getDouble("spawn.yaw");
		float pitch = (float) this.spawnFile.getDouble("spawn.pitch");
		return Optional.of(new Location(world, x, y, z, yaw, pitch));
	}

	public boolean isSpawnSet() {
		return getSpawn().isPresent();
	}

	public void setSpawn(Location location) {
		World world = location.getWorld();
		if (world == null) {
			throw new IllegalArgumentException("Spawn location must have a world");
		}

		this.spawnFile.set("spawn.world", world.getName());
		this.spawnFile.set("spawn.x", location.getX());
		this.spawnFile.set("spawn.y", location.getY());
		this.spawnFile.set("spawn.z", location.getZ());
		this.spawnFile.set("spawn.yaw", location.getYaw());
		this.spawnFile.set("spawn.pitch", location.getPitch());
		this.spawnFile.save();
	}

	public long getRemainingCooldownSeconds(UUID playerId) {
		Long startedAt = this.cooldowns.get(playerId);
		if (startedAt == null) {
			return 0L;
		}

		long remainingMillis = this.teleportCooldown * 1000L - (System.currentTimeMillis() - startedAt);
		if (remainingMillis <= 0L) {
			this.cooldowns.remove(playerId);
			return 0L;
		}
		return ((remainingMillis - 1L) / 1000L) + 1L;
	}

	public boolean isTeleporting(UUID playerId) {
		return this.teleportingPlayers.contains(playerId);
	}

	public void beginTeleport(Player player, int chargedCost) {
		UUID playerId = player.getUniqueId();
		this.cooldowns.put(playerId, System.currentTimeMillis());
		this.teleportingPlayers.add(playerId);
		this.chargedCosts.put(playerId, Math.max(0, chargedCost));
	}

	public void trackTeleportTask(UUID playerId, BukkitTask task) {
		BukkitTask previousTask = this.teleportTasks.put(playerId, task);
		if (previousTask != null && previousTask != task) {
			previousTask.cancel();
		}
	}

	public int completeTeleport(Player player) {
		UUID playerId = player.getUniqueId();
		this.teleportTasks.remove(playerId);
		this.teleportingPlayers.remove(playerId);
		player.resetTitle();
		Integer chargedCost = this.chargedCosts.remove(playerId);
		return chargedCost == null ? 0 : chargedCost;
	}

	public int cancelTeleport(Player player, boolean refund) {
		UUID playerId = player.getUniqueId();
		BukkitTask task = this.teleportTasks.remove(playerId);
		boolean wasTeleporting = this.teleportingPlayers.remove(playerId);
		Integer chargedCost = this.chargedCosts.remove(playerId);
		if (task == null && !wasTeleporting && chargedCost == null) {
			return 0;
		}

		if (task != null) {
			task.cancel();
		}
		this.cooldowns.remove(playerId);
		player.resetTitle();

		int amount = chargedCost == null ? 0 : chargedCost;
		return refund && amount > 0 && plugin.getEconomy().deposit(player, amount) ? amount : 0;
	}

	public int refundFailedTeleport(Player player, int chargedCost) {
		this.cooldowns.remove(player.getUniqueId());
		return chargedCost > 0 && plugin.getEconomy().deposit(player, chargedCost) ? chargedCost : 0;
	}

	public void cancelAll(boolean refund) {
		for (UUID playerId : new ArrayList<>(this.teleportingPlayers)) {
			Player player = Bukkit.getPlayer(playerId);
			if (player != null) {
				cancelTeleport(player, refund);
				continue;
			}

			BukkitTask task = this.teleportTasks.remove(playerId);
			if (task != null) {
				task.cancel();
			}
			this.teleportingPlayers.remove(playerId);
			this.chargedCosts.remove(playerId);
			this.cooldowns.remove(playerId);
		}
		for (BukkitTask task : this.teleportTasks.values()) {
			task.cancel();
		}
		this.teleportTasks.clear();
		this.teleportingPlayers.clear();
		this.chargedCosts.clear();
		this.cooldowns.clear();
	}
}