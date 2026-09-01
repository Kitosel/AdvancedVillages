package pl.kiosel.villages.integrations;

public interface PlayerListProfileIntegration {

	void enable();

	void disable();

	void installProfile();

	String restorePreviousProfile();

	boolean isProfileInstalled();

	boolean isProfileActive();

	String getActiveProfile();
}
