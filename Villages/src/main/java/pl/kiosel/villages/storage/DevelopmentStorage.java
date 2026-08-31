package pl.kiosel.villages.storage;

import pl.kiosel.rosacore.database.DatabaseTable;
import pl.kiosel.rosacore.database.DatabaseValues;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.development.VillageDevelopmentState;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class DevelopmentStorage {

	private final AdvancedVillages plugin;
	private static final DatabaseTable TABLE = DatabaseTable.named("village_development");

	public DevelopmentStorage(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	public void load(Consumer<VillageDevelopmentState> consumer) {
		this.plugin.getDataManager().withSession(session ->
				session.queryEach("SELECT * FROM " + session.tableName(TABLE), row -> {
					String villageId = row.getString("village_uuid");
					try {
						consumer.accept(new VillageDevelopmentState(
								UUID.fromString(villageId),
								parse(row.getString("unlocked_nodes"))
						));
					} catch (RuntimeException exception) {
						this.plugin.getRosaLogger().log(Level.WARNING,
								"Ignoring invalid development state for village " + villageId, exception);
					}
				})
		);
	}

	public void save(VillageDevelopmentState state) {
		VillageDevelopmentState.StoredState snapshot = state.snapshot();
		this.plugin.getDataManager().getDatabase().upsert(TABLE, DatabaseValues.create()
				.set("village_uuid", snapshot.getVillageId())
				.set("unlocked_nodes", serialize(snapshot.getUnlocked())), "village_uuid");
		state.markUnchanged(snapshot.getVersion());
	}

	public void delete(UUID villageId) {
		this.plugin.getDataManager().getDatabase().delete(TABLE, "village_uuid", villageId);
	}

	private static String serialize(Set<String> unlocked) {
		return unlocked.stream().sorted().collect(Collectors.joining(";"));
	}

	private static Set<String> parse(String serialized) {
		if (serialized == null || serialized.isBlank()) return new LinkedHashSet<>();
		return Arrays.stream(serialized.split(";"))
				.map(String::trim)
				.filter(value -> !value.isEmpty())
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}
}
