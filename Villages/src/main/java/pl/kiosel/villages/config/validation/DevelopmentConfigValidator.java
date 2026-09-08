package pl.kiosel.villages.config.validation;

import org.bukkit.configuration.ConfigurationSection;
import pl.kiosel.rosacore.config.ConfigValidator;
import pl.kiosel.rosacore.config.ConfigView;
import pl.kiosel.rosacore.config.ValidationContext;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.features.development.DevelopmentBonus;
import pl.kiosel.villages.data.village.level.LevelManager;

import java.util.*;
import java.util.regex.Pattern;

public final class DevelopmentConfigValidator implements ConfigValidator {

	private static final Pattern ID = Pattern.compile("[a-z0-9_-]{1,48}");
	private static final Pattern LEVEL = Pattern.compile("level-([1-9][0-9]*)", Pattern.CASE_INSENSITIVE);
	private final AdvancedVillages plugin;

	public DevelopmentConfigValidator(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

    @Override
    public void validate(ConfigView config, ValidationContext context) {
        ConfigChecks.bool(config, "enabled", context);
		ConfigurationSection nodes = ConfigChecks.section(config, "nodes", context);
		if (nodes == null) return;
		int highestLevel = this.highestConfiguredLevel();
		Map<String, Set<String>> requirements = new LinkedHashMap<>();
        Map<String, String> paths = new HashMap<>();
        for (String rawId : nodes.getKeys(false)) {
            String id = rawId.toLowerCase(Locale.ROOT);
            String path = "nodes." + rawId;
            if (!ID.matcher(id).matches()) {
                context.error(path, "Node id must match " + ID.pattern());
                continue;
            }
            if (paths.putIfAbsent(id, path) != null) {
                context.error(path, "Duplicate node id: " + id);
                continue;
            }
            if (ConfigChecks.section(config, path, context) == null) continue;
            ConfigChecks.bool(config, path + ".enabled", context);
            if (!config.getBoolean(path + ".enabled", true)) continue;
            ConfigChecks.number(config, path + ".cost", 0, Integer.MAX_VALUE, true, context);
			ConfigChecks.number(config, path + ".required-village-level", 1, highestLevel, true, context);
            Set<String> required = new LinkedHashSet<>();
            List<?> entries = ConfigChecks.list(config, path + ".requires", context);
            for (int index = 0; index < entries.size(); index++) {
                Object entry = entries.get(index);
                if (!(entry instanceof String text) || text.isBlank()) {
                    context.error(path + ".requires[" + index + "]", "Expected a node id");
                } else {
                    required.add(text.trim().toLowerCase(Locale.ROOT));
                }
            }
            requirements.put(id, required);
            validateBonuses(config, path + ".bonuses", context);
        }

        for (Map.Entry<String, Set<String>> node : requirements.entrySet()) {
            String path = paths.get(node.getKey()) + ".requires";
            for (String required : node.getValue()) {
                if (!requirements.containsKey(required)) {
                    context.error(path, "Missing or disabled prerequisite: " + required);
                }
            }
            if (reaches(node.getKey(), node.getKey(), requirements, new HashSet<>())) {
                context.error(path, "Cyclic prerequisites involving node: " + node.getKey());
            }
        }
    }

    private void validateBonuses(ConfigView config, String path, ValidationContext context) {
        ConfigurationSection bonuses = ConfigChecks.section(config, path, context);
        if (bonuses == null) return;
        for (String key : bonuses.getKeys(false)) {
            DevelopmentBonus bonus = DevelopmentBonus.fromConfigKey(key);
            if (bonus == null) {
                context.error(path + "." + key, "Unknown development bonus: " + key);
                continue;
            }
            int maximum = bonus == DevelopmentBonus.TELEPORT_DELAY_REDUCTION_PERCENT
                    || bonus == DevelopmentBonus.EFFECT_COST_DISCOUNT_PERCENT ? 100 : 10_000;
            ConfigChecks.number(config, path + "." + key, 0, maximum, true, context);
        }
    }

	private boolean reaches(String current, String target, Map<String, Set<String>> nodes, Set<String> visited) {
        if (!visited.add(current)) return false;
        for (String requirement : nodes.getOrDefault(current, Collections.emptySet())) {
            if (requirement.equals(target) || reaches(requirement, target, nodes, visited)) return true;
        }
		return false;
	}

	private int highestConfiguredLevel() {
		if (this.plugin == null) return LevelManager.MAX_LEVEL;
		int highest = 0;
		for (String key : this.plugin.getLevelsFile().getKeys(false)) {
			var matcher = LEVEL.matcher(key);
			if (!matcher.matches()) continue;
			try {
				highest = Math.max(highest, Integer.parseInt(matcher.group(1)));
			} catch (NumberFormatException ignored) {
			}
		}
		return Math.max(1, Math.min(LevelManager.MAX_LEVEL, highest));
	}
}
