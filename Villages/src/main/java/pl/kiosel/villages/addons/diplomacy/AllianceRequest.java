package pl.kiosel.villages.addons.diplomacy;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public final class AllianceRequest {

	private final UUID id;
	private final UUID senderVillageId;
	private final UUID targetVillageId;
	private final Instant createdAt;
	private final Instant expiresAt;

	public AllianceRequest(UUID id, UUID senderVillageId, UUID targetVillageId,
	                       Instant createdAt, Instant expiresAt) {
		this.id = id == null ? UUID.randomUUID() : id;
		this.senderVillageId = senderVillageId;
		this.targetVillageId = targetVillageId;
		this.createdAt = createdAt == null ? Instant.now() : createdAt;
		this.expiresAt = expiresAt;
	}

	public boolean isExpired(Instant now) {
		return this.expiresAt == null || !this.expiresAt.isAfter(now);
	}
}
