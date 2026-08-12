package pl.kiosel.villages.addons.antylogout;

import pl.kiosel.core.configuration.Config;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.settings.Settings;

import java.util.LinkedHashSet;

/** Loads a validated, immutable anti-logout configuration snapshot. */
public final class CombatConfig {

	private static final String DEFAULT_BYPASS_PERMISSION = "advancedvillages.antylogout.bypass";

	private final Config file;
	private volatile CombatSettings settings;

	public CombatConfig(AdvancedVillages plugin) {
		this.file = plugin.getCombatFile();
		this.reload();
	}

	public synchronized void reload() {
		String bypassPermission = this.file.getString("combat-bypass-permission", DEFAULT_BYPASS_PERMISSION);
		if (bypassPermission == null || bypassPermission.trim().isEmpty()) {
			bypassPermission = DEFAULT_BYPASS_PERMISSION;
		}

		this.settings = new CombatSettings(
				Settings.ADDONS_ANTYLOGOUT_ENABLE.getBoolean(),
				this.file.getBoolean("combat-bypass", false),
				bypassPermission.trim(),
				this.file.getBoolean("combat-quit-broadcast", true),
				this.file.getLong("combat-duration", 20L),
				this.file.getBoolean("combat-start-notifications-enabled", true),
				this.file.getBoolean("remove-combat-on-opponent-death", true),
				this.file.getBoolean("combat-from-mobs", true),
				this.file.getBoolean("combat-from-projectiles", true),
				this.file.getBoolean("commands-blocked-during-combat", true),
				new LinkedHashSet<>(this.file.getStringList("combat-command-whitelist")),
				this.file.getDouble("blocked-region-knockback-multiplier", 1.0D),
				new LinkedHashSet<>(this.file.getStringList("blocked-regions"))
		);
	}

	public CombatSettings snapshot() {
		return this.settings;
	}
}
