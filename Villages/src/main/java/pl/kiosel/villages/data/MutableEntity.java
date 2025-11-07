package pl.kiosel.villages.data;

public interface MutableEntity extends Entity {

	void markChanged();

	void markUnchanged();

	boolean wasChanged();

}
