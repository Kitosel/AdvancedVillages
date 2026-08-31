package pl.kiosel.villages.storage;

import pl.kiosel.rosacore.database.DatabaseTable;
import pl.kiosel.rosacore.database.DatabaseValues;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.quests.VillageQuestState;

import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class QuestStorage {

	private static final DatabaseTable TABLE = DatabaseTable.named("village_quests");

	private final AdvancedVillages plugin;

	public QuestStorage(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	public void load(Consumer<VillageQuestState> consumer) {
		this.plugin.getDataManager().withSession(session ->
				session.queryEach("SELECT * FROM " + session.tableName(TABLE), row -> {
					String rawVillageId = row.getString("village_uuid");
					try {
						VillageQuestState state = new VillageQuestState(
								UUID.fromString(rawVillageId),
								row.getString("daily_period"),
								row.getString("weekly_period"),
								parseProgress(row.getString("daily_progress")),
								parseProgress(row.getString("weekly_progress")),
								parseCompleted(row.getString("daily_completed")),
								parseCompleted(row.getString("weekly_completed")),
								parseCompleted(row.getString("daily_active")),
								parseCompleted(row.getString("weekly_active"))
						);
						state.markUnchanged();
						consumer.accept(state);
					} catch (RuntimeException exception) {
						this.plugin.getRosaLogger().log(Level.WARNING,
								"Ignoring invalid quest state for village " + rawVillageId, exception);
					}
				})
		);
	}

	public void save(VillageQuestState state) {
		VillageQuestState.StoredState snapshot = state.snapshot();
		this.plugin.getDataManager().getDatabase().upsert(TABLE, DatabaseValues.create()
				.set("village_uuid", snapshot.getVillageId())
				.set("daily_period", snapshot.getDailyPeriodKey())
				.set("weekly_period", snapshot.getWeeklyPeriodKey())
				.set("daily_progress", serializeProgress(snapshot.getDailyProgress()))
				.set("weekly_progress", serializeProgress(snapshot.getWeeklyProgress()))
				.set("daily_completed", serializeCompleted(snapshot.getDailyCompleted()))
				.set("weekly_completed", serializeCompleted(snapshot.getWeeklyCompleted()))
				.set("daily_active", serializeCompleted(snapshot.getDailyActive()))
				.set("weekly_active", serializeCompleted(snapshot.getWeeklyActive())), "village_uuid");
		state.markUnchanged(snapshot.getChangeVersion());
	}

	public void delete(UUID villageId) {
		this.plugin.getDataManager().getDatabase().delete(TABLE, "village_uuid", villageId);
	}

	private static String serializeProgress(Map<String, Integer> progress) {
		return progress.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.map(entry -> entry.getKey() + "=" + entry.getValue())
				.collect(Collectors.joining(";"));
	}

	private static Map<String, Integer> parseProgress(String serialized) {
		Map<String, Integer> progress = new HashMap<>();
		if (serialized == null || serialized.isBlank()) return progress;
		for (String entry : serialized.split(";")) {
			String[] parts = entry.split("=", 2);
			if (parts.length != 2 || parts[0].isBlank()) continue;
			try {
				progress.put(parts[0], Math.max(0, Integer.parseInt(parts[1])));
			} catch (NumberFormatException ignored) {
				// Ignore only the malformed quest entry; keep the remaining state.
			}
		}
		return progress;
	}

	private static String serializeCompleted(Set<String> completed) {
		return completed.stream().sorted().collect(Collectors.joining(";"));
	}

	private static Set<String> parseCompleted(String serialized) {
		Set<String> completed = new HashSet<>();
		if (serialized == null || serialized.isBlank()) return completed;
		for (String entry : serialized.split(";")) {
			if (!entry.isBlank()) completed.add(entry);
		}
		return completed;
	}
}
