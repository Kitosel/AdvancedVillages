package pl.kiosel.villages.storage;

import pl.kiosel.dependencies.org.jooq.Field;
import pl.kiosel.dependencies.org.jooq.Table;
import pl.kiosel.dependencies.org.jooq.impl.DSL;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.logs.VillageLogEntry;
import pl.kiosel.villages.addons.logs.VillageLogType;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;

public final class VillageLogStorage {

	private final AdvancedVillages plugin;
	private final String tableName;

	public VillageLogStorage(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.tableName = plugin.getDataManager().getTablePrefix() + "village_logs";
	}

	public void load(long cutoff, int limit, Consumer<VillageLogEntry> consumer) {
		Table<?> table = DSL.table(this.tableName);
		Field<Long> createdAt = DSL.field("created_at", Long.class);
		this.plugin.getDataManager().getDatabaseConnector().connectDSL(dsl ->
				dsl.select().from(table)
						.where(createdAt.ge(cutoff))
						.orderBy(createdAt.desc())
						.limit(Math.max(1, limit))
						.fetch()
						.forEach(record -> {
							try {
								String actorId = record.get("actor_uuid", String.class);
								consumer.accept(new VillageLogEntry(
										UUID.fromString(record.get("id", String.class)),
										UUID.fromString(record.get("village_uuid", String.class)),
										VillageLogType.valueOf(record.get("type", String.class)),
										actorId == null || actorId.isBlank() ? null : UUID.fromString(actorId),
										record.get("actor_name", String.class),
										Instant.ofEpochMilli(record.get("created_at", Long.class)),
										decodeDetails(record.get("details", String.class))
								));
							} catch (RuntimeException exception) {
								this.plugin.getLogger().log(Level.WARNING,
										"Ignoring invalid village activity log entry", exception);
							}
						})
		);
	}

	public void save(Collection<VillageLogEntry> entries, int maxPerVillage, int retentionDays) {
		if (entries.isEmpty()) {
			return;
		}
		this.plugin.getDataManager().getDatabaseConnector().connectDSL(dsl -> {
			Table<?> table = DSL.table(this.tableName);
			Field<String> id = DSL.field("id", String.class);
			Field<String> villageId = DSL.field("village_uuid", String.class);
			Field<Long> createdAt = DSL.field("created_at", Long.class);
			Set<String> touchedVillages = new LinkedHashSet<>();

			for (VillageLogEntry entry : entries) {
				String entryId = entry.getId().toString();
				String entryVillageId = entry.getVillageId().toString();
				touchedVillages.add(entryVillageId);
				dsl.deleteFrom(table).where(id.eq(entryId)).execute();
				dsl.insertInto(table)
						.set(id, entryId)
						.set(villageId, entryVillageId)
						.set(DSL.field("type", String.class), entry.getType().name())
						.set(DSL.field("actor_uuid", String.class),
								entry.getActorId() == null ? null : entry.getActorId().toString())
						.set(DSL.field("actor_name", String.class), entry.getActorName())
						.set(createdAt, entry.getCreatedAt().toEpochMilli())
						.set(DSL.field("details", String.class), encodeDetails(entry.getDetails()))
						.execute();
			}

			long cutoff = Instant.now().minusSeconds(Math.max(1, retentionDays) * 86_400L).toEpochMilli();
			dsl.deleteFrom(table).where(createdAt.lt(cutoff)).execute();
			for (String touchedVillage : touchedVillages) {
				List<String> keptIds = dsl.select(id).from(table)
						.where(villageId.eq(touchedVillage))
						.orderBy(createdAt.desc(), id.desc())
						.limit(Math.max(1, maxPerVillage))
						.fetch(id);
				if (!keptIds.isEmpty()) {
					dsl.deleteFrom(table)
							.where(villageId.eq(touchedVillage).and(id.notIn(keptIds)))
							.execute();
				}
			}
		});
	}

	public void delete(UUID villageId) {
		this.plugin.getDataManager().getDatabaseConnector().connectDSL(dsl ->
				dsl.deleteFrom(DSL.table(this.tableName))
						.where(DSL.field("village_uuid").eq(villageId.toString()))
						.execute()
		);
	}

	private static String encodeDetails(Map<String, String> details) {
		if (details.isEmpty()) {
			return "";
		}
		Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
		List<String> encoded = new ArrayList<>();
		for (Map.Entry<String, String> entry : details.entrySet()) {
			encoded.add(encode(encoder, entry.getKey()) + ":" + encode(encoder, entry.getValue()));
		}
		return String.join(";", encoded);
	}

	private static Map<String, String> decodeDetails(String serialized) {
		if (serialized == null || serialized.isBlank()) {
			return Collections.emptyMap();
		}
		Base64.Decoder decoder = Base64.getUrlDecoder();
		Map<String, String> details = new LinkedHashMap<>();
		for (String pair : serialized.split(";")) {
			String[] parts = pair.split(":", 2);
			if (parts.length == 2) {
				details.put(decode(decoder, parts[0]), decode(decoder, parts[1]));
			}
		}
		return details;
	}

	private static String encode(Base64.Encoder encoder, String value) {
		return encoder.encodeToString(value.getBytes(StandardCharsets.UTF_8));
	}

	private static String decode(Base64.Decoder decoder, String value) {
		return new String(decoder.decode(value), StandardCharsets.UTF_8);
	}
}
