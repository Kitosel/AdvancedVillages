package pl.kiosel.villages.addons.tablist.placeholders.resolver;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

@FunctionalInterface
public interface LocaleMonoResolver<T> extends Function<T, Object> {

    Object resolve(@Nullable Object entity, T data);

    @Override
    default Object apply(T data) {
        return this.resolve(null, data);
    }

}
