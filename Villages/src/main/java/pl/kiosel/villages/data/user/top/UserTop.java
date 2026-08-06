package pl.kiosel.villages.data.user.top;

import panda.std.Option;
import pl.kiosel.villages.data.rank.Top;
import pl.kiosel.villages.data.rank.TopComparator;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.UserRank;

import java.util.NavigableSet;
import java.util.function.BiFunction;

public class UserTop extends Top<UserRank> {

    public UserTop(TopComparator<UserRank> comparator, BiFunction<String, TopComparator<UserRank>, NavigableSet<UserRank>> recalculateFunction) {
        super(comparator, recalculateFunction);
    }

    public Option<User> getUser(int place) {
        return this.get(place).map(UserRank::getUser);
    }

}
