package pl.kiosel.villages.data.village;

import panda.std.Option;
import pl.kiosel.villages.data.village.top.VillageTop;
import pl.kiosel.villages.data.rank.RankManager;

import java.util.Map;

public class VillageRankManager extends RankManager<VillageTop, VillageRank> {

    public Option<Village> getVillage(String topId, int place) {
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
