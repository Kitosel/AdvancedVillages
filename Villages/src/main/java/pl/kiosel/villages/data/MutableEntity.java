package pl.kiosel.villages.data;

public interface MutableEntity extends Entity {

	void markChanged();

	void markUnchanged();

	/**
	 * Marks the entity as saved only when it has not changed since the supplied
	 * version was captured.
	 *
	 * @param expectedVersion version captured before persistence started
	 * @return {@code true} when the entity is now clean
	 */
	default boolean markUnchanged(long expectedVersion) {
		if (this.getChangeVersion() != expectedVersion) {
			return false;
		}
		this.markUnchanged();
		return true;
	}

	boolean wasChanged();

	/**
	 * Returns a monotonically increasing version of the entity state.
	 */
	default long getChangeVersion() {
		return this.wasChanged() ? 1L : 0L;
	}

}
