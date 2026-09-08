package pl.kiosel.villages.storage;

import pl.kiosel.rosacore.database.DatabaseSession;
import pl.kiosel.rosacore.database.DatabaseTable;
import pl.kiosel.rosacore.database.DatabaseValues;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.features.logs.VillageLogEntry;
import pl.kiosel.villages.data.village.features.logs.VillageLogType;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;

public final class VillageLogStorage {

	private static final DatabaseTable TABLE = DatabaseTable.named("village_logs");

	private final AdvancedVillages plugin;

	public VillageLogStorage(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	public void load(long cutoff, int limit, Consumer<VillageLogEntry> consumer) {
		int safeLimit = Math.max(1, limit);
		this.plugin.getDataManager().withSession(session -> session.queryEach(
				"SELECT * FROM " + session.tableName(TABLE)
						+ " WHERE created_at >= ? ORDER BY created_at DESC LIMIT " + safeLimit,
				row -> {
					try {
						String actorId = row.getString("actor_uuid");
						consumer.accept(new VillageLogEntry(
								UUID.fromString(row.getString("id")),
								UUID.fromString(row.getString("village_uuid")),
								VillageLogType.valueOf(row.getString("type")),
								actorId == null || actorId.isBlank() ? null : UUID.fromString(actorId),
								row.getString("actor_name"),
								Instant.ofEpochMilli(row.getLong("created_at")),
								decodeDetails(row.getString("details"))
						));
					} catch (RuntimeException exception) {
						this.plugin.getRosaLogger().log(Level.WARNING,
								"Ignoring invalid village activity log entry", exception);
					}
				}, cutoff));
	}

	public void save(Collection<VillageLogEntry> entries, int maxPerVillage, int retentionDays) {
		if (entries.isEmpty()) return;
		this.plugin.getDataManager().withTransaction(session -> {
			Set<String> touchedVillages = new LinkedHashSet<>();
			for (VillageLogEntry entry : entries) {
				touchedVillages.add(entry.getVillageId().toString());
				session.upsert(TABLE, DatabaseValues.create()
						.set("id", entry.getId())
						.set("village_uuid", entry.getVillageId())
						.set("type", entry.getType())
						.set("actor_uuid", entry.getActorId())
						.set("actor_name", entry.getActorName())
						.set("created_at", entry.getCreatedAt())
						.set("details", encodeDetails(entry.getDetails())), "id");
			}

			long cutoff = Instant.now().minusSeconds(Math.max(1, retentionDays) * 86_400L).toEpochMilli();
			session.executeUpdate("DELETE FROM " + session.tableName(TABLE) + " WHERE created_at < ?", cutoff);
			int safeLimit = Math.max(1, maxPerVillage);
			for (String villageId : touchedVillages) {
				List<String> keptIds = session.query(
						"SELECT id FROM " + session.tableName(TABLE)
								+ " WHERE village_uuid = ? ORDER BY created_at DESC, id DESC LIMIT " + safeLimit,
						row -> row.getString("id"), villageId);
				deleteExcept(session, villageId, keptIds);
			}
		});
	}

	public void delete(UUID villageId) {
		this.plugin.getDataManager().getDatabase().delete(TABLE, "village_uuid", villageId);
	}

	private static void deleteExcept(DatabaseSession session, String villageId, List<String> keptIds) {
		String sql = "DELETE FROM " + session.tableName(TABLE) + " WHERE village_uuid = ?";
		if (keptIds.isEmpty()) {
			session.executeUpdate(sql, villageId);
			return;
		}

		String placeholders = String.join(", ", Collections.nCopies(keptIds.size(), "?"));
		Object[] parameters = new Object[keptIds.size() + 1];
		parameters[0] = villageId;
		for (int index = 0; index < keptIds.size(); index++) parameters[index + 1] = keptIds.get(index);
		session.executeUpdate(sql + " AND id NOT IN (" + placeholders + ")", parameters);
	}

	private static String encodeDetails(Map<String, String> details) {
		if (details.isEmpty()) return "";
		Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
		List<String> encoded = new ArrayList<>();
		for (Map.Entry<String, String> entry : details.entrySet()) {
			encoded.add(encode(encoder, entry.getKey()) + ":" + encode(encoder, entry.getValue()));
		}
		return String.join(";", encoded);
	}

	private static Map<String, String> decodeDetails(String serialized) {
		if (serialized == null || serialized.isBlank()) return Collections.emptyMap();
		Base64.Decoder decoder = Base64.getUrlDecoder();
		Map<String, String> details = new LinkedHashMap<>();
		for (String pair : serialized.split(";")) {
			String[] parts = pair.split(":", 2);
			if (parts.length == 2) details.put(decode(decoder, parts[0]), decode(decoder, parts[1]));
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
