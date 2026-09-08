package pl.kiosel.villages.integrations;

public interface TabListIntegration {

	void enable();

	void disable();

	void installProfile();

	String restorePreviousProfile();

	boolean isProfileInstalled();

	boolean isProfileActive();

	String getActiveProfile();
}
