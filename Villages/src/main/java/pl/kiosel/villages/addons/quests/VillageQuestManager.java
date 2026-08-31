package pl.kiosel.villages.addons.quests;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.compatibility.ZSound;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.logs.VillageLogType;
import pl.kiosel.villages.api.events.VillageQuestCompleteEvent;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.storage.QuestStorage;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class VillageQuestManager {

	private final AdvancedVillages plugin;
	private final QuestConfiguration configuration;
	private final QuestStorage storage;
	private final Map<UUID, VillageQuestState> states = new ConcurrentHashMap<>();
	private final Set<UUID> questExperienceRecipients = ConcurrentHashMap.newKeySet();
	private volatile QuestSettings settings;

	public VillageQuestManager(AdvancedVillages plugin, QuestConfiguration configuration) {
		this.plugin = plugin;
		this.configuration = configuration;
		this.storage = new QuestStorage(plugin);
		this.settings = configuration.snapshot();
	}

	public void load() {
		this.states.clear();
		this.storage.load(state -> {
			if (this.plugin.getVillageManager().findByUuid(state.getVillageId()).isPresent()) {
				this.states.put(state.getVillageId(), state);
			}
		});
		for (Village village : this.plugin.getVillageManager().getVillagesView()) {
			this.getState(village);
		}
	}

	public void reload() {
		this.configuration.reload();
		this.settings = this.configuration.snapshot();
		for (VillageQuestState state : this.states.values()) {
			this.ensureCurrentPeriods(state);
		}
	}

	public void save(boolean ignoreUnchanged) {
		for (VillageQuestState state : this.states.values()) {
			if (ignoreUnchanged && !state.wasChanged()) {
				continue;
			}
			try {
				this.storage.save(state);
			} catch (RuntimeException exception) {
				this.plugin.getRosaLogger().log(Level.SEVERE,
						"Could not save quests for village " + state.getVillageId(), exception);
			}
		}
	}

	public void delete(Village village) {
		if (village == null) {
			return;
		}
		this.states.remove(village.getUUID());
		this.storage.delete(village.getUUID());
	}

	public boolean isEnabled() {
		return this.settings.isEnabled();
	}

	public boolean isGrantingQuestExperience(Player player) {
		return player != null && this.questExperienceRecipients.contains(player.getUniqueId());
	}

	boolean shouldTrackPlacedBlock(Material material) {
		if (material == null || !material.isBlock()) {
			return false;
		}
		String target = material.name();
		for (QuestDefinition definition : this.settings.getDefinitions(QuestType.BREAK_BLOCK)) {
			if (definition.matches(QuestType.BREAK_BLOCK, target)) {
				return true;
			}
		}
		for (QuestDefinition definition : this.settings.getDefinitions(QuestType.COLLECT_ITEM)) {
			if (definition.matches(QuestType.COLLECT_ITEM, target)) {
				return true;
			}
		}
		return false;
	}

	/** Activates one configured quest for the village until its next period reset. */
	public boolean activate(Village village, QuestDefinition definition) {
		if (village == null || definition == null || !this.settings.isEnabled()
				|| !Bukkit.isPrimaryThread()) {
			return false;
		}
		QuestDefinition configured = this.findDefinition(definition.getPeriod(), definition.getId());
		return configured != null && this.getState(village).activate(configured);
	}

	/** Activates a quest only when the player still belongs to the displayed village. */
	public boolean activate(Player player, Village village, QuestDefinition definition) {
		if (player == null || village == null) {
			return false;
		}
		User user = this.plugin.getUserManager().findByPlayer(player).orElse(null);
		Village currentVillage = user == null ? null : user.getPresentVillage();
		if (currentVillage == null || !currentVillage.getUUID().equals(village.getUUID())) {
			return false;
		}
		return this.activate(currentVillage, definition);
	}

	public void record(Player player, QuestType type, String target, int amount) {
		if (player == null || amount <= 0 || !this.settings.isEnabled()) {
			return;
		}
		if (!Bukkit.isPrimaryThread()) {
			Bukkit.getScheduler().runTask(this.plugin,
					() -> this.record(player, type, target, amount));
			return;
		}
		this.plugin.getUserManager().findByPlayer(player).ifPresent(user -> {
			Village village = user.getPresentVillage();
			if (village != null) {
				this.record(village, type, target, amount);
			}
		});
	}

	/** Allows integrations to advance configured CUSTOM quests. */
	public void record(Village village, QuestType type, String target, int amount) {
		QuestSettings currentSettings = this.settings;
		if (village == null || type == null || amount <= 0 || !currentSettings.isEnabled()) {
			return;
		}
		if (!Bukkit.isPrimaryThread()) {
			Bukkit.getScheduler().runTask(this.plugin,
					() -> this.record(village, type, target, amount));
			return;
		}

		VillageQuestState state = this.getState(village);
		for (QuestDefinition definition : currentSettings.getDefinitions(type)) {
			if (!definition.matches(type, target)) {
				continue;
			}
			VillageQuestState.ProgressUpdate update = state.addProgress(definition, amount);
			if (update.isCompletedNow()) {
				this.grantReward(village, definition);
			}
		}
	}

	public List<VillageQuestView> getViews(Village village) {
		if (village == null) {
			return Collections.emptyList();
		}
		VillageQuestState state = this.getState(village);
		QuestSettings currentSettings = this.settings;
		Map<QuestPeriod, Duration> resetTimes = new EnumMap<>(QuestPeriod.class);
		resetTimes.put(QuestPeriod.DAILY, this.untilReset(QuestPeriod.DAILY));
		resetTimes.put(QuestPeriod.WEEKLY, this.untilReset(QuestPeriod.WEEKLY));

		List<VillageQuestView> views = new ArrayList<>();
		for (QuestPeriod period : QuestPeriod.values()) {
			for (QuestDefinition definition : currentSettings.getDefinitions(period)) {
				views.add(new VillageQuestView(
						definition,
						state.getProgress(definition),
						state.isCompleted(definition),
						state.isActive(definition),
						resetTimes.get(period)
				));
			}
		}
		return Collections.unmodifiableList(views);
	}

	private VillageQuestState getState(Village village) {
		VillageQuestState state = this.states.computeIfAbsent(
				village.getUUID(), VillageQuestState::new
		);
		this.ensureCurrentPeriods(state);
		return state;
	}

	private void ensureCurrentPeriods(VillageQuestState state) {
		ZonedDateTime shifted = this.shiftedNow();
		LocalDate daily = shifted.toLocalDate();
		LocalDate weekly = daily.with(TemporalAdjusters.previousOrSame(this.settings.getWeeklyResetDay()));
		state.ensurePeriods(daily.toString(), weekly.toString());
	}

	private Duration untilReset(QuestPeriod period) {
		QuestSettings currentSettings = this.settings;
		ZonedDateTime now = ZonedDateTime.now(currentSettings.getZoneId());
		ZonedDateTime shifted = now.minusHours(currentSettings.getResetHour());
		LocalDate resetDate;
		if (period == QuestPeriod.DAILY) {
			resetDate = shifted.toLocalDate().plusDays(1L);
		} else {
			resetDate = shifted.toLocalDate()
					.with(TemporalAdjusters.previousOrSame(currentSettings.getWeeklyResetDay()))
					.plusWeeks(1L);
		}
		ZonedDateTime reset = resetDate.atTime(currentSettings.getResetHour(), 0)
				.atZone(currentSettings.getZoneId());
		Duration remaining = Duration.between(now, reset);
		return remaining.isNegative() ? Duration.ZERO : remaining;
	}

	private ZonedDateTime shiftedNow() {
		QuestSettings currentSettings = this.settings;
		return ZonedDateTime.now(currentSettings.getZoneId())
				.minusHours(currentSettings.getResetHour());
	}

	private QuestDefinition findDefinition(QuestPeriod period, String id) {
		for (QuestDefinition definition : this.settings.getDefinitions(period)) {
			if (definition.getId().equals(id)) {
				return definition;
			}
		}
		return null;
	}

	private void grantReward(Village village, QuestDefinition definition) {
		QuestReward reward = definition.getReward();
		int bankReward = this.plugin.getDevelopmentManager()
				.applyQuestBankReward(village, reward.getBank());
		if (bankReward > 0) {
			long updatedBank = (long) village.getBank() + bankReward;
			village.setBank((int) Math.min(Integer.MAX_VALUE, updatedBank));
		}

		for (User member : village.getMembers()) {
			if (reward.getPoints() > 0) {
				long updatedPoints = (long) member.getRank().getPoints() + reward.getPoints();
				member.getRank().setPoints((int) Math.min(Integer.MAX_VALUE, updatedPoints));
			}
			Player player = Bukkit.getPlayer(member.getUUID());
			if (player == null || !player.isOnline()) {
				continue;
			}
			if (reward.getExperience() > 0) {
				this.questExperienceRecipients.add(player.getUniqueId());
				try {
					player.giveExp(reward.getExperience());
				} finally {
					this.questExperienceRecipients.remove(player.getUniqueId());
				}
			}
			this.plugin.getVillageMessages().sendPrefixed(
					player,
					Lang.QUESTS_COMPLETED,
					"quest", this.questName(definition),
					"bank", bankReward,
					"experience", reward.getExperience(),
					"points", reward.getPoints()
			);
			ZSound.ENTITY_PLAYER_LEVELUP.play(player, 0.5f, 1f);
		}
		this.plugin.getLogManager().recordSystem(village, VillageLogType.QUEST_COMPLETED,
				"quest", definition.getId());
		Bukkit.getPluginManager().callEvent(new VillageQuestCompleteEvent(village, definition));
	}

	private String questName(QuestDefinition definition) {
		return this.plugin.getGuiSettings().text(
				"guis.quests.tasks." + definition.getId() + ".name", definition.getId());
	}
}
