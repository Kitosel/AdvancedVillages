package pl.kiosel.villages.addons.quests;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import pl.kiosel.rosacore.config.RosaConfig;
import pl.kiosel.rosacore.utils.TimeUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.Settings;

import java.time.DayOfWeek;
import java.util.*;
import java.util.regex.Pattern;

public final class QuestConfiguration {

	private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9_-]{1,48}");

	private final AdvancedVillages plugin;
	private final RosaConfig file;
	private volatile QuestSettings settings;

	public QuestConfiguration(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.file = plugin.getQuestFile();
		this.reload();
	}

	public synchronized void reload() {
		Map<QuestPeriod, List<QuestDefinition>> definitions = new EnumMap<>(QuestPeriod.class);
		definitions.put(QuestPeriod.DAILY, this.readDefinitions("quests.daily", QuestPeriod.DAILY));
		definitions.put(QuestPeriod.WEEKLY, this.readDefinitions("quests.weekly", QuestPeriod.WEEKLY));

		this.settings = new QuestSettings(
				Settings.ADDONS_QUESTS_ENABLE.getBoolean(),
				TimeUtils.readZoneId(this.file.getString("reset.time-zone", "Europe/Warsaw")),
				this.file.getInt("reset.hour", 0),
				this.readResetDay(),
				definitions
		);
	}

	public QuestSettings snapshot() {
		return this.settings;
	}

	private List<QuestDefinition> readDefinitions(String path, QuestPeriod period) {
		ConfigurationSection section = this.file.getConfigurationSection(path);
		if (section == null) {
			return List.of();
		}

		List<QuestDefinition> definitions = new ArrayList<>();
		for (String rawId : section.getKeys(false)) {
			String id = rawId.toLowerCase(Locale.ROOT);
			if (!ID_PATTERN.matcher(id).matches()) {
				this.warn("Ignoring quest '" + rawId + "': id must match " + ID_PATTERN.pattern());
				continue;
			}
			ConfigurationSection quest = section.getConfigurationSection(rawId);
			if (quest == null || !quest.getBoolean("enabled", true)) {
				continue;
			}

			QuestType type = this.readType(quest.getString("type", ""), id);
			if (type == null) {
				continue;
			}
			Set<String> targets = this.readTargets(type, quest.getStringList("targets"), id);

			definitions.add(new QuestDefinition(
					id,
					period,
					type,
					targets,
					Math.max(1, quest.getInt("required", 1)),
					new QuestReward(
							quest.getInt("rewards.bank", 0),
							quest.getInt("rewards.experience", 0),
							quest.getInt("rewards.points", 0)
					)
			));
		}
		return definitions;
	}

	private QuestType readType(String rawType, String questId) {
		try {
			return QuestType.valueOf(rawType.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException exception) {
			this.warn("Ignoring quest '" + questId + "': unknown type '" + rawType + "'");
			return null;
		}
	}

	private Set<String> readTargets(QuestType type, List<String> rawTargets, String questId) {
		Set<String> targets = new LinkedHashSet<>();
		for (String rawTarget : rawTargets) {
			String target = rawTarget.trim().toUpperCase(Locale.ROOT);
			if (target.isEmpty()) {
				continue;
			}
			if (isEntityQuest(type) && !isEntityType(target)) {
				this.warn("Ignoring invalid entity '" + rawTarget + "' in quest '" + questId + "'");
				continue;
			}
			if (isMaterialQuest(type)) {
				Material material = Material.matchMaterial(target);
				if (material == null) {
					this.warn("Ignoring invalid material '" + rawTarget + "' in quest '" + questId + "'");
					continue;
				}
				if ((type == QuestType.BREAK_BLOCK || type == QuestType.PLACE_BLOCK)
						&& !material.isBlock()) {
					this.warn("Ignoring non-block material '" + rawTarget + "' in quest '" + questId + "'");
					continue;
				}
				if (material.isAir()) {
					this.warn("Ignoring air material '" + rawTarget + "' in quest '" + questId + "'");
					continue;
				}
				target = material.name();
			}
			targets.add(target);
		}
		return targets;
	}

	private DayOfWeek readResetDay() {
		String configured = this.file.getString("reset.weekly-day", "MONDAY");
		try {
			return DayOfWeek.valueOf(configured.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException exception) {
			this.warn("Invalid weekly quest reset day '" + configured + "'; using MONDAY");
			return DayOfWeek.MONDAY;
		}
	}

	private void warn(String message) {
		this.plugin.getRosaLogger().warning(message);
	}

	private static boolean isEntityQuest(QuestType type) {
		return type == QuestType.KILL_ENTITY
				|| type == QuestType.BREED_ENTITY
				|| type == QuestType.TAME_ENTITY;
	}

	private static boolean isMaterialQuest(QuestType type) {
		return type == QuestType.BREAK_BLOCK
				|| type == QuestType.PLACE_BLOCK
				|| type == QuestType.COLLECT_ITEM
				|| type == QuestType.CRAFT_ITEM
				|| type == QuestType.FISH_ITEM
				|| type == QuestType.SMELT_ITEM
				|| type == QuestType.ENCHANT_ITEM
				|| type == QuestType.CONSUME_ITEM;
	}

	private static boolean isEntityType(String value) {
		try {
			EntityType.valueOf(value);
			return true;
		} catch (IllegalArgumentException exception) {
			return false;
		}
	}
}
