package pl.kiosel.villages.manager.teleport;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import pl.kiosel.rosacore.config.RosaConfig;
import pl.kiosel.rosacore.utils.NumberUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.config.Settings;
import pl.kiosel.villages.config.VillageMessage;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.manager.VillageUtilsManager;

import java.util.*;
import java.util.logging.Level;

import static pl.kiosel.rosacore.utils.ColorUtils.tl;

public final class TeleportManager {

	private static final long MAX_TELEPORT_DELAY_SECONDS = 3600L;
	private static final long MAX_COOLDOWN_SECONDS = Long.MAX_VALUE / 1000L;

	private final AdvancedVillages plugin;
	private final RosaConfig spawnFile;
	private final Map<UUID, TeleportSession> sessions = new HashMap<>();
	private final Map<TeleportType, Map<UUID, Long>> cooldowns = new EnumMap<>(TeleportType.class);
	private final Set<UUID> settingVillageHome = new HashSet<>();

	private long spawnCooldown;
	private long spawnDelay;
	private int spawnCost;
	private boolean spawnCancelOnMove;
	private boolean spawnMessage;
	private boolean spawnTitle;
	private boolean spawnAnimatedTitle;

	public TeleportManager(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.spawnFile = plugin.getSpawnFile();
		for (TeleportType type : TeleportType.values()) {
			this.cooldowns.put(type, new HashMap<>());
		}
		this.reload();
	}

	public void reload() {
		this.spawnCooldown = NumberUtils.clampSeconds(this.spawnFile.getLong("cooldown", 60L), 0L, MAX_COOLDOWN_SECONDS);
		this.spawnDelay = NumberUtils.clampSeconds(this.spawnFile.getLong("delay", 5L), 0L, MAX_TELEPORT_DELAY_SECONDS);
		this.spawnCost = Math.max(0, this.spawnFile.getInt("cost", 5));
		this.spawnCancelOnMove = this.spawnFile.getBoolean("cancel-on-move", true);
		this.spawnMessage = this.spawnFile.getBoolean("messages.message", true);
		this.spawnTitle = this.spawnFile.getBoolean("messages.title", true);
		this.spawnAnimatedTitle = this.spawnFile.getBoolean("messages.animated-title", true);
	}

	public boolean isTeleportTask(Player player) {
		return player != null && this.settingVillageHome.contains(player.getUniqueId());
	}

	public void setTeleportTask(Player player) {
		this.settingVillageHome.add(player.getUniqueId());
	}

	public void removeTeleportTask(Player player) {
		this.settingVillageHome.remove(player.getUniqueId());
	}

