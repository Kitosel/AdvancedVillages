package pl.kiosel.villages.data.user.top;

import pl.kiosel.villages.data.rank.Rank;
import pl.kiosel.villages.data.rank.TopComparator;
import pl.kiosel.villages.data.user.UserRank;

import java.util.function.Function;

public final class UserComparator implements TopComparator<UserRank> {

    public static final TopComparator<UserRank> POINTS_COMPARATOR = new UserComparator(UserRank::getPoints).reversed();
    public static final TopComparator<UserRank> KILLS_COMPARATOR = new UserComparator(UserRank::getKills).reversed();
    public static final TopComparator<UserRank> DEATHS_COMPARATOR = new UserComparator(UserRank::getDeaths).reversed();
    public static final TopComparator<UserRank> KDR_COMPARATOR = new UserComparator(UserRank::getKDR).reversed();
    public static final TopComparator<UserRank> KDA_COMPARATOR = new UserComparator(UserRank::getKDA).reversed();
    public static final TopComparator<UserRank> ASSISTS_COMPARATOR = new UserComparator(UserRank::getAssists).reversed();
    public static final TopComparator<UserRank> LOGOUTS_COMPARATOR = new UserComparator(UserRank::getLogouts).reversed();

    private final Function<UserRank, Number> valueFunction;

    private UserComparator(Function<UserRank, Number> valueFunction) {
        this.valueFunction = valueFunction;
    }

    @Override
    public int compare(UserRank o1, UserRank o2) {
		int result = Double.compare(this.getValue(o1).doubleValue(), this.getValue(o2).doubleValue());
        if (result == 0) {
            result = Rank.compareName(o1, o2);
        }

        return result;
    }

    @Override
    public Number getValue(UserRank rank) {
        return this.valueFunction.apply(rank);
    }

}
