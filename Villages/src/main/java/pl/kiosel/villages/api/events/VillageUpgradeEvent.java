package pl.kiosel.villages.api.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import pl.kiosel.villages.data.village.Upgrade;
import pl.kiosel.villages.data.village.Village;

public class VillageUpgradeEvent extends VillageEvent implements Cancellable {

    private boolean cancelled;
	@Getter @Setter
	private Upgrade actualUpgrade;
	@Getter @Setter
	private Upgrade nextUpgrade;
	@Getter @Setter
	private int moneyCost;

    public VillageUpgradeEvent(Village village, Player player, Upgrade actualUpgrade, Upgrade nextUpgrade, int moneyCost) {
        super(village, player);
		this.actualUpgrade = actualUpgrade;
		this.nextUpgrade = nextUpgrade;
		this.moneyCost = moneyCost;
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
