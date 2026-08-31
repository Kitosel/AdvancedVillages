package pl.kiosel.villages.data.village;

import pl.kiosel.villages.data.village.top.VillageTop;
import pl.kiosel.villages.data.rank.RankManager;

import java.util.Map;
import java.util.Optional;

public class VillageRankManager extends RankManager<VillageTop, VillageRank> {

    public Optional<Village> getVillage(String topId, int place) {
        return this.getTop(topId).flatMap(top -> top.getVillage(place));
    }

    public boolean isRankedVillage(Village village) {
		return village != null && !village.getMembers().isEmpty();
    }

    public void register(String id, VillageTop villageTop) {
		this.addTop(id, villageTop);
    }

    public void register(Map<String, VillageTop> topsToRegister) {
        topsToRegister.forEach(this::register);
    }

}
