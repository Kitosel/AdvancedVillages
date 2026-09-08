package pl.kiosel.villages.data.village.features.development;

import lombok.Getter;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public final class VillageDevelopmentState {

	@Getter
	private final UUID villageId;
	private final Set<String> unlocked;
	private long changeVersion;
	private long savedVersion;

	public VillageDevelopmentState(UUID villageId) {
		this(villageId, Collections.emptySet());
	}

	public VillageDevelopmentState(UUID villageId, Set<String> unlocked) {
		this.villageId = villageId;
		this.unlocked = new LinkedHashSet<>(unlocked);
	}

	public synchronized boolean isUnlocked(String nodeId) {
		return this.unlocked.contains(nodeId);
	}

	public synchronized boolean unlock(String nodeId) {
		if (!this.unlocked.add(nodeId)) return false;
		this.changeVersion++;
		return true;
	}

	public synchronized Set<String> getUnlocked() {
		return Collections.unmodifiableSet(new LinkedHashSet<>(this.unlocked));
	}

	public synchronized boolean wasChanged() {
		return this.changeVersion != this.savedVersion;
	}

	public synchronized StoredState snapshot() {
		return new StoredState(this.villageId, new LinkedHashSet<>(this.unlocked), this.changeVersion);
	}

	public synchronized void markUnchanged(long version) {
		if (this.changeVersion == version) this.savedVersion = version;
	}

	@Getter
	public static final class StoredState {
		private final UUID villageId;
		private final Set<String> unlocked;
		private final long version;

		private StoredState(UUID villageId, Set<String> unlocked, long version) {
			this.villageId = villageId;
			this.unlocked = Collections.unmodifiableSet(unlocked);
			this.version = version;
		}

	}
}
