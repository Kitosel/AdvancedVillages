package pl.kiosel.villages.addons.development;

import org.bukkit.configuration.ConfigurationSection;
import pl.kiosel.rosacore.config.RosaConfig;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.Settings;

import java.util.*;
import java.util.regex.Pattern;

public final class DevelopmentConfiguration {

	private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9_-]{1,48}");

	private final AdvancedVillages plugin;
	private final RosaConfig file;
	private volatile DevelopmentSettings settings;

	public DevelopmentConfiguration(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.file = plugin.getDevelopmentFile();
		this.reload();
	}

	public synchronized void reload() {
		Map<String, DevelopmentNode> nodes = this.readNodes();
		this.removeInvalidDependencies(nodes);
		this.settings = new DevelopmentSettings(
				Settings.ADDONS_DEVELOPMENT_ENABLE.getBoolean()
						&& this.file.getBoolean("enabled", true),
				nodes
		);
	}

	public DevelopmentSettings snapshot() {
		return this.settings;
	}

	private Map<String, DevelopmentNode> readNodes() {
		Map<String, DevelopmentNode> nodes = new LinkedHashMap<>();
		ConfigurationSection section = this.file.getConfigurationSection("nodes");
		if (section == null) return nodes;

		for (String rawId : section.getKeys(false)) {
			String id = rawId.toLowerCase(Locale.ROOT);
			if (!ID_PATTERN.matcher(id).matches()) {
				this.warn("Ignoring development node '" + rawId + "': invalid id");
				continue;
			}
			ConfigurationSection node = section.getConfigurationSection(rawId);
			if (node == null || !node.getBoolean("enabled", true)) continue;

			Set<String> requirements = new LinkedHashSet<>();
			for (String requirement : node.getStringList("requires")) {
				String normalized = requirement.trim().toLowerCase(Locale.ROOT);
				if (!normalized.isEmpty() && !normalized.equals(id)) requirements.add(normalized);
			}

			Map<DevelopmentBonus, Integer> bonuses = new EnumMap<>(DevelopmentBonus.class);
			ConfigurationSection bonusSection = node.getConfigurationSection("bonuses");
			if (bonusSection != null) {
				for (String key : bonusSection.getKeys(false)) {
					DevelopmentBonus bonus = DevelopmentBonus.fromConfigKey(key);
					if (bonus == null) {
						this.warn("Ignoring unknown bonus '" + key + "' in development node '" + id + "'");
						continue;
					}
					int value = Math.max(0, Math.min(10_000, bonusSection.getInt(key, 0)));
					if (value > 0) bonuses.put(bonus, value);
				}
			}

			nodes.put(id, new DevelopmentNode(
					id,
					Math.max(0, node.getInt("cost", 0)),
					Math.max(1, Math.min(10, node.getInt("required-village-level", 1))),
					requirements, bonuses
			));
		}
		return nodes;
	}

	private void removeInvalidDependencies(Map<String, DevelopmentNode> nodes) {
		boolean removed;
		do {
			removed = false;
			for (DevelopmentNode node : Set.copyOf(nodes.values())) {
				if (node.getRequirements().stream().anyMatch(required -> !nodes.containsKey(required))) {
					this.warn("Ignoring development node '" + node.getId() + "': missing prerequisite");
					nodes.remove(node.getId());
					removed = true;
				}
			}
		} while (removed);

		for (String id : Set.copyOf(nodes.keySet())) {
			if (this.reaches(id, id, nodes, new HashSet<>())) {
				this.warn("Ignoring development node '" + id + "': cyclic prerequisite");
				nodes.remove(id);
			}
		}
	}

	private boolean reaches(String current, String target, Map<String, DevelopmentNode> nodes,
	                        Set<String> visited) {
		DevelopmentNode node = nodes.get(current);
		if (node == null || !visited.add(current)) return false;
		for (String requirement : node.getRequirements()) {
			if (requirement.equals(target) || this.reaches(requirement, target, nodes, visited)) return true;
		}
		return false;
	}

	private void warn(String message) {
		this.plugin.getRosaLogger().warning(message);
	}
}
