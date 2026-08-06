package pl.kiosel.villages.addons.tablist.placeholders.placeholder;

import pl.kiosel.villages.addons.tablist.placeholders.resolver.LocaleMonoResolver;
import pl.kiosel.villages.addons.tablist.placeholders.resolver.LocaleSimpleResolver;

public class FallbackPlaceholder<T> extends Placeholder<T> {

    private final LocaleSimpleResolver fallbackResolver;

    public FallbackPlaceholder(LocaleMonoResolver<T> resolver, LocaleSimpleResolver fallbackResolver) {
        super(resolver);
        this.fallbackResolver = fallbackResolver;
    }

    @Override
    public Object getRaw(Object entity, T data) {
        if (data == null) {
            return this.getRawFallback(entity);
        }

        return super.getRaw(entity, data);
    }

    public Object getRawFallback(Object entity) {
        return this.fallbackResolver.resolve(entity);
    }

}
