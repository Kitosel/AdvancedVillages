package pl.kiosel.villages.api.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.kiosel.villages.data.village.Village;

public abstract class VillageEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

	@Nullable
    private final Village village;
    @Getter
	private final Player player;

    public VillageEvent(@Nullable Village village, Player player) {
        this.village = village;
        this.player = player;
    }

    public @Nullable Village getVillage() {
        return village;
    }

	public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
