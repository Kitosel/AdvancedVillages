package pl.kiosel.villages.addons.logs;

import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.Permission;
import pl.kiosel.villages.storage.VillageLogStorage;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

/** Records activity without blocking the server thread on database writes. */
public final class VillageLogManager {

	private final AdvancedVillages plugin;
	private final VillageLogConfiguration configuration;
	private final VillageLogStorage storage;
	private final Map<UUID, ConcurrentLinkedDeque<VillageLogEntry>> entries = new ConcurrentHashMap<>();
	private final ConcurrentLinkedQueue<VillageLogEntry> pending = new ConcurrentLinkedQueue<>();
	private volatile VillageLogSettings settings;

	public VillageLogManager(AdvancedVillages plugin, VillageLogConfiguration configuration) {
		this.plugin = plugin;
		this.configuration = configuration;
		this.storage = new VillageLogStorage(plugin);
		this.settings = configuration.snapshot();
	}

	public void load() {
		this.entries.clear();
		this.pending.clear();
		VillageLogSettings current = this.settings;
		long cutoff = Instant.now().minusSeconds(current.getRetentionDays() * 86_400L).toEpochMilli();
		int villageCount = Math.max(1, this.plugin.getVillageManager().countVillage());
		int loadLimit = Math.min(50_000, current.getMaxEntriesPerVillage() * villageCount * 2);
		List<VillageLogEntry> loaded = new ArrayList<>();
		this.storage.load(cutoff, loadLimit, loaded::add);
		loaded.sort(Comparator.comparing(VillageLogEntry::getCreatedAt));
		loaded.forEach(entry -> {
			if (this.plugin.getVillageManager().findByUuid(entry.getVillageId()).isPresent()) {
				this.cache(entry);
			}
		});
	}

	public void reload() {
		this.configuration.reload();
		this.settings = this.configuration.snapshot();
		this.entries.values().forEach(this::trim);
	}

	public boolean isEnabled() {
		return this.settings.isEnabled();
	}

	public ZoneId getZoneId() {
		return this.settings.getZoneId();
	}

	public boolean canView(Player player, Village village) {
		if (player == null || village == null || !this.settings.isEnabled()) {
			return false;
		}
		if (player.hasPermission("villages.admin.logs")) {
			return true;
		}
		User user = this.plugin.getUserManager().findByPlayer(player).orNull();
		Village currentVillage = user == null ? null : user.getPresentVillage();
		return currentVillage != null
				&& currentVillage.getUUID().equals(village.getUUID())
				&& (village.isOwner(user)
				|| this.plugin.getPermissionManager().hasPermission(user, Permission.SETTINGS));
	}

	public void record(Village village, VillageLogType type, Player actor, Object... details) {
		this.record(village, type,
				actor == null ? null : actor.getUniqueId(),
				actor == null ? "" : actor.getName(),
				details);
	}

	public void recordSystem(Village village, VillageLogType type, Object... details) {
		this.record(village, type, null, "", details);
	}

	public void record(Village village, VillageLogType type, UUID actorId,
	                   String actorName, Object... details) {
		if (village == null || type == null || !this.settings.isEnabled()) {
			return;
		}
		VillageLogEntry entry = new VillageLogEntry(
				UUID.randomUUID(), village.getUUID(), type, actorId, actorName,
				Instant.now(), detailMap(details)
		);
		this.cacheOrMerge(entry);
	}

	public List<VillageLogEntry> getEntries(Village village) {
		if (village == null) {
			return Collections.emptyList();
		}
		ConcurrentLinkedDeque<VillageLogEntry> villageEntries = this.entries.get(village.getUUID());
		return villageEntries == null
				? Collections.emptyList()
				: Collections.unmodifiableList(new ArrayList<>(villageEntries));
	}

	public void save() {
		List<VillageLogEntry> batch = new ArrayList<>();
		VillageLogEntry entry;
		while ((entry = this.pending.poll()) != null) {
			batch.add(entry);
		}
		if (batch.isEmpty()) {
			return;
		}
		try {
			VillageLogSettings current = this.settings;
			this.storage.save(batch, current.getMaxEntriesPerVillage(), current.getRetentionDays());
		} catch (RuntimeException exception) {
			this.pending.addAll(batch);
			this.plugin.getLogger().log(Level.SEVERE, "Could not save village activity logs", exception);
		}
	}

