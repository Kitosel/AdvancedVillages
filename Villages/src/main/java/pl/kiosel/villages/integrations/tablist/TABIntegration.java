package pl.kiosel.villages.integrations.tablist;

import pl.kiosel.villages.integrations.TabListIntegration;

public class TABIntegration implements TabListIntegration {

	@Override
	public void enable() {

	}

	@Override
	public void disable() {

	}

	@Override
	public void installProfile() {

	}

	@Override
	public String restorePreviousProfile() {
		return "";
	}

	@Override
	public boolean isProfileInstalled() {
		return false;
	}

	@Override
	public boolean isProfileActive() {
		return false;
	}

	@Override
	public String getActiveProfile() {
		return "";
	}
}
