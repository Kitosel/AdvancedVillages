package pl.kiosel.villages.data;

import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.storage.Dataloader;

import java.util.logging.Level;

public class DataSaveTask implements Runnable {

	private final AdvancedVillages plugin;
	private final Dataloader dataModel;
	private final boolean fullSave;

	public DataSaveTask(AdvancedVillages plugin, boolean fullSave) {
		this.plugin = plugin;
		this.dataModel = plugin.getDataloader();
		this.fullSave = fullSave;
	}

	@Override
	public void run() {
		try {
			this.dataModel.save(!this.fullSave);
			plugin.getDebug().debug("Saved data");
		} catch (RuntimeException exception) {
			plugin.getRosaLogger().log(Level.SEVERE, "Automatic data save failed", exception);
		}
	}
}
