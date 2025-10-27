package pl.kiosel.villages.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import pl.kiosel.villages.village.Village;

public class VillageRemoveEvent extends VillageEvent implements Cancellable {

    private boolean cancelled;

    public VillageRemoveEvent(Village village, Player player) {
        super(village, player);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
