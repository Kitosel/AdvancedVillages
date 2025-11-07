package pl.kiosel.villages.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import pl.kiosel.villages.data.village.Village;

public class PlayerEnterVillageEvent extends VillageEvent {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final Player player;
    private final Village village;

    public PlayerEnterVillageEvent(Village village, Player player) {
        super(village, player);
        this.player = player;
        this.village = village;
    }

    public Village getVillage() {
        return village;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}