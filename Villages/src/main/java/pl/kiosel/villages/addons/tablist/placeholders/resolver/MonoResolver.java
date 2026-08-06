package pl.kiosel.villages.addons.tablist.placeholders.resolver;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface MonoResolver<T> extends LocaleMonoResolver<T> {

    Object resolve(T data);

    @Override
    default Object resolve(@Nullable Object entity, T data) {
        return this.resolve(data);
    }

}
