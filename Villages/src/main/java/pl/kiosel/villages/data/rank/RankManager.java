package pl.kiosel.villages.data.rank;

import java.util.*;

public abstract class RankManager<T extends Top<R>, R extends Rank<?>> {

    protected final Map<String, T> topMap = new LinkedHashMap<>();

    public Map<String, T> getTopMap() {
        return new HashMap<>(this.topMap);
    }

    public Set<String> getTopIds() {
		return Collections.unmodifiableSet(new LinkedHashSet<>(this.topMap.keySet()));
    }

    public Set<T> getTops() {
		return Collections.unmodifiableSet(new LinkedHashSet<>(this.topMap.values()));
    }

    public Optional<T> getTop(String id) {
		if (id == null || id.isBlank()) return Optional.empty();
        return Optional.ofNullable(this.topMap.get(id.toLowerCase(Locale.ROOT)));
    }

    public void addTop(String id, T top) {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(top, "top");
        this.topMap.put(id.toLowerCase(Locale.ROOT), top);
    }

    public void recalculateTops() {
        this.topMap.forEach((id, top) -> top.recalculate(id));
    }

}
