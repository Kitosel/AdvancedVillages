package pl.kiosel.villages.data.rank;

import panda.std.Option;

import java.util.*;

public abstract class RankManager<T extends Top<R>, R extends Rank<?>> {

    protected final Map<String, T> topMap = new HashMap<>();

    public Map<String, T> getTopMap() {
        return new HashMap<>(this.topMap);
    }

    public Set<String> getTopIds() {
        return this.topMap.keySet();
    }

    public Set<T> getTops() {
        return new HashSet<>(this.topMap.values());
    }

    public Option<T> getTop(String id) {
        return Option.of(this.topMap.get(id.toLowerCase(Locale.ROOT)));
    }

    public void addTop(String id, T top) {
        this.topMap.put(id.toLowerCase(Locale.ROOT), top);
    }

    public void recalculateTops() {
        this.topMap.forEach((id, top) -> top.recalculate(id));
    }

}