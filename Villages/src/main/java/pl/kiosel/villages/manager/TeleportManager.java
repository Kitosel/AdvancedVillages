package pl.kiosel.villages.manager;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.settings.Settings;

import java.util.*;

import static pl.kiosel.core.utils.ColorUtils.tl;

public class TeleportManager {

	private static final long MAX_TELEPORT_DELAY_SECONDS = 3600L;

	private final Map<UUID, BukkitTask> teleportTasks = new HashMap<>();
	private final Set<UUID> teleportingPlayers = new HashSet<>();
	private final Set<UUID> setTeleport = new HashSet<>();
	private final Map<UUID, Long> cooldowns = new HashMap<>();

	private final AdvancedVillages plugin;

	public TeleportManager(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	public boolean isTeleportTask(Player player) {
		return this.setTeleport.contains(player.getUniqueId());
	}

	public void setTeleportTask(Player player) {
		this.setTeleport.add(player.getUniqueId());
	}

	public void removeTeleportTask(Player player) {
		this.setTeleport.remove(player.getUniqueId());
	}

	public boolean isTeleporting(Player player) {
		return this.teleportingPlayers.contains(player.getUniqueId());
	}

	public void sendHoverSet(Player player) {
		if (!isTeleportTask(player)) return;
		TextComponent teleportMessage = new TextComponent(this.plugin.getLocale().getMessage(Lang.TELEPORT_SET.getPath()).toText());

		teleportMessage.setHoverEvent(new HoverEvent(
				HoverEvent.Action.SHOW_TEXT,
				new ComponentBuilder(this.plugin.getLocale().getMessage(Lang.TELEPORT_SET_HOVER.getPath()).toText()).create()
		));
		teleportMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/village teleporting6 village7 set9"));

		player.sendMessage(tl("&8——————————————————————————"));
		player.spigot().sendMessage(teleportMessage);
		player.sendMessage(tl("&8——————————————————————————"));
	}

	public void setTeleportToVillage(Location location, Village village) {
		village.setHome(location);
	}

	public boolean teleportPlayerToVillage(Player player) {
		UUID playerId = player.getUniqueId();
		if (this.teleportingPlayers.contains(playerId)
				|| (this.plugin.getSpawnManager() != null
				&& this.plugin.getSpawnManager().isTeleporting(playerId))) {
			return false;
		}

		long remainingCooldown = getRemainingCooldownSeconds(playerId);
		if (remainingCooldown > 0L) {
			this.plugin.getLocale().getMessage(Lang.TELEPORT_COOLDOWN.getPath())
					.processPlaceholder("time", remainingCooldown)
					.sendPrefixedMessage(player);
			return false;
		}

		Village village = this.plugin.getUserManager().findByUuid(playerId)
				.map(user -> user.getPresentVillage())
				.orNull();
		if (village == null || !village.hasHome()) {
			this.plugin.getLocale().getMessage(Lang.VILLAGE_NO.getPath()).sendPrefixedMessage(player);
			return false;
		}

		long delaySeconds = Math.min(
				MAX_TELEPORT_DELAY_SECONDS,
				Math.max(0L, Settings.TELEPORT_COOLDOWN.getLong())
		);
		this.cooldowns.put(playerId, System.currentTimeMillis());
		this.teleportingPlayers.add(playerId);

		BukkitTask task = new BukkitRunnable() {
			private long secondsLeft = delaySeconds;

			@Override
			public void run() {
				if (!teleportingPlayers.contains(playerId)) {
					cancel();
					return;
				}
				if (this.secondsLeft <= 0L) {
					cancel();
					finishTeleport(player);
					return;
				}

				player.sendTitle(
						plugin.getLocale().getMessage(Lang.TELEPORT_TITLE.getPath())
								.processPlaceholder("time", this.secondsLeft).toString(),
						plugin.getLocale().getMessage(Lang.TELEPORT_SUBTITLE.getPath())
								.processPlaceholder("time", this.secondsLeft).toString(),
						10, 20, 10
				);
				this.secondsLeft--;
			}
		}.runTaskTimer(this.plugin, 0L, 20L);

		BukkitTask previousTask = this.teleportTasks.put(playerId, task);
		if (previousTask != null && previousTask != task) {
			previousTask.cancel();
		}
		return true;
	}

	public boolean cancelTeleport(Player player) {
		UUID playerId = player.getUniqueId();
		BukkitTask task = this.teleportTasks.remove(playerId);
		boolean wasTeleporting = this.teleportingPlayers.remove(playerId);
		if (task != null) {
			task.cancel();
		}
		if (task == null && !wasTeleporting) {
			return false;
		}

		this.cooldowns.remove(playerId);
		player.resetTitle();
		return true;
	}

	public void cleanupPlayer(Player player) {
		cancelTeleport(player);
		UUID playerId = player.getUniqueId();
		this.cooldowns.remove(playerId);
		this.setTeleport.remove(playerId);
	}

	public void shutdown() {
		for (BukkitTask task : this.teleportTasks.values()) {
			task.cancel();
		}
		this.teleportTasks.clear();
		this.teleportingPlayers.clear();
		this.setTeleport.clear();
		this.cooldowns.clear();
	}

	private long getRemainingCooldownSeconds(UUID playerId) {
		Long startedAt = this.cooldowns.get(playerId);
		if (startedAt == null) {
			return 0L;
		}

		long cooldownSeconds = Math.min(
				Long.MAX_VALUE / 1000L,
				Math.max(0L, Settings.TELEPORT_BETWEEN_COOLDOWN.getLong())
		);
		long remainingMillis = cooldownSeconds * 1000L - (System.currentTimeMillis() - startedAt);
		if (remainingMillis <= 0L) {
			this.cooldowns.remove(playerId);
			return 0L;
		}
		return ((remainingMillis - 1L) / 1000L) + 1L;
	}

	private void finishTeleport(Player player) {
		UUID playerId = player.getUniqueId();
		if (!this.teleportingPlayers.remove(playerId)) {
			return;
		}
		this.teleportTasks.remove(playerId);
		player.resetTitle();

		Village village = this.plugin.getUserManager().findByPlayer(player)
				.map(user -> user.getPresentVillage())
				.orNull();
		if (!player.isOnline() || village == null || !village.hasHome()) {
			this.cooldowns.remove(playerId);
			if (player.isOnline()) {
				this.plugin.getLocale().getMessage(Lang.VILLAGE_NO.getPath()).sendPrefixedMessage(player);
			}
			return;
		}

		village.teleportHome(player);
		this.plugin.getLocale().getMessage(Lang.TELEPORTED.getPath())
				.processPlaceholder("village", village.getName())
				.sendPrefixedMessage(player);
	}
}
