package pl.kiosel.villages.data.village.top;

import pl.kiosel.villages.data.village.VillageRank;
import pl.kiosel.villages.data.rank.Rank;
import pl.kiosel.villages.data.rank.TopComparator;

import java.util.function.Function;

public final class VillageComparator implements TopComparator<VillageRank> {

    public static final TopComparator<VillageRank> POINTS_COMPARATOR = new VillageComparator(VillageRank::getPoints).reversed();
    public static final TopComparator<VillageRank> KILLS_COMPARATOR = new VillageComparator(VillageRank::getKills).reversed();
    public static final TopComparator<VillageRank> DEATHS_COMPARATOR = new VillageComparator(VillageRank::getDeaths).reversed();
    public static final TopComparator<VillageRank> KDR_COMPARATOR = new VillageComparator(VillageRank::getKDR).reversed();
    public static final TopComparator<VillageRank> KDA_COMPARATOR = new VillageComparator(VillageRank::getKDA).reversed();
    public static final TopComparator<VillageRank> ASSISTS_COMPARATOR = new VillageComparator(VillageRank::getAssists).reversed();
    public static final TopComparator<VillageRank> LOGOUTS_COMPARATOR = new VillageComparator(VillageRank::getLogouts).reversed();

    public static final TopComparator<VillageRank> AVG_POINTS_COMPARATOR = new VillageComparator(VillageRank::getAveragePoints).reversed();
    public static final TopComparator<VillageRank> AVG_KILLS_COMPARATOR = new VillageComparator(VillageRank::getAverageKills).reversed();
    public static final TopComparator<VillageRank> AVG_DEATHS_COMPARATOR = new VillageComparator(VillageRank::getAverageDeaths).reversed();
    public static final TopComparator<VillageRank> AVG_KDR_COMPARATOR = new VillageComparator(VillageRank::getAverageKDR).reversed();
    public static final TopComparator<VillageRank> AVG_KDA_COMPARATOR = new VillageComparator(VillageRank::getAverageKDA).reversed();
    public static final TopComparator<VillageRank> AVG_ASSISTS_COMPARATOR = new VillageComparator(VillageRank::getAverageAssists).reversed();
    public static final TopComparator<VillageRank> AVG_LOGOUTS_COMPARATOR = new VillageComparator(VillageRank::getAverageLogouts).reversed();

    private final Function<VillageRank, Number> valueFunction;

    private VillageComparator(Function<VillageRank, Number> valueFunction) {
        this.valueFunction = valueFunction;
    }

    @Override
    public int compare(VillageRank o1, VillageRank o2) {
        int result = Float.compare(this.getValue(o1).floatValue(), this.getValue(o2).floatValue());
        if (result == 0) {
            result = Rank.compareName(o1, o2);
        }

        return result;
    }

    @Override
    public Number getValue(VillageRank rank) {
        return this.valueFunction.apply(rank);
    }

}
