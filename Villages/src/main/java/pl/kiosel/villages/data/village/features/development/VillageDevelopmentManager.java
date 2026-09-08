package pl.kiosel.villages.data.village.features.development;

import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.features.logs.VillageLogType;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.config.Settings;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.storage.DevelopmentStorage;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class VillageDevelopmentManager {

	private static final int MAX_MEMBER_LIMIT = 27;

	private final AdvancedVillages plugin;
	private final DevelopmentConfiguration configuration;
	private final DevelopmentStorage storage;
	private final Map<UUID, VillageDevelopmentState> states = new ConcurrentHashMap<>();
	private volatile DevelopmentSettings settings;

	public VillageDevelopmentManager(AdvancedVillages plugin, DevelopmentConfiguration configuration) {
		this.plugin = plugin;
		this.configuration = configuration;
		this.storage = new DevelopmentStorage(plugin);
		this.settings = configuration.snapshot();
	}

	public void load() {
		if (!this.plugin.isDev()) return;
		this.states.clear();
		this.storage.load(state -> {
			if (this.plugin.getVillageManager().findByUuid(state.getVillageId()).isPresent()) {
				this.states.put(state.getVillageId(), state);
			}
		});
	}

	public void reload() {
		this.configuration.reload();
		this.settings = this.configuration.snapshot();
	}

	public void save(boolean ignoreNotChanged) {
		if (!this.plugin.isDev()) return;
		for (VillageDevelopmentState state : this.states.values()) {
			if (ignoreNotChanged && !state.wasChanged()) continue;
			try {
				this.storage.save(state);
			} catch (RuntimeException exception) {
				this.plugin.getRosaLogger().log(Level.SEVERE,
						"Could not save development state for village " + state.getVillageId(), exception);
			}
		}
	}

	public void delete(Village village) {
		if (!this.plugin.isDev() || village == null) return;
		this.states.remove(village.getUUID());
		this.storage.delete(village.getUUID());
	}

	public boolean isEnabled() {
		return this.plugin.isDev() && this.settings.isEnabled();
	}

	public Collection<DevelopmentNode> getNodes() {
		return this.isEnabled() ? List.copyOf(this.settings.getNodes().values()) : Collections.emptyList();
	}

	public DevelopmentNode getNode(String nodeId) {
		return this.isEnabled() ? this.settings.getNode(nodeId) : null;
	}

	public boolean isUnlocked(Village village, String nodeId) {
		if (!this.isEnabled() || village == null) return false;
		VillageDevelopmentState state = this.states.get(village.getUUID());
		return state != null && state.isUnlocked(nodeId);
	}

	public boolean requirementsMet(Village village, DevelopmentNode node) {
		VillageDevelopmentState state = this.states.get(village.getUUID());
		if (node.getRequirements().isEmpty()) return true;
		if (state == null) return false;
		for (String requirement : node.getRequirements()) {
			if (!state.isUnlocked(requirement)) return false;
		}
		return true;
	}

	public List<String> getMissingRequirements(Village village, DevelopmentNode node) {
		List<String> missing = new ArrayList<>();
		for (String requirement : node.getRequirements()) {
			if (!this.isUnlocked(village, requirement)) missing.add(requirement);
		}
		return missing;
	}

	public synchronized DevelopmentPurchaseResult unlock(Player actor, Village village, String nodeId) {
		DevelopmentSettings current = this.settings;
		if (!this.isEnabled()) return DevelopmentPurchaseResult.DISABLED;
		DevelopmentNode node = current.getNode(nodeId);
		if (village == null || node == null) return DevelopmentPurchaseResult.NOT_FOUND;

		VillageDevelopmentState state = this.states.computeIfAbsent(village.getUUID(), VillageDevelopmentState::new);
		if (state.isUnlocked(nodeId)) return DevelopmentPurchaseResult.ALREADY_UNLOCKED;
		if (village.getLevel().getLevel() < node.getRequiredVillageLevel()) {
			return DevelopmentPurchaseResult.LEVEL_REQUIRED;
		}
		if (!this.requirementsMet(village, node)) {
			return DevelopmentPurchaseResult.PREREQUISITE_REQUIRED;
		}
		if (village.getBank() < node.getCost()) return DevelopmentPurchaseResult.NOT_ENOUGH_BANK;

		village.removeBank(node.getCost());
		state.unlock(nodeId);
		this.plugin.getLogManager().record(village, VillageLogType.DEVELOPMENT_UNLOCKED, actor,
				"node", nodeId, "cost", node.getCost());
		village.broadcast(this.plugin.getVillageMessages().prefixedText(
				Lang.DEVELOPMENT_UNLOCKED_BROADCAST,
				"player", actor == null ? "" : actor.getName(),
				"node", this.getNodeName(node)
		));
		return DevelopmentPurchaseResult.SUCCESS;
	}

	public String getNodeName(DevelopmentNode node) {
		return this.plugin.getGuiSettings().text(
				"guis.development.nodes." + node.getId() + ".name", node.getId());
	}

	public int getBonus(Village village, DevelopmentBonus bonus) {
		if (village == null || !this.isEnabled()) return 0;
		VillageDevelopmentState state = this.states.get(village.getUUID());
		if (state == null) return 0;
		Set<String> unlocked = state.getUnlocked();
		long total = 0;
		for (String nodeId : unlocked) {
			DevelopmentNode node = this.settings.getNode(nodeId);
			if (node != null) total += node.getBonuses().getOrDefault(bonus, 0);
		}
		return (int) Math.min(10_000, total);
	}

	public int getMaxMembers(Village village) {
		long total = (long) Settings.VILLAGE_MAX_MEMBERS.getInt()
				+ this.getBonus(village, DevelopmentBonus.MAX_MEMBERS);
		return (int) Math.max(1, Math.min(MAX_MEMBER_LIMIT, total));
	}

	public int applyQuestBankReward(Village village, int baseReward) {
		return this.increaseByPercent(baseReward,
				this.getBonus(village, DevelopmentBonus.QUEST_BANK_PERCENT));
	}

	public int applyWarScore(Village village, int baseScore) {
		return this.increaseByPercent(baseScore,
				this.getBonus(village, DevelopmentBonus.WAR_SCORE_PERCENT));
	}

	public Duration applyAttackProtection(Village village, Duration baseDuration) {
		if (baseDuration == null || baseDuration.isZero() || baseDuration.isNegative()) return baseDuration;
		int percent = this.getBonus(village, DevelopmentBonus.ATTACK_PROTECTION_PERCENT);
		try {
			return baseDuration.multipliedBy(100L + percent).dividedBy(100L);
		} catch (ArithmeticException exception) {
			return baseDuration;
		}
	}

	public long applyTeleportDelay(Village village, long baseSeconds) {
		int discount = Math.min(100,
				this.getBonus(village, DevelopmentBonus.TELEPORT_DELAY_REDUCTION_PERCENT));
		return Math.max(0L, Math.round(baseSeconds * ((100.0D - discount) / 100.0D)));
	}

	public double applyEffectCost(Village village, double baseCost) {
		int discount = Math.min(100,
				this.getBonus(village, DevelopmentBonus.EFFECT_COST_DISCOUNT_PERCENT));
		return Math.max(0.0D, baseCost * ((100.0D - discount) / 100.0D));
	}

	private int increaseByPercent(int base, int percent) {
		if (base <= 0) return Math.max(0, base);
		long result = Math.round(base * ((100.0D + percent) / 100.0D));
		return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, result));
	}
}
