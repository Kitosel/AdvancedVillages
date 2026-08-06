package pl.kiosel.villages.data.village;

import panda.std.Option;
import panda.std.stream.PandaStream;
import pl.kiosel.villages.data.village.top.VillageTop;
import pl.kiosel.villages.data.rank.RankManager;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class VillageRankManager extends RankManager<VillageTop, VillageRank> {

	private final Set<String> enabledVillageTops = new TreeSet<>(Arrays.asList("kills", "deaths", "avg_points"));

    public Option<Village> getVillage(String topId, int place) {
        return this.getTop(topId).flatMap(top -> top.getVillage(place));
    }

    public boolean isRankedVillage(Village village) {
        return village.getMembers().size() >= 2;
    }

    public void register(String id, VillageTop villageTop) {
        PandaStream.of(enabledVillageTops)
                .find(enabledTop -> enabledTop.equalsIgnoreCase(id))
                .peek(enabledTop -> this.addTop(id, villageTop));
    }

    public void register(Map<String, VillageTop> topsToRegister) {
        topsToRegister.forEach(this::register);
    }

}
