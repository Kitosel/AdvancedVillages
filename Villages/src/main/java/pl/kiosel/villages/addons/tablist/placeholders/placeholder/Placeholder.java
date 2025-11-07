package pl.kiosel.villages.addons.tablist.placeholders.placeholder;

import org.jetbrains.annotations.Nullable;
import pl.kiosel.villages.addons.tablist.placeholders.resolver.LocaleMonoResolver;

import java.util.Objects;

public class Placeholder<T> {

    private final LocaleMonoResolver<T> resolver;

    public Placeholder(LocaleMonoResolver<T> resolver) {
        this.resolver = resolver;
    }

    public Object getRaw(@Nullable Object entity, T data) {
        return this.resolver.resolve(entity, data);
    }

    public String get(@Nullable Object entity, T data) {
        return Objects.toString(this.getRaw(entity, data), "");
    }

}
