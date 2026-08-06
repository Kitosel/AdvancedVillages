package pl.kiosel.villages.data.rank;

import com.google.common.collect.ImmutableMap;
import pl.kiosel.villages.data.village.VillageManager;
import pl.kiosel.villages.data.village.top.VillageComparator;
import pl.kiosel.villages.data.village.top.VillageRecalculation;
import pl.kiosel.villages.data.village.top.VillageTop;
import pl.kiosel.villages.data.user.UserManager;
import pl.kiosel.villages.data.user.top.UserComparator;
import pl.kiosel.villages.data.user.top.UserRecalculation;
import pl.kiosel.villages.data.user.top.UserTop;

import java.util.Map;

public final class DefaultTops {

    public static final String USER_POINTS_TOP = "points";
    public static final String USER_KILLS_TOP = "kills";
    public static final String USER_DEATHS_TOP = "deaths";
    public static final String USER_KDR_TOP = "kdr";
    public static final String USER_KDA_TOP = "kda";
    public static final String USER_ASSISTS_TOP = "assists";
    public static final String USER_LOGOUTS_TOP = "logouts";

    public static final String VILLAGE_POINTS_TOP = "points";
    public static final String VILLAGE_KILLS_TOP = "kills";
    public static final String VILLAGE_DEATHS_TOP = "deaths";
    public static final String VILLAGE_KDR_TOP = "kdr";
    public static final String VILLAGE_KDA_TOP = "kda";
    public static final String VILLAGE_ASSISTS_TOP = "assists";
    public static final String VILLAGE_LOGOUTS_TOP = "logouts";

    public static final String VILLAGE_AVG_POINTS_TOP = "avg_points";
    public static final String VILLAGE_AVG_KILLS_TOP = "avg_kills";
    public static final String VILLAGE_AVG_DEATHS_TOP = "avg_deaths";
    public static final String VILLAGE_AVG_KDR_TOP = "avg_kdr";
    public static final String VILLAGE_AVG_KDA_TOP = "avg_kda";
    public static final String VILLAGE_AVG_ASSISTS_TOP = "avg_assists";
    public static final String VILLAGE_AVG_LOGOUTS_TOP = "avg_logouts";

    private DefaultTops() {
    }

    public static Map<String, UserTop> defaultUserTops(UserManager userManager) {
        UserRecalculation recalculation = new UserRecalculation(userManager);
        return ImmutableMap.<String, UserTop>builder()
                .put(USER_POINTS_TOP, new UserTop(UserComparator.POINTS_COMPARATOR, recalculation))
                .put(USER_KILLS_TOP, new UserTop(UserComparator.KILLS_COMPARATOR, recalculation))
                .put(USER_DEATHS_TOP, new UserTop(UserComparator.DEATHS_COMPARATOR, recalculation))
                .put(USER_KDR_TOP, new UserTop(UserComparator.KDR_COMPARATOR, recalculation))
                .put(USER_KDA_TOP, new UserTop(UserComparator.KDA_COMPARATOR, recalculation))
                .put(USER_ASSISTS_TOP, new UserTop(UserComparator.ASSISTS_COMPARATOR, recalculation))
                .put(USER_LOGOUTS_TOP, new UserTop(UserComparator.LOGOUTS_COMPARATOR, recalculation))
                .build();
    }

    public static Map<String, VillageTop> defaultVillageTops(VillageManager villageManager) {
        VillageRecalculation recalculation = new VillageRecalculation(villageManager);
        return ImmutableMap.<String, VillageTop>builder()
                .put(VILLAGE_POINTS_TOP, new VillageTop(VillageComparator.POINTS_COMPARATOR, recalculation))
                .put(VILLAGE_KILLS_TOP, new VillageTop(VillageComparator.KILLS_COMPARATOR, recalculation))
                .put(VILLAGE_DEATHS_TOP, new VillageTop(VillageComparator.DEATHS_COMPARATOR, recalculation))
                .put(VILLAGE_KDR_TOP, new VillageTop(VillageComparator.KDR_COMPARATOR, recalculation))
                .put(VILLAGE_KDA_TOP, new VillageTop(VillageComparator.KDA_COMPARATOR, recalculation))
                .put(VILLAGE_ASSISTS_TOP, new VillageTop(VillageComparator.ASSISTS_COMPARATOR, recalculation))
                .put(VILLAGE_LOGOUTS_TOP, new VillageTop(VillageComparator.LOGOUTS_COMPARATOR, recalculation))
                .put(VILLAGE_AVG_POINTS_TOP, new VillageTop(VillageComparator.AVG_POINTS_COMPARATOR, recalculation))
                .put(VILLAGE_AVG_KILLS_TOP, new VillageTop(VillageComparator.AVG_KILLS_COMPARATOR, recalculation))
                .put(VILLAGE_AVG_DEATHS_TOP, new VillageTop(VillageComparator.AVG_DEATHS_COMPARATOR, recalculation))
                .put(VILLAGE_AVG_KDR_TOP, new VillageTop(VillageComparator.AVG_KDR_COMPARATOR, recalculation))
                .put(VILLAGE_AVG_KDA_TOP, new VillageTop(VillageComparator.AVG_KDA_COMPARATOR, recalculation))
                .put(VILLAGE_AVG_ASSISTS_TOP, new VillageTop(VillageComparator.AVG_ASSISTS_COMPARATOR, recalculation))
                .put(VILLAGE_AVG_LOGOUTS_TOP, new VillageTop(VillageComparator.AVG_LOGOUTS_COMPARATOR, recalculation))
                .build();
    }

}
