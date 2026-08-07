package pl.kiosel.villages.data;

import pl.kiosel.core.thread.MetaTask;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.storage.Dataloader;

import java.util.logging.Level;

public class DataSaveTask extends MetaTask.AsyncMetaTask {

	private final Dataloader dataModel;
	private final boolean fullSave;

	public DataSaveTask(Dataloader dataModel, boolean fullSave) {
		this.dataModel = dataModel;
		this.fullSave = fullSave;
	}

	@Override
	public void execute() {
		try {
			this.dataModel.save(!this.fullSave);
		} catch (RuntimeException exception) {
			AdvancedVillages plugin = AdvancedVillages.getInstance();
			if (plugin != null) {
				plugin.getLogger().log(Level.SEVERE, "Automatic data save failed", exception);
			}
		}
	}
}
