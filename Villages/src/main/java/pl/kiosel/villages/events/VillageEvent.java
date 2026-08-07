package pl.kiosel.villages.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.kiosel.villages.data.village.Village;

public abstract class VillageEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();

	@Nullable
    private final Village village;

    public VillageEvent(@Nullable Village village, Player player) {
	    super(player);
		this.village = village;
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
