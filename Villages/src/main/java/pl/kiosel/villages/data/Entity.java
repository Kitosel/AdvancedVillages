package pl.kiosel.villages.data;

import panda.std.stream.PandaStream;

import java.util.Set;

public interface Entity {

	UnitType getType();

    String getName();

    static <T extends Entity> Set<String> names(Iterable<T> entities) {
        return PandaStream.of(entities)
                .map(Entity::getName)
                .toSet();
    }

    enum UnitType {

        GUILD,
        OFFLINE_USER,
        RANK,
        REGION,
        USER

    }

}
