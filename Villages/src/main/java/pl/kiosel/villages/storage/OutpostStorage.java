package pl.kiosel.villages.storage;

import org.bukkit.Location;
import pl.kiosel.rosacore.database.DatabaseTable;
import pl.kiosel.rosacore.database.DatabaseValues;
import pl.kiosel.rosacore.location.LocationUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.outpost.Outpost;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.level.Level;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Consumer;

public final class OutpostStorage {

	private static final DatabaseTable TABLE = DatabaseTable.named("village_outposts");
	private final AdvancedVillages plugin;

	public OutpostStorage(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	public void load(Consumer<Outpost> consumer) {
		this.plugin.getDataManager().withSession(session ->
				session.queryEach("SELECT * FROM " + session.tableName(TABLE), row -> {
					String id = row.getString("uuid");
					try {
						Village village = this.plugin.getVillageManager()
								.findByUuid(UUID.fromString(row.getString("village_uuid")))
								.orElse(null);
						Level level = this.plugin.getOutpostLevelManager().getLevel(row.getInt("level"));
						Location location = LocationUtils.getLocationFromString(row.getString("location"));
						if (village == null || level == null || location == null) {
							this.plugin.getRosaLogger().warning("Ignoring outpost " + id
									+ " because its outpost, level or world is unavailable");
							return;
						}
						Outpost outpost = new Outpost(
								UUID.fromString(id), village, row.getString("name"), location, level,
								Instant.ofEpochMilli(row.getLong("created_at"))
						);
						outpost.markUnchanged();
						consumer.accept(outpost);
					} catch (RuntimeException exception) {
						this.plugin.getRosaLogger().warning("Ignoring invalid outpost " + id + ": " + exception.getMessage());
					}
				})
		);
	}

	public void save(Outpost outpost) {
		long version = outpost.getChangeVersion();
		this.plugin.getDataManager().getDatabase().upsert(TABLE, DatabaseValues.create()
				.set("uuid", outpost.getUuid())
				.set("village_uuid", outpost.getVillage().getUUID())
				.set("name", outpost.getName())
				.set("location", LocationUtils.convertLocationToString(outpost.getLocation().orElseThrow()))
				.set("level", outpost.getLevel().getLevel())
				.set("created_at", outpost.getBorn()), "uuid");
		outpost.markUnchanged(version);
	}

	public void delete(UUID outpostId) {
		this.plugin.getDataManager().getDatabase().delete(TABLE, "uuid", outpostId);
	}

	public void deleteByVillage(UUID villageId) {
		this.plugin.getDataManager().getDatabase().delete(TABLE, "village_uuid", villageId);
	}

}
