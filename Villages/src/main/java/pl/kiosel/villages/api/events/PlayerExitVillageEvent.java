package pl.kiosel.villages.api.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class PlayerExitVillageEvent extends Event {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final Player player;

    public PlayerExitVillageEvent(Player player) {
        this.player = player;
    }

	@Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}