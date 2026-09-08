package pl.kiosel.villages.data.village.handler.turret;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import pl.kiosel.rosacore.listener.RosaListener;
import pl.kiosel.villages.AdvancedVillages;

public class WorldEditTurretListener extends RosaListener {

	private final AdvancedVillages plugin;

	public WorldEditTurretListener(AdvancedVillages plugin) {
		super(plugin);
		this.plugin = plugin;
		WorldEdit.getInstance().getEventBus().register(this);
	}

	@Override
	public boolean isAvailable() {
		return plugin.getHookManager().isEnabled("WorldEdit");
	}

	@Subscribe
	public void onEditSession(EditSessionEvent event) {
		if (event.getStage() != EditSession.Stage.BEFORE_CHANGE || event.getWorld() == null) return;
		Actor actor = event.getActor();
		if (actor == null || !actor.isPlayer()) return;
		event.setExtent(new VillageProtectionExtent(
				event.getExtent(),
				this.plugin,
				event.getWorld().getName(),
				actor.getUniqueId()
		));
	}

	public void unregister() {
		WorldEdit.getInstance().getEventBus().unregister(this);
	}
}
