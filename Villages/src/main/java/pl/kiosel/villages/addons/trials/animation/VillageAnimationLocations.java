package pl.kiosel.villages.addons.trials.animation;

import org.bukkit.Location;
import pl.kiosel.villages.data.village.Village;

final class VillageAnimationLocations {

    private VillageAnimationLocations() {
    }

    static Location centralBlock(Village village) {
        return village.getLocation().get().clone().add(0.5, 1.0, 0.5);
    }
}
