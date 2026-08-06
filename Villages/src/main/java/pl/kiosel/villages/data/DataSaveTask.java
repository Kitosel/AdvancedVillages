package pl.kiosel.villages.data;

import pl.kiosel.core.thread.MetaTask;
import pl.kiosel.villages.storage.Dataloader;

public class DataSaveTask extends MetaTask.AsyncMetaTask {

	private final Dataloader dataModel;
	private final boolean fullSave;

	public DataSaveTask(Dataloader dataModel, boolean fullSave) {
		this.dataModel = dataModel;
		this.fullSave = fullSave;
	}

	@Override
	public void execute() {
		this.dataModel.save(!this.fullSave);
	}
}
