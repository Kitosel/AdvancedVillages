package pl.kiosel.villages.data;

import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractMutableEntity implements MutableEntity {

	private final AtomicLong changeVersion = new AtomicLong(1L);
	private volatile long persistedVersion;

    @Override
    public void markChanged() {
		this.changeVersion.incrementAndGet();
    }

    @Override
    public void markUnchanged() {
		this.persistedVersion = this.changeVersion.get();
	}

	@Override
	public boolean markUnchanged(long expectedVersion) {
		if (this.changeVersion.get() != expectedVersion) {
			return false;
		}
		this.persistedVersion = expectedVersion;
		return true;
    }

    @Override
    public boolean wasChanged() {
		return this.changeVersion.get() != this.persistedVersion;
	}

	@Override
	public long getChangeVersion() {
		return this.changeVersion.get();
    }

}
