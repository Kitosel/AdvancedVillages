package pl.kiosel.villages.data.village.top;

import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.VillageManager;
import pl.kiosel.villages.data.village.VillageRank;
import pl.kiosel.villages.data.village.VillageRankManager;
import pl.kiosel.villages.data.rank.TopComparator;

import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;

public class VillageRecalculation implements BiFunction<String, TopComparator<VillageRank>, NavigableSet<VillageRank>> {

    private final VillageManager guildManager;

    public VillageRecalculation(VillageManager guildManager) {
        this.guildManager = guildManager;
    }

    @Override
    public NavigableSet<VillageRank> apply(String id, TopComparator<VillageRank> topComparator) {
        VillageRankManager rankManager = AdvancedVillages.getInstance().getVillageRankManager();
        NavigableSet<VillageRank> villageRank = new TreeSet<>(topComparator);
		Set<Village> villages = this.guildManager.getVillages();
		villages.forEach(village -> village.getRank().setPosition(id, 0));

		villages.stream()
                .filter(rankManager::isRankedVillage)
                .map(Village::getRank)
                .forEach(villageRank::add);

        int position = 0;
        for (VillageRank guildRank : villageRank) {
            guildRank.setPosition(id, ++position);
        }
        return villageRank;
    }
}
