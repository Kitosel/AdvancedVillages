package pl.kiosel.villages.addons.antylogout;

import pl.kiosel.core.configuration.Config;
import pl.kiosel.villages.AdvancedVillages;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class CombatConfig {

	private final AdvancedVillages plugin;
	private final Config configuration;

	public CombatConfig(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.configuration = plugin.getCombatFile();
	}

	public String getCombatBypassPermission() {
		return this.configuration.getString("combat-bypass-permission");
	}

	public boolean isBypass() {
		return this.configuration.getBoolean("combat-bypass");
	}

	public boolean isBroadcast() {
		return this.configuration.getBoolean("combat-quit-broadcast");
	}

	public List<String> getCombatBroadcastMessage() {
		return this.configuration.getStringList("combat-quit-broadcast-message");
	}

	public long getCombatDuration() {
		return this.configuration.getLong("combat-duration");
	}

	public boolean isCombatStartNotificationsEnabled() {
		return this.configuration.getBoolean("combat-start-notifications-enabled");
	}

	public CombatMessage getCombatStartMessageAttacker() {
		return this.getMessage("combat-start-message-attacker");
	}

	public CombatMessage getCombatStartMessageVictim() {
		return this.getMessage("combat-start-message-victim");
	}

	public CombatMessage getCombatMessage() {
		return this.getMessage("combat-message");
	}

	public Set<CombatMessage> getCombatEndMessages() {
		return this.getMessages("combat-end-message");
	}

	public boolean isRemoveCombatOnOpponentDeath() {
		return this.configuration.getBoolean("remove-combat-on-opponent-death");
	}

	public Set<CombatMessage> getRemoveCombatMessage() {
		return this.getMessages("remove-combat-message");
	}

	public boolean isCombatFromMobs() {
		return this.configuration.getBoolean("combat-from-mobs");
	}

	public boolean isCombatFromProjectiles() {
		return this.configuration.getBoolean("combat-from-projectiles");
	}

	public boolean isCommandsBlockedDuringCombat() {
		return this.configuration.getBoolean("commands-blocked-during-combat");
	}

	public Set<String> getCombatCommandWhitelist() {
		return new HashSet<>(this.configuration.getStringList("combat-command-whitelist"));
	}

	public Set<CombatMessage> getCombatCommandBlockedMessage() {
		return this.getMessages("combat-command-blocked-message");
	}

	public Set<String> getCombatBlockedRegions() {
		return new HashSet<>(this.configuration.getStringList("combat-blocked-regions"));
	}

	public double getCombatBlockedRegionKnockbackMultiplier() {
		return this.configuration.getDouble("combat-blocked-region-knockback-multiplier");
	}

	public Set<CombatMessage> getCombatBlockedRegionEnterMessage() {
		return this.getMessages("combat-blocked-region-enter-message");
	}

	private CombatMessage getMessage(String path) {
		return new CombatMessage(Objects.requireNonNull(this.configuration.getConfigurationSection(path)).getValues(false));
	}

	private Set<CombatMessage> getMessages(String path) {
		List<String> rawMessages = this.configuration.getStringList(path);
		return rawMessages.stream().map(CombatMessage::new).collect(Collectors.toSet());
	}
}