	public void delete(Village village) {
		if (village == null) {
			return;
		}
		UUID villageId = village.getUUID();
		this.entries.remove(villageId);
		this.pending.removeIf(entry -> villageId.equals(entry.getVillageId()));
		this.storage.delete(villageId);
	}

	private void cache(VillageLogEntry entry) {
		ConcurrentLinkedDeque<VillageLogEntry> villageEntries = this.entries.computeIfAbsent(
				entry.getVillageId(), ignored -> new ConcurrentLinkedDeque<>()
		);
		villageEntries.addFirst(entry);
		this.trim(villageEntries);
	}

	private void cacheOrMerge(VillageLogEntry entry) {
		ConcurrentLinkedDeque<VillageLogEntry> villageEntries = this.entries.computeIfAbsent(
				entry.getVillageId(), ignored -> new ConcurrentLinkedDeque<>()
		);
		synchronized (villageEntries) {
			int windowSeconds = this.settings.getAntiSpamWindowSeconds();
			if (windowSeconds > 0) {
				for (VillageLogEntry previous : villageEntries) {
					if (entry.getCreatedAt().isAfter(previous.getCreatedAt().plusSeconds(windowSeconds))) {
						break;
					}
					VillageLogEntry merged = this.merge(previous, entry);
					if (merged != null) {
						villageEntries.remove(previous);
						villageEntries.addFirst(merged);
						this.pending.add(merged);
						return;
					}
				}
			}
			villageEntries.addFirst(entry);
			this.trim(villageEntries);
			this.pending.add(entry);
		}
	}

	private VillageLogEntry merge(VillageLogEntry previous, VillageLogEntry current) {
		if (previous == null
				|| previous.getType() != current.getType()
				|| !Objects.equals(previous.getActorId(), current.getActorId())) {
			return null;
		}

		Map<String, String> details = new LinkedHashMap<>(current.getDetails());
		switch (current.getType()) {
			case BANK_DEPOSIT:
			case BANK_WITHDRAW:
				details.put("amount", Long.toString(safeAdd(
						parseLong(previous.getDetails().get("amount")),
						parseLong(current.getDetails().get("amount"))
				)));
				break;
			case SETTING_CHANGED:
				if (!sameDetail(previous, current, "setting")) {
					return null;
				}
				break;
			case MEMBER_PERMISSION:
				if (!sameDetail(previous, current, "member")
						|| !sameDetail(previous, current, "permission")) {
					return null;
				}
				break;
			default:
				return null;
		}

		details.put("count", Long.toString(safeAdd(
				Math.max(1L, parseLong(previous.getDetails().get("count"))), 1L)));
		return new VillageLogEntry(
				previous.getId(), current.getVillageId(), current.getType(),
				current.getActorId(), current.getActorName(), current.getCreatedAt(), details
		);
	}

	private void trim(ConcurrentLinkedDeque<VillageLogEntry> villageEntries) {
		int maximum = this.settings.getMaxEntriesPerVillage();
		while (villageEntries.size() > maximum) {
			villageEntries.pollLast();
		}
	}

	private static Map<String, String> detailMap(Object... details) {
		if (details == null || details.length == 0) {
			return Collections.emptyMap();
		}
		if (details.length % 2 != 0) {
			throw new IllegalArgumentException("Log details must be provided as name/value pairs");
		}
		Map<String, String> result = new LinkedHashMap<>();
		for (int index = 0; index < details.length; index += 2) {
			String key = String.valueOf(details[index]);
			Object value = details[index + 1];
			result.put(key, value == null ? "" : String.valueOf(value));
		}
		return result;
	}

	private static boolean sameDetail(VillageLogEntry first, VillageLogEntry second, String key) {
		return Objects.equals(first.getDetails().get(key), second.getDetails().get(key));
	}

	private static long parseLong(String value) {
		try {
			return value == null ? 0L : Long.parseLong(value);
		} catch (NumberFormatException ignored) {
			return 0L;
		}
	}

	private static long safeAdd(long first, long second) {
		if (second > 0L && first > Long.MAX_VALUE - second) {
			return Long.MAX_VALUE;
		}
		if (second < 0L && first < Long.MIN_VALUE - second) {
			return Long.MIN_VALUE;
		}
		return first + second;
	}
}
