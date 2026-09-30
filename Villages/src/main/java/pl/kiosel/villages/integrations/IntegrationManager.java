package pl.kiosel.villages.integrations;

import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
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
		PluginManager pluginManager = plugin.getServer().getPluginManager();
		Plugin aplplugin = pluginManager.getPlugin("AdvancedPlayerList");

		if (pluginManager.isPluginEnabled("AdvancedPlayerList") && aplplugin != null) {
			if (isVersionNewerThan(aplplugin.getDescription().getVersion(), "1.1.0")) {
				try {
					this.tabListIntegration = new AdvancedPlayerListIntegration(plugin);
					this.tabListIntegration.enable();
					plugin.getRosaLogger().info("AdvancedPlayerList integration enabled");
				} catch (RuntimeException exception) {
					this.tabListIntegration = null;
					plugin.getRosaLogger().log(Level.WARNING, "Could not enable AdvancedPlayerList integration", exception);
				}
			} else {
				plugin.getRosaLogger().warning("AdvancedPlayerList version is too old (1.1.1 or lower required/not supported)!");
			}
		}
	}

	private boolean isVersionNewerThan(String currentVersion, String targetVersion) {
		String[] currentParts = currentVersion.split("\\.");
		String[] targetParts = targetVersion.split("\\.");

		int length = Math.max(currentParts.length, targetParts.length);
		for (int i = 0; i < length; i++) {
			int currentPart = i < currentParts.length ? Integer.parseInt(currentParts[i].replaceAll("[^0-9]", "")) : 0;
			int targetPart = i < targetParts.length ? Integer.parseInt(targetParts[i].replaceAll("[^0-9]", "")) : 0;

			if (currentPart > targetPart) return true;
			if (currentPart < targetPart) return false;
		}
		return false;
	}

	public void disable() {
		if (this.tabListIntegration != null)
			this.tabListIntegration.disable();
		this.tabListIntegration = null;
	}
}
