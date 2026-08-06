package pl.kiosel.villages.data.rank;

import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.VillageRankManager;
import pl.kiosel.villages.data.user.UserRankManager;

public class RankRecalculationTask implements Runnable {

    private final UserRankManager userRankManager;
    private final VillageRankManager villageRankManager;

    public RankRecalculationTask(AdvancedVillages plugin) {
        this.userRankManager = plugin.getUserRankManager();
        this.villageRankManager = plugin.getVillageRankManager();
    }

    @Override
    public void run() {
        this.userRankManager.recalculateTops();
        this.villageRankManager.recalculateTops();
    }

}
