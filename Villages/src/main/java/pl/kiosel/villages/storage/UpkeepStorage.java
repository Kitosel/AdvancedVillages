package pl.kiosel.villages.storage;

import pl.kiosel.rosacore.database.DatabaseTable;
import pl.kiosel.rosacore.database.DatabaseValues;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.upkeep.VillageUpkeepState;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

public final class UpkeepStorage {
	private static final DatabaseTable TABLE = DatabaseTable.named("village_upkeep");

	private final AdvancedVillages plugin;

	public UpkeepStorage(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	public void load(Consumer<VillageUpkeepState> consumer) {
		this.plugin.getDataManager().withSession(session ->
				session.queryEach("SELECT * FROM " + session.tableName(TABLE), row -> {
					String villageId = row.getString("village_uuid");
					try {
						consumer.accept(new VillageUpkeepState(UUID.fromString(villageId),
								Instant.ofEpochMilli(row.getLong("next_payment")),
								row.getInt("missed_payments")));
					} catch (RuntimeException exception) {
						this.plugin.getRosaLogger().log(Level.WARNING,
								"Ignoring invalid upkeep state for village " + villageId, exception);
					}
				})
		);
	}

	public void save(VillageUpkeepState state) {
		VillageUpkeepState.Snapshot snapshot = state.snapshot();
		this.plugin.getDataManager().getDatabase().upsert(TABLE, DatabaseValues.create()
				.set("village_uuid", snapshot.getVillageId())
				.set("next_payment", snapshot.getNextPayment())
				.set("missed_payments", snapshot.getMissedPayments()), "village_uuid");
		state.markUnchanged(snapshot.getVersion());
	}

	public void delete(UUID villageId) {
		this.plugin.getDataManager().getDatabase().delete(TABLE, "village_uuid", villageId);
	}
}
