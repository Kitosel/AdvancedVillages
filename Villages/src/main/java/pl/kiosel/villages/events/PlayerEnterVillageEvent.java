package pl.kiosel.villages.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import pl.kiosel.villages.data.village.Village;

import java.util.Set;

@Getter
public class PlayerEnterVillageEvent extends VillageEvent {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final Village village;
    private final Set<Player> playersInsideVillage;

    public PlayerEnterVillageEvent(Village village, Player player, Set<Player> playersInsideVillage) {
        super(village, player);
        this.village = village;
        this.playersInsideVillage = playersInsideVillage;
    }

	@Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}