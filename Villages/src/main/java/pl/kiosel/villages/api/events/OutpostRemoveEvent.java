package pl.kiosel.villages.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import pl.kiosel.villages.data.outpost.Outpost;

public class OutpostRemoveEvent extends OutpostEvent implements Cancellable {

    private boolean cancelled;

    public OutpostRemoveEvent(Outpost outpost, Player player) {
        super(outpost, player);
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