	public void sendHoverSet(Player player) {
		if (!this.isTeleportTask(player)) return;
		TextComponent message = new TextComponent(this.plugin.getVillageMessages().get(Lang.TELEPORT_SET).toText());
		message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
				new ComponentBuilder(this.plugin.getVillageMessages().get(Lang.TELEPORT_SET_HOVER).toText()).create()));
		message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
				"/village teleporting6 village7 set9"));
		player.sendMessage(tl("&8——————————————————————————"));
		player.spigot().sendMessage(message);
		player.sendMessage(tl("&8——————————————————————————"));
	}

	public boolean isTeleporting(Player player) {
		return player != null && this.isTeleporting(player.getUniqueId());
	}

	public boolean isTeleporting(UUID playerId) {
		return playerId != null && this.sessions.containsKey(playerId);
	}

	public Optional<TeleportType> getActiveType(UUID playerId) {
		TeleportSession session = this.sessions.get(playerId);
		return session == null ? Optional.empty() : Optional.of(session.getType());
	}

	public boolean shouldCancelOnMove(Player player) {
		TeleportSession session = player == null ? null : this.sessions.get(player.getUniqueId());
		return session != null && session.isCancelOnMove();
	}

	public void setTeleportToVillage(Location location, Village village) {
		village.setHome(location);
	}

	public boolean teleportPlayer(Player player, TeleportType teleportType) {
		switch (teleportType) {
			case SPAWN:
				return teleportPlayerToSpawn(player);
			case VILLAGE:
				return teleportPlayerToVillage(player);
		}
		return false;
	}

	public boolean teleportPlayerToVillage(Player player) {
		UUID playerId = player.getUniqueId();
		if (this.sessions.containsKey(playerId) || !this.checkCooldown(player, TeleportType.VILLAGE)) {
			return false;
		}

		Village village = this.plugin.getUserManager().findByUuid(playerId)
				.map(User::getPresentVillage)
				.orElse(null);
		Location destination = village == null ? null : village.getHome().orElse(null);
		if (village == null || destination == null) {
			this.plugin.getVillageMessages().get(Lang.VILLAGE_NO).sendPrefixed(player);
			return false;
		}

		long delay = NumberUtils.clampSeconds(Settings.TELEPORT_COOLDOWN.getLong(), 0L, MAX_TELEPORT_DELAY_SECONDS);
		delay = this.plugin.getDevelopmentManager().applyTeleportDelay(village, delay);
		long cooldown = NumberUtils.clampSeconds(Settings.TELEPORT_BETWEEN_COOLDOWN.getLong(), 0L, MAX_COOLDOWN_SECONDS);
		TeleportSession session = new TeleportSession(
				TeleportType.VILLAGE,
				destination,
				village,
				delay,
				cooldown,
				Settings.TELEPORT_CANCEL_ON_MOVE.getBoolean(),
				0,
				true,
				true,
				Lang.TELEPORT_TITLE,
				Lang.TELEPORT_SUBTITLE
		);
		return this.startSession(player, session);
	}

	public boolean teleportPlayerToSpawn(Player player) {
		Optional<Location> spawn = this.getSpawn();
		if (spawn.isEmpty()) {
			this.plugin.getVillageMessages().get(Lang.SPAWN_NOT_SET).sendPrefixed(player);
			return false;
		}

		UUID playerId = player.getUniqueId();
		if (this.sessions.containsKey(playerId) || !this.checkCooldown(player, TeleportType.SPAWN)) {
			return false;
		}

		if (this.spawnCost > 0 && (!this.plugin.getEconomy().hasBalance(player, this.spawnCost)
				|| !this.plugin.getEconomy().withdrawBalance(player, this.spawnCost))) {
			double missing = Math.max(0.0D, this.spawnCost - this.plugin.getEconomy().getBalance(player));
			this.plugin.getVillageMessages().get(Lang.NO_MONEY)
					.with("money", missing)
					.sendPrefixed(player);
			return false;
		}

		if (this.spawnCost > 0) {
			this.plugin.getVillageMessages().get(Lang.MONEY_REMOVE)
					.with("money", this.spawnCost)
					.sendPrefixed(player);
		}
		if (this.spawnMessage) {
			this.plugin.getVillageMessages().get(Lang.SPAWN_TELEPORT)
					.with("seconds", this.spawnDelay)
					.with("time", this.spawnDelay)
					.sendPrefixed(player);
		}

		TeleportSession session = new TeleportSession(
				TeleportType.SPAWN,
				spawn.get(),
				null,
				this.spawnDelay,
				this.spawnCooldown,
				this.spawnCancelOnMove,
				this.spawnCost,
				this.spawnTitle,
				this.spawnAnimatedTitle,
				Lang.TELEPORT_TITLE,
				Lang.TELEPORT_SUBTITLE
		);
		if (this.startSession(player, session)) {
			return true;
		}

		int refunded = this.refund(player, this.spawnCost);
		if (refunded > 0) {
			this.sendRefund(player, refunded);
		}
		return false;
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

	public boolean cancelTeleport(Player player) {
		return this.cancelTeleport(player, true) >= 0;
	}

	public int cancelTeleport(Player player, boolean refund) {
		TeleportSession session = this.sessions.remove(player.getUniqueId());
		if (session == null) {
			return -1;
		}

		session.cancelTask();
		this.cooldowns.get(session.getType()).remove(player.getUniqueId());
		player.resetTitle();
		return refund ? this.refund(player, session.getChargedCost()) : 0;
	}

	public void cancelTeleports(TeleportType type, boolean refund) {
		for (Map.Entry<UUID, TeleportSession> entry : new ArrayList<>(this.sessions.entrySet())) {
			if (entry.getValue().getType() != type) {
				continue;
			}
			Player player = Bukkit.getPlayer(entry.getKey());
			if (player != null) {
				this.cancelTeleport(player, refund);
			} else {
				this.removeOfflineSession(entry.getKey(), entry.getValue());
			}
		}
	}

	public void cleanupPlayer(Player player) {
		this.cancelTeleport(player, true);
		UUID playerId = player.getUniqueId();
		this.settingVillageHome.remove(playerId);
		for (Map<UUID, Long> typeCooldowns : this.cooldowns.values()) {
			typeCooldowns.remove(playerId);
		}
	}

	public void shutdown() {
		for (Map.Entry<UUID, TeleportSession> entry : new ArrayList<>(this.sessions.entrySet())) {
			Player player = Bukkit.getPlayer(entry.getKey());
			if (player != null) {
				this.cancelTeleport(player, true);
			} else {
				this.removeOfflineSession(entry.getKey(), entry.getValue());
			}
		}
		this.sessions.clear();
		this.settingVillageHome.clear();
		this.cooldowns.values().forEach(Map::clear);
	}

	private boolean startSession(Player player, TeleportSession session) {
		UUID playerId = player.getUniqueId();
		if (this.sessions.putIfAbsent(playerId, session) != null) {
			return false;
		}

		long now = System.currentTimeMillis();
		long durationMillis = session.getCooldownSeconds() * 1000L;
		long expiresAt = durationMillis > Long.MAX_VALUE - now ? Long.MAX_VALUE : now + durationMillis;
		this.cooldowns.get(session.getType()).put(playerId, expiresAt);
		try {
			BukkitTask task = new BukkitRunnable() {
				private long secondsLeft = session.getDelaySeconds();

				@Override
				public void run() {
					if (sessions.get(playerId) != session) {
						cancel();
						return;
					}
					if (this.secondsLeft <= 0L) {
						cancel();
						finishTeleport(player, session);
						return;
					}

					if (session.shouldShowTitle()
							&& (session.isAnimatedTitle() || this.secondsLeft == session.getDelaySeconds())) {
						sendAnimation(player, session, this.secondsLeft);
					}
					this.secondsLeft--;
				}
			}.runTaskTimer(this.plugin, 0L, 20L);
			session.setTask(task);
			return true;
		} catch (RuntimeException exception) {
			this.sessions.remove(playerId, session);
			this.cooldowns.get(session.getType()).remove(playerId);
			this.plugin.getRosaLogger().log(Level.WARNING,
					"Could not schedule " + session.getType().name().toLowerCase() + " teleport for " + player.getName(),
					exception);
			return false;
		}
	}

	private void finishTeleport(Player player, TeleportSession session) {
		UUID playerId = player.getUniqueId();
		if (!this.sessions.remove(playerId, session)) {
			return;
		}
		player.resetTitle();

		Location destination = this.resolveDestination(player, session);
		if (!player.isOnline() || destination == null || !player.teleport(destination)) {
			this.cooldowns.get(session.getType()).remove(playerId);
			int refunded = this.refund(player, session.getChargedCost());
			if (player.isOnline() && refunded > 0) {
				this.sendRefund(player, refunded);
			}
			if (player.isOnline() && session.getType() == TeleportType.VILLAGE) {
				this.plugin.getVillageMessages().get(Lang.VILLAGE_NO).sendPrefixed(player);
			}
			return;
		}

		if (session.getType() == TeleportType.VILLAGE && session.getVillage() != null) {
			this.plugin.getVillageMessages().get(Lang.TELEPORTED)
					.with("village", session.getVillage().getName())
					.sendPrefixed(player);
		}
	}

	private Location resolveDestination(Player player, TeleportSession session) {
		if (session.getType() != TeleportType.VILLAGE) {
			return session.getDestination();
		}

		Village currentVillage = this.plugin.getUserManager().findByPlayer(player)
				.map(User::getPresentVillage)
				.orElse(null);
		if (currentVillage == null || !currentVillage.equals(session.getVillage())) {
			return null;
		}
		Location currentHome = currentVillage.getHome().orElse(null);
		return currentHome == null ? null : currentHome.clone();
	}

	private void sendAnimation(Player player, TeleportSession session, long secondsLeft) {
		String title = this.formatAnimationLine(session.getTitle(), session.getVillage(), secondsLeft);
		String subtitle = this.formatAnimationLine(session.getSubtitle(), session.getVillage(), secondsLeft);
		this.plugin.getMessenger().title(player, title, subtitle, 2, 20, 2);
	}

	private String formatAnimationLine(Lang key, Village village, long secondsLeft) {
		VillageMessage message = this.plugin.getVillageMessages().get(key)
				.with("time", secondsLeft)
				.with("seconds", secondsLeft);
		String text = message.toText();
		return village == null ? text : VillageUtilsManager.replaceWith(village, text);
	}

	private boolean checkCooldown(Player player, TeleportType type) {
		long remaining = this.getRemainingCooldownSeconds(player.getUniqueId(), type);
		if (remaining <= 0L) {
			return true;
		}
		this.plugin.getVillageMessages().get(Lang.TELEPORT_COOLDOWN)
				.with("time", remaining)
				.with("seconds", remaining)
				.sendPrefixed(player);
		return false;
	}

	private long getRemainingCooldownSeconds(UUID playerId, TeleportType type) {
		Map<UUID, Long> typeCooldowns = this.cooldowns.get(type);
		Long expiresAt = typeCooldowns.get(playerId);
		if (expiresAt == null) {
			return 0L;
		}

		long remainingMillis = expiresAt - System.currentTimeMillis();
		if (remainingMillis <= 0L) {
			typeCooldowns.remove(playerId);
			return 0L;
		}
		return ((remainingMillis - 1L) / 1000L) + 1L;
	}

	private int refund(Player player, int amount) {
		return amount > 0 && this.plugin.getEconomy().deposit(player, amount) ? amount : 0;
	}

	private void sendRefund(Player player, int amount) {
		this.plugin.getVillageMessages().get(Lang.MONEY_ADD)
				.with("money", amount)
				.sendPrefixed(player);
	}

	private void removeOfflineSession(UUID playerId, TeleportSession session) {
		this.sessions.remove(playerId, session);
		session.cancelTask();
		this.cooldowns.get(session.getType()).remove(playerId);
	}

	public enum TeleportType {
		VILLAGE,
		SPAWN
	}
}
