package pl.kiosel.villages.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import pl.kiosel.villages.data.village.Village;

public class VillageCreateEvent extends VillageEvent implements Cancellable {

    private boolean cancelled;

    public VillageCreateEvent(Village village, Player player) {
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
