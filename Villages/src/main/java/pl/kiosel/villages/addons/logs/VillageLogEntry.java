package pl.kiosel.villages.addons.logs;

import lombok.Getter;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** One immutable and persistable village activity entry. */
@Getter
public final class VillageLogEntry {

	private final UUID id;
	private final UUID villageId;
	private final VillageLogType type;
	private final UUID actorId;
	private final String actorName;
	private final Instant createdAt;
	private final Map<String, String> details;

	public VillageLogEntry(UUID id, UUID villageId, VillageLogType type,
	                       UUID actorId, String actorName, Instant createdAt,
	                       Map<String, String> details) {
		this.id = Objects.requireNonNull(id, "id");
		this.villageId = Objects.requireNonNull(villageId, "villageId");
		this.type = Objects.requireNonNull(type, "type");
		this.actorId = actorId;
		this.actorName = actorName == null ? "" : actorName;
		this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
		this.details = Collections.unmodifiableMap(new LinkedHashMap<>(details));
	}
}
