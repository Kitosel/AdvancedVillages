package pl.kiosel.villages.addons.diplomacy;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public final class VillageAlliance {

	private final UUID id;
	private final UUID firstVillageId;
	private final UUID secondVillageId;
	private final Instant createdAt;

	public VillageAlliance(UUID id, UUID firstVillageId, UUID secondVillageId, Instant createdAt) {
		this.id = id == null ? UUID.randomUUID() : id;
		this.firstVillageId = firstVillageId;
		this.secondVillageId = secondVillageId;
		this.createdAt = createdAt == null ? Instant.now() : createdAt;
	}

	public boolean contains(UUID villageId) {
		return this.firstVillageId.equals(villageId) || this.secondVillageId.equals(villageId);
	}

	public UUID getOther(UUID villageId) {
		if (this.firstVillageId.equals(villageId)) {
			return this.secondVillageId;
		}
		if (this.secondVillageId.equals(villageId)) {
			return this.firstVillageId;
		}
		return null;
	}
}
