package pl.kiosel.villages.storage;

import pl.kiosel.rosacore.database.DatabaseTable;
import pl.kiosel.rosacore.database.DatabaseValues;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.diplomacy.AllianceRequest;
import pl.kiosel.villages.addons.diplomacy.VillageAlliance;
import pl.kiosel.villages.addons.diplomacy.VillageWar;
import pl.kiosel.villages.addons.diplomacy.WarEndReason;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public final class DiplomacyStorage {

	private static final DatabaseTable ALLIANCES = DatabaseTable.named("village_alliances");
	private static final DatabaseTable REQUESTS = DatabaseTable.named("village_alliance_requests");
	private static final DatabaseTable WARS = DatabaseTable.named("village_wars");

	private final AdvancedVillages plugin;

	public DiplomacyStorage(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	public LoadedDiplomacy load() {
		List<VillageAlliance> alliances = new ArrayList<>();
		List<AllianceRequest> requests = new ArrayList<>();
		List<VillageWar> wars = new ArrayList<>();
		this.plugin.getDataManager().withSession(session -> {
			session.queryEach("SELECT * FROM " + session.tableName(ALLIANCES), row -> {
				try {
					alliances.add(new VillageAlliance(
							uuid(row.getString("id")), uuid(row.getString("first_uuid")),
							uuid(row.getString("second_uuid")), instant(row.getLongObject("created_at"))));
				} catch (RuntimeException exception) {
					warn("alliance", exception);
				}
			});
			session.queryEach("SELECT * FROM " + session.tableName(REQUESTS), row -> {
				try {
					requests.add(new AllianceRequest(
							uuid(row.getString("id")), uuid(row.getString("sender_uuid")),
							uuid(row.getString("target_uuid")), instant(row.getLongObject("created_at")),
							instant(row.getLongObject("expires_at"))));
				} catch (RuntimeException exception) {
					warn("alliance request", exception);
				}
			});
			session.queryEach("SELECT * FROM " + session.tableName(WARS), row -> {
				try {
					wars.add(new VillageWar(
							uuid(row.getString("id")), uuid(row.getString("attacker_uuid")),
							uuid(row.getString("defender_uuid")), instant(row.getLongObject("declared_at")),
							instant(row.getLongObject("starts_at")), instant(row.getLongObject("scheduled_ends_at")),
							integer(row.getInteger("attacker_score")), integer(row.getInteger("defender_score")),
							nullableInstant(row.getLongObject("ended_at")),
							nullableInstant(row.getLongObject("cooldown_until")),
							nullableUuid(row.getString("winner_uuid")), endReason(row.getString("end_reason"))));
				} catch (RuntimeException exception) {
					warn("war", exception);
				}
			});
		});
		return new LoadedDiplomacy(alliances, requests, wars);
	}

	public void save(Collection<VillageAlliance> alliances, Collection<AllianceRequest> requests,
	                 Collection<VillageWar.Snapshot> wars, Collection<UUID> removedAllianceIds,
	                 Collection<UUID> removedRequestIds, Collection<UUID> removedWarIds) {
		this.plugin.getDataManager().withTransaction(session -> {
			for (UUID id : removedAllianceIds) session.delete(ALLIANCES, "id", id);
			for (UUID id : removedRequestIds) session.delete(REQUESTS, "id", id);
			for (UUID id : removedWarIds) session.delete(WARS, "id", id);

			for (VillageAlliance alliance : alliances) {
				session.upsert(ALLIANCES, DatabaseValues.create()
						.set("id", alliance.getId())
						.set("first_uuid", alliance.getFirstVillageId())
						.set("second_uuid", alliance.getSecondVillageId())
						.set("created_at", alliance.getCreatedAt()), "id");
			}
			for (AllianceRequest request : requests) {
				session.upsert(REQUESTS, DatabaseValues.create()
						.set("id", request.getId())
						.set("sender_uuid", request.getSenderVillageId())
						.set("target_uuid", request.getTargetVillageId())
						.set("created_at", request.getCreatedAt())
						.set("expires_at", request.getExpiresAt()), "id");
			}
			for (VillageWar.Snapshot war : wars) {
				session.upsert(WARS, DatabaseValues.create()
						.set("id", war.getId())
						.set("attacker_uuid", war.getAttackerVillageId())
						.set("defender_uuid", war.getDefenderVillageId())
						.set("declared_at", war.getDeclaredAt())
						.set("starts_at", war.getStartsAt())
						.set("scheduled_ends_at", war.getScheduledEndsAt())
						.set("attacker_score", war.getAttackerScore())
						.set("defender_score", war.getDefenderScore())
						.set("ended_at", war.getEndedAt())
						.set("cooldown_until", war.getCooldownUntil())
						.set("winner_uuid", war.getWinnerVillageId())
						.set("end_reason", war.getEndReason()), "id");
			}
		});
	}

	private void warn(String type, RuntimeException exception) {
		this.plugin.getRosaLogger().log(Level.WARNING, "Ignoring invalid stored diplomacy " + type, exception);
	}

	private static UUID uuid(String value) {
		return UUID.fromString(value);
	}

	private static UUID nullableUuid(String value) {
		return value == null || value.isBlank() ? null : UUID.fromString(value);
	}

	private static Instant instant(Long value) {
		if (value == null) throw new IllegalArgumentException("Required timestamp is missing");
		return Instant.ofEpochMilli(value);
	}

	private static Instant nullableInstant(Long value) {
		return value == null ? null : Instant.ofEpochMilli(value);
	}

	private static Integer integer(Integer value) {
		return value == null ? 0 : value;
	}

	private static WarEndReason endReason(String value) {
		return value == null || value.isBlank() ? null : WarEndReason.valueOf(value);
	}

	public static final class LoadedDiplomacy {
		private final List<VillageAlliance> alliances;
		private final List<AllianceRequest> requests;
		private final List<VillageWar> wars;

		private LoadedDiplomacy(List<VillageAlliance> alliances,
		                        List<AllianceRequest> requests, List<VillageWar> wars) {
			this.alliances = alliances;
			this.requests = requests;
			this.wars = wars;
		}

		public List<VillageAlliance> getAlliances() {
			return this.alliances;
		}

		public List<AllianceRequest> getRequests() {
			return this.requests;
		}

		public List<VillageWar> getWars() {
			return this.wars;
		}
	}
}
