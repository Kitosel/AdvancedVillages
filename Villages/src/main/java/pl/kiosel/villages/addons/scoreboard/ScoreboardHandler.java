package pl.kiosel.villages.addons.scoreboard;

import org.bukkit.configuration.ConfigurationSection;
import pl.kiosel.rosacore.config.RosaConfig;
import pl.kiosel.villages.AdvancedVillages;

import java.util.*;

public class ScoreboardHandler {

	private final RosaConfig scoreboardConfig;
	private final AdvancedVillages plugin;

	public ScoreboardHandler(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.scoreboardConfig = plugin.getScoreboardFile();
	}

	ScoreboardSnapshot snapshot() {
		Map<String, List<String>> handlers = readHandlers();
		Map<String, ScoreboardSnapshot.ConditionalPlaceholder> placeholders = readPlaceholders();
		for (String identifier : placeholders.keySet()) {
			if (handlers.remove(identifier) != null) {
				this.plugin.getRosaLogger().warning("Scoreboard identifier '" + identifier
						+ "' is both a placeholder and handler; the placeholder takes priority");
			}
		}

		List<String> frames = new ArrayList<>();
		if (scoreboardAnimationEnabled()) {
			for (String title : getTitles()) {
				if (title != null && !title.isEmpty()) frames.add(title);
			}
		}
		if (frames.isEmpty()) frames.add(scoreboardTitle());

		long animationInterval = Math.max(1L, getAnimationSpeed()) * 20L;
		long refreshInterval = Math.max(1L, Math.min(1200L, refreshInterval()));
		return new ScoreboardSnapshot(this.plugin, getScore(), handlers, placeholders,
				frames, animationInterval, refreshInterval, hideNumberEnabled());
	}

	public boolean scoreboardAnimationEnabled() {
		return this.scoreboardConfig.getBoolean("animation.enabled", false);
	}

	public boolean hideNumberEnabled() {
		return this.scoreboardConfig.getBoolean("hide-numbers", true);
	}

	public int refreshInterval() {
		return this.scoreboardConfig.getInt("refresh-interval-ticks", 40);
	}

	public String scoreboardTitle() {
		return this.scoreboardConfig.getString("title", "&6&lADVANCED&f&lVILLAGES");
	}

	public String scoreboardNoVillage() {
		return this.scoreboardConfig.getString("no-village", "&cNone");
	}

	public int getAnimationSpeed() {
		return Math.max(1, this.scoreboardConfig.getInt("animation.speed", 2));
	}

	public List<String> getScore() {
		return this.scoreboardConfig.getStringList("score");
	}

	public List<String> getTitles() {
		return this.scoreboardConfig.getStringList("animation.titles");
	}

	private Map<String, List<String>> readHandlers() {
		ConfigurationSection section = this.scoreboardConfig.getConfigurationSection("handlers");
		if (section == null) return new LinkedHashMap<>();

		Map<String, List<String>> handlers = new LinkedHashMap<>();
		for (String configuredIdentifier : section.getKeys(false)) {
			String identifier = normalizeIdentifier(configuredIdentifier, "handler");
			if (identifier == null) continue;

			List<String> lines;
			if (section.isList(configuredIdentifier)) {
				lines = section.getStringList(configuredIdentifier);
			} else if (section.isString(configuredIdentifier)) {
				lines = Collections.singletonList(section.getString(configuredIdentifier, ""));
			} else {
				this.plugin.getRosaLogger().warning("Ignoring scoreboard handler '" + configuredIdentifier
						+ "'; its value must be text or a list of lines");
				continue;
			}
			handlers.put(identifier, new ArrayList<>(lines));
		}
		return handlers;
	}

	private Map<String, ScoreboardSnapshot.ConditionalPlaceholder> readPlaceholders() {
		ConfigurationSection section = this.scoreboardConfig.getConfigurationSection("placeholders");
		if (section == null) return Collections.emptyMap();

		Map<String, ScoreboardSnapshot.ConditionalPlaceholder> placeholders = new LinkedHashMap<>();
		for (String configuredIdentifier : section.getKeys(false)) {
			String identifier = normalizeIdentifier(configuredIdentifier, "placeholder");
			if (identifier == null) continue;

			ConfigurationSection definition = section.getConfigurationSection(configuredIdentifier);
			if (definition == null) {
				this.plugin.getRosaLogger().warning("Ignoring incomplete scoreboard placeholder '"
						+ configuredIdentifier + "'");
				continue;
			}

			String condition = definition.getString("condition");
			String positive = definition.getString("true");
			String negative = definition.getString("false");
			if (condition == null || positive == null || negative == null) {
				this.plugin.getRosaLogger().warning("Ignoring incomplete scoreboard placeholder '"
						+ configuredIdentifier + "'; condition, true and false are required");
				continue;
			}

			try {
				placeholders.put(identifier, new ScoreboardSnapshot.ConditionalPlaceholder(ScoreboardCondition.compile(condition), positive, negative));
			} catch (IllegalArgumentException exception) {
				this.plugin.getRosaLogger().warning("Ignoring scoreboard placeholder '" + configuredIdentifier
						+ "': " + exception.getMessage());
			}
		}
		return placeholders;
	}

	private String normalizeIdentifier(String identifier, String type) {
		String normalized = identifier == null ? "" : identifier.trim().toLowerCase(Locale.ROOT);
		if (!normalized.matches("[a-z0-9_.-]+")) {
			this.plugin.getRosaLogger().warning("Ignoring scoreboard " + type + " with invalid identifier '"
					+ identifier + "'");
			return null;
		}
		return normalized;
	}
}
