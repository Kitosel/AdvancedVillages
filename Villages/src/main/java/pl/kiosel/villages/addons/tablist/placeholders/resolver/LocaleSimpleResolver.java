package pl.kiosel.villages.addons.tablist.placeholders.resolver;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@FunctionalInterface
public interface LocaleSimpleResolver extends Consumer<Object> {

    Object resolve(@Nullable Object entity);

    @Override
    default void accept(Object o) {
        this.resolve(o);
    }

}
