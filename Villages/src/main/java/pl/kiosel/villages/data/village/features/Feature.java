package pl.kiosel.villages.data.village.features;

public interface Feature {

	boolean isEnabled();
	void setEnabled(boolean enabled);

	void enable();
	void disable();

}
