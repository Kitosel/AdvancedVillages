package pl.kiosel.villages.data.rank;

import lombok.Getter;
import panda.std.Option;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.function.BiFunction;

public class Top<T> {

    @Getter
    private final TopComparator<T> comparator;
    private final BiFunction<String, TopComparator<T>, NavigableSet<T>> recalculateFunction;
	private volatile List<T> values;

    public Top(TopComparator<T> comparator, BiFunction<String, TopComparator<T>, NavigableSet<T>> recalculateFunction) {
        this.comparator = comparator;
        this.recalculateFunction = recalculateFunction;
		this.values = Collections.emptyList();
    }

	public Option<T> get(int place) {
		List<T> snapshot = this.values;
		return Option.when(place > 0 && place <= snapshot.size(), () -> snapshot.get(place - 1));
    }

    public int count() {
        return this.values.size();
    }

    public void recalculate(String id) {
		this.values = Collections.unmodifiableList(new ArrayList<>(
				this.recalculateFunction.apply(id, this.comparator)
		));
    }

}
