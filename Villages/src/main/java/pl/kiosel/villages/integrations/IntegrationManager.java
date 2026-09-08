package pl.kiosel.villages.integrations;

import lombok.Getter;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.integrations.tablist.AdvancedPlayerListIntegration;

import java.util.logging.Level;

public class IntegrationManager {

	private final AdvancedVillages plugin;
	@Getter private TabListIntegration tabListIntegration;

	public IntegrationManager(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	public void enable() {
		if (plugin.getServer().getPluginManager().isPluginEnabled("AdvancedPlayerList")) {
			try {
				this.tabListIntegration = new AdvancedPlayerListIntegration(plugin);
				this.tabListIntegration.enable();
				plugin.getRosaLogger().info("AdvancedPlayerList integration enabled");
			} catch (RuntimeException exception) {
				this.tabListIntegration = null;
				plugin.getRosaLogger().log(Level.WARNING, "Could not enable AdvancedPlayerList integration", exception);
			}
		}
	}

	public void disable() {
		if (this.tabListIntegration != null)
			this.tabListIntegration.disable();
		this.tabListIntegration = null;
	}
}
