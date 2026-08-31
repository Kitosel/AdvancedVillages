package pl.kiosel.villages.data.village.top;

import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.VillageRank;
import pl.kiosel.villages.data.rank.Top;
import pl.kiosel.villages.data.rank.TopComparator;

import java.util.NavigableSet;
import java.util.Optional;
import java.util.function.BiFunction;

public class VillageTop extends Top<VillageRank> {

    public VillageTop(TopComparator<VillageRank> comparator, BiFunction<String, TopComparator<VillageRank>, NavigableSet<VillageRank>> recalculateFunction) {
        super(comparator, recalculateFunction);
    }

    public Optional<Village> getVillage(int place) {
        return this.get(place).map(VillageRank::getVillage);
    }

}
