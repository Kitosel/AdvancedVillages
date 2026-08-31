package pl.kiosel.villages.data;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public interface VEntity {

	UnitType getType();

	String getName();

	default String getIdentityKey() {
		return this.getType() + ":" + this.getName();
	}

    static <T extends VEntity> Set<String> names(Iterable<T> entities) {
        return StreamSupport.stream(entities.spliterator(), false)
                .map(VEntity::getName)
                .collect(Collectors.toSet());
    }

    enum UnitType {
        VILLAGE,
        OFFLINE_USER,
        RANK,
        REGION,
        USER
    }
}