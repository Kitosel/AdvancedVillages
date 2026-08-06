package pl.kiosel.villages.data.user;

import panda.std.Option;
import panda.std.stream.PandaStream;
import pl.kiosel.villages.data.rank.RankManager;
import pl.kiosel.villages.data.user.top.UserTop;

import java.util.Map;

public class UserRankManager extends RankManager<UserTop, UserRank> {

    public Option<User> getUser(String topId, int place) {
        return this.getTop(topId).flatMap(top -> top.getUser(place));
    }

    public void register(String id, UserTop userTop) {
        PandaStream.of(id)
                .find(top -> top.equalsIgnoreCase(id))
                .peek(enabledTop -> this.addTop(id, userTop));
    }

    public void register(Map<String, UserTop> topsToRegister) {
        topsToRegister.forEach(this::register);
    }

}
