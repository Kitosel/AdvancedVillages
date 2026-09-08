package pl.kiosel.villages.data.village.features.diplomacy;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public final class AllianceRequest {

	private final UUID senderVillageId;
	private final UUID targetVillageId;
	private final Instant createdAt;
	private final Instant expiresAt;

	public AllianceRequest(UUID senderVillageId, UUID targetVillageId,
	                       Instant createdAt, Instant expiresAt) {
		this.senderVillageId = senderVillageId;
		this.targetVillageId = targetVillageId;
		this.createdAt = createdAt == null ? Instant.now() : createdAt;
		this.expiresAt = expiresAt;
	}

	public boolean isExpired(Instant now) {
		return this.expiresAt == null || !this.expiresAt.isAfter(now);
	}
}
