package pl.kiosel.villages.addons.antylogout;

import lombok.Getter;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public final class CombatSettings {

	@Getter
	private final boolean enabled;
	@Getter
	private final boolean bypassEnabled;
	@Getter
	private final String bypassPermission;
	@Getter
	private final boolean quitBroadcastEnabled;
	@Getter
	private final long durationSeconds;
	private final boolean startNotificationsEnabled;
	private final boolean removeOnOpponentDeath;
	@Getter
	private final boolean combatFromMobs;
	@Getter
	private final boolean combatFromProjectiles;
	private final boolean commandsBlocked;
	private final Set<String> commandWhitelist;
	@Getter
	private final double blockedRegionKnockback;
	private final Set<String> blockedRegions;

	public CombatSettings(boolean enabled, boolean bypassEnabled, String bypassPermission,
	                      boolean quitBroadcastEnabled, long durationSeconds,
	                      boolean startNotificationsEnabled, boolean removeOnOpponentDeath,
	                      boolean combatFromMobs, boolean combatFromProjectiles,
	                      boolean commandsBlocked, Set<String> commandWhitelist,
	                      double blockedRegionKnockback, Set<String> blockedRegions) {
		this.enabled = enabled;
		this.bypassEnabled = bypassEnabled;
		this.bypassPermission = bypassPermission;
		this.quitBroadcastEnabled = quitBroadcastEnabled;
		this.durationSeconds = Math.max(1L, durationSeconds);
		this.startNotificationsEnabled = startNotificationsEnabled;
		this.removeOnOpponentDeath = removeOnOpponentDeath;
		this.combatFromMobs = combatFromMobs;
		this.combatFromProjectiles = combatFromProjectiles;
		this.commandsBlocked = commandsBlocked;
		this.commandWhitelist = normalize(commandWhitelist, true);
		this.blockedRegionKnockback = Math.max(0.0D, blockedRegionKnockback);
		this.blockedRegions = normalize(blockedRegions, false);
	}

	public boolean areStartNotificationsEnabled() {
		return this.startNotificationsEnabled;
	}

	public boolean shouldRemoveOnOpponentDeath() {
		return this.removeOnOpponentDeath;
	}

	public boolean areCommandsBlocked() {
		return this.commandsBlocked;
	}

	public boolean isCommandAllowed(String command) {
		String normalized = normalizeCommand(command);
		if (this.commandWhitelist.contains(normalized)) {
			return true;
		}
		int namespaceSeparator = normalized.indexOf(':');
		return namespaceSeparator >= 0
				&& this.commandWhitelist.contains(normalized.substring(namespaceSeparator + 1));
	}

	public boolean isRegionBlocked(String regionId) {
		return regionId != null && this.blockedRegions.contains(regionId.toLowerCase(Locale.ROOT));
	}

	public static String normalizeCommand(String command) {
		String normalized = command == null ? "" : command.trim().toLowerCase(Locale.ROOT);
		while (normalized.startsWith("/")) {
			normalized = normalized.substring(1);
		}
		int argumentSeparator = normalized.indexOf(' ');
		return argumentSeparator < 0 ? normalized : normalized.substring(0, argumentSeparator);
	}

	private static Set<String> normalize(Set<String> values, boolean commands) {
		Set<String> normalized = new LinkedHashSet<>();
		for (String value : values) {
			String entry = commands
					? normalizeCommand(value)
					: value.trim().toLowerCase(Locale.ROOT);
			if (!entry.isEmpty()) {
				normalized.add(entry);
			}
		}
		return Collections.unmodifiableSet(normalized);
	}
}
