package pl.kiosel.villages.addons.scoreboard;

import lombok.Getter;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.scoreboard.RosaScoreboard;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ScoreboardSnapshot {

	private static final int EXPANSION_DEPTH_LIMIT = 24;
	private static final Pattern TOKEN = Pattern.compile("%([A-Za-z0-9_.-]+)%");

	private final AdvancedVillages plugin;
	private final List<String> lines;
	private final Map<String, List<String>> handlers;
	private final Map<String, ConditionalPlaceholder> placeholders;
	@Getter
	private final List<String> titleFrames;
	@Getter
	private final long animationIntervalTicks;
	@Getter
	private final long refreshIntervalTicks;
	@Getter
	private final long updateIntervalTicks;
	@Getter
	private final boolean hideNumbers;
	private final Set<String> reportedWarnings = ConcurrentHashMap.newKeySet();

	ScoreboardSnapshot(AdvancedVillages plugin, List<String> lines, Map<String, List<String>> handlers,
					   Map<String, ConditionalPlaceholder> placeholders, List<String> titleFrames,
					   long animationIntervalTicks, long updateIntervalTicks, boolean hideNumbers) {
		this.plugin = Objects.requireNonNull(plugin, "plugin");
		this.lines = List.copyOf(Objects.requireNonNull(lines, "lines"));
		this.handlers = copyHandlers(handlers);
		this.placeholders = Collections.unmodifiableMap(new LinkedHashMap<>(
				Objects.requireNonNull(placeholders, "placeholders")));
		this.titleFrames = List.copyOf(Objects.requireNonNull(titleFrames, "titleFrames"));
		if (this.titleFrames.isEmpty()) {
			throw new IllegalArgumentException("Scoreboard must contain at least one title frame");
		}
		this.animationIntervalTicks = Math.max(1L, animationIntervalTicks);
		this.updateIntervalTicks = updateIntervalTicks;
		this.refreshIntervalTicks = this.titleFrames.size() > 1
				? greatestCommonDivisor(updateIntervalTicks, this.animationIntervalTicks)
				: updateIntervalTicks;
		this.hideNumbers = hideNumbers;
	}

	public List<String> renderLines(Player player, User user, Village village) {
		ScoreboardCondition.Context context = ScoreboardCondition.context(this.plugin, player, user, village);
		List<String> expanded = new ArrayList<>();
		for (String line : this.lines) {
			expandLine(line == null ? "" : line, context, new ArrayDeque<>(), 0, expanded);
			if (expanded.size() > RosaScoreboard.MAX_LINES) break;
		}

		if (expanded.size() <= RosaScoreboard.MAX_LINES) return expanded;
		warnOnce("too-many-lines", "Expanded scoreboard contains more than " + RosaScoreboard.MAX_LINES
				+ " lines; extra lines are hidden");
		return new ArrayList<>(expanded.subList(0, RosaScoreboard.MAX_LINES));
	}

	private void expandLine(String source, ScoreboardCondition.Context context, Deque<String> handlerStack,
	                        int depth, List<String> output) {
		if (output.size() > RosaScoreboard.MAX_LINES) return;
		if (depth > EXPANSION_DEPTH_LIMIT) {
			warnOnce("expansion-depth", "Scoreboard handler expansion exceeded " + EXPANSION_DEPTH_LIMIT
					+ " nested references");
			return;
		}

		String resolved = resolvePlaceholders(source, context, new ArrayDeque<>(), 0);
		Matcher matcher = TOKEN.matcher(resolved);
		while (matcher.find()) {
			String identifier = normalize(matcher.group(1));
			List<String> handler = this.handlers.get(identifier);
			if (handler == null) continue;

			String before = resolved.substring(0, matcher.start());
			String after = resolved.substring(matcher.end());
			if (handlerStack.contains(identifier)) {
				warnOnce("handler-cycle:" + identifier, "Scoreboard handler cycle detected at '%"
						+ identifier + "%'");
				expandLine(before + after, context, handlerStack, depth + 1, output);
				return;
			}

			handlerStack.addLast(identifier);
			for (String handlerLine : handler) {
				expandLine(before + (handlerLine == null ? "" : handlerLine) + after,
						context, handlerStack, depth + 1, output);
				if (output.size() > RosaScoreboard.MAX_LINES) break;
			}
			handlerStack.removeLast();
			return;
		}
		output.add(resolved);
	}

	private String resolvePlaceholders(String source, ScoreboardCondition.Context context,
	                                   Deque<String> placeholderStack, int depth) {
		if (depth > EXPANSION_DEPTH_LIMIT) {
			warnOnce("placeholder-depth", "Scoreboard placeholder expansion exceeded "
					+ EXPANSION_DEPTH_LIMIT + " nested references");
			return "";
		}

		Matcher matcher = TOKEN.matcher(source);
		StringBuilder output = new StringBuilder();
		while (matcher.find()) {
			String identifier = normalize(matcher.group(1));
			ConditionalPlaceholder placeholder = this.placeholders.get(identifier);
			if (placeholder == null) continue;

			String replacement;
			if (placeholderStack.contains(identifier)) {
				warnOnce("placeholder-cycle:" + identifier, "Scoreboard placeholder cycle detected at '%"
						+ identifier + "%'");
				replacement = "";
			} else {
				placeholderStack.addLast(identifier);
				try {
					replacement = resolvePlaceholders(placeholder.select(context), context,
							placeholderStack, depth + 1);
				} catch (RuntimeException exception) {
					warnOnce("placeholder-error:" + identifier, "Could not evaluate scoreboard placeholder '%"
							+ identifier + "%': " + exception.getMessage());
					replacement = placeholder.getFallback();
				}
				placeholderStack.removeLast();
			}
			matcher.appendReplacement(output, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(output);
		return output.toString();
	}

	private void warnOnce(String key, String message) {
		if (this.reportedWarnings.add(key)) this.plugin.getRosaLogger().warning(message);
	}

	private static Map<String, List<String>> copyHandlers(Map<String, List<String>> source) {
		Map<String, List<String>> copy = new LinkedHashMap<>();
		Objects.requireNonNull(source, "handlers").forEach((key, value) ->
				copy.put(key, List.copyOf(value)));
		return Collections.unmodifiableMap(copy);
	}

	private static String normalize(String identifier) {
		return identifier.toLowerCase(Locale.ROOT);
	}

	private static long greatestCommonDivisor(long first, long second) {
		long left = Math.abs(first);
		long right = Math.abs(second);
		while (right != 0L) {
			long remainder = left % right;
			left = right;
			right = remainder;
		}
		return Math.max(1L, left);
	}

	public static final class ConditionalPlaceholder {

		private final ScoreboardCondition condition;
		private final String positive;
		private final String negative;

		ConditionalPlaceholder(ScoreboardCondition condition, String positive, String negative) {
			this.condition = Objects.requireNonNull(condition, "condition");
			this.positive = Objects.requireNonNull(positive, "positive");
			this.negative = Objects.requireNonNull(negative, "negative");
		}

		private String select(ScoreboardCondition.Context context) {
			return this.condition.test(context) ? this.positive : this.negative;
		}

		private String getFallback() {
			return this.negative;
		}
	}
}
