package pl.kiosel.villages.data.user.top;

import panda.std.stream.PandaStream;
import pl.kiosel.villages.data.rank.TopComparator;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.UserManager;
import pl.kiosel.villages.data.user.UserRank;

import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;

public class UserRecalculation implements BiFunction<String, TopComparator<UserRank>, NavigableSet<UserRank>> {

    private final UserManager userManager;

    public UserRecalculation(UserManager userManager) {
        this.userManager = userManager;
    }

    @Override
    public NavigableSet<UserRank> apply(String id, TopComparator<UserRank> topComparator) {
        NavigableSet<UserRank> usersRank = new TreeSet<>(topComparator);
		Set<User> users = this.userManager.getUsers();
		users.forEach(user -> user.getRank().setPosition(id, 0));

		PandaStream.of(users)
                .filterNot(user -> user.hasPermission("advancedvillages.ranking.exempt"))
                .map(User::getRank)
                .forEach(usersRank::add);

        int position = 0;
        for (UserRank userRank : usersRank) {
            userRank.setPosition(id, ++position);
        }

        return usersRank;
    }

}
