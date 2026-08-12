package pl.kiosel.villages.storage;

import pl.kiosel.dependencies.org.jooq.impl.DSL;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.quests.VillageQuestState;

import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class QuestStorage {

	private final AdvancedVillages plugin;
	private final String table;

	public QuestStorage(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.table = plugin.getDataManager().getTablePrefix() + "village_quests";
	}

	public void load(Consumer<VillageQuestState> consumer) {
		this.plugin.getDataManager().getDatabaseConnector().connectDSL(dsl ->
				dsl.select().from(DSL.table(this.table)).fetch().forEach(record -> {
					String rawVillageId = record.get("village_uuid", String.class);
					try {
						VillageQuestState state = new VillageQuestState(
								UUID.fromString(rawVillageId),
								record.get("daily_period", String.class),
								record.get("weekly_period", String.class),
								parseProgress(record.get("daily_progress", String.class)),
								parseProgress(record.get("weekly_progress", String.class)),
								parseCompleted(record.get("daily_completed", String.class)),
								parseCompleted(record.get("weekly_completed", String.class)),
								parseCompleted(record.get("daily_active", String.class)),
								parseCompleted(record.get("weekly_active", String.class))
						);
						state.markUnchanged();
						consumer.accept(state);
					} catch (RuntimeException exception) {
						this.plugin.getLogger().log(Level.WARNING,
								"Ignoring invalid quest state for village " + rawVillageId, exception);
					}
				})
		);
	}

	public void save(VillageQuestState state) {
		VillageQuestState.StoredState snapshot = state.snapshot();
		this.plugin.getDataManager().getDatabaseConnector().connectDSL(dsl -> {
			boolean exists = dsl.fetchExists(
					dsl.selectOne()
							.from(DSL.table(this.table))
							.where(DSL.field("village_uuid").eq(snapshot.getVillageId().toString()))
			);

			if (exists) {
				dsl.update(DSL.table(this.table))
						.set(DSL.field("daily_period"), snapshot.getDailyPeriodKey())
						.set(DSL.field("weekly_period"), snapshot.getWeeklyPeriodKey())
						.set(DSL.field("daily_progress"), serializeProgress(snapshot.getDailyProgress()))
						.set(DSL.field("weekly_progress"), serializeProgress(snapshot.getWeeklyProgress()))
						.set(DSL.field("daily_completed"), serializeCompleted(snapshot.getDailyCompleted()))
						.set(DSL.field("weekly_completed"), serializeCompleted(snapshot.getWeeklyCompleted()))
						.set(DSL.field("daily_active"), serializeCompleted(snapshot.getDailyActive()))
						.set(DSL.field("weekly_active"), serializeCompleted(snapshot.getWeeklyActive()))
						.where(DSL.field("village_uuid").eq(snapshot.getVillageId().toString()))
						.execute();
			} else {
				dsl.insertInto(DSL.table(this.table))
						.set(DSL.field("village_uuid"), snapshot.getVillageId().toString())
						.set(DSL.field("daily_period"), snapshot.getDailyPeriodKey())
						.set(DSL.field("weekly_period"), snapshot.getWeeklyPeriodKey())
						.set(DSL.field("daily_progress"), serializeProgress(snapshot.getDailyProgress()))
						.set(DSL.field("weekly_progress"), serializeProgress(snapshot.getWeeklyProgress()))
						.set(DSL.field("daily_completed"), serializeCompleted(snapshot.getDailyCompleted()))
						.set(DSL.field("weekly_completed"), serializeCompleted(snapshot.getWeeklyCompleted()))
						.set(DSL.field("daily_active"), serializeCompleted(snapshot.getDailyActive()))
						.set(DSL.field("weekly_active"), serializeCompleted(snapshot.getWeeklyActive()))
						.execute();
			}
		});
		state.markUnchanged(snapshot.getChangeVersion());
	}

	public void delete(UUID villageId) {
		this.plugin.getDataManager().getDatabaseConnector().connectDSL(dsl ->
				dsl.deleteFrom(DSL.table(this.table))
						.where(DSL.field("village_uuid").eq(villageId.toString()))
						.execute()
		);
	}

	private static String serializeProgress(Map<String, Integer> progress) {
		return progress.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.map(entry -> entry.getKey() + "=" + entry.getValue())
				.collect(Collectors.joining(";"));
	}

	private static Map<String, Integer> parseProgress(String serialized) {
		Map<String, Integer> progress = new HashMap<>();
		if (serialized == null || serialized.isBlank()) {
			return progress;
		}
		for (String entry : serialized.split(";")) {
			String[] parts = entry.split("=", 2);
			if (parts.length != 2 || parts[0].isBlank()) {
				continue;
			}
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
		if (serialized == null || serialized.isBlank()) {
			return completed;
		}
		for (String entry : serialized.split(";")) {
			if (!entry.isBlank()) {
				completed.add(entry);
			}
		}
		return completed;
	}
}
