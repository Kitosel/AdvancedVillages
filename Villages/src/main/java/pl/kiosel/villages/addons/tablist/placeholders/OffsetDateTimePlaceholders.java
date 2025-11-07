package pl.kiosel.villages.addons.tablist.placeholders;

import pl.kiosel.core.utils.TextUtils;
import pl.kiosel.villages.addons.tablist.placeholders.resolver.MonoResolver;
import pl.kiosel.villages.addons.tablist.placeholders.resolver.PairResolver;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;

public class OffsetDateTimePlaceholders extends Placeholders<OffsetDateTime, OffsetDateTimePlaceholders> {

    public OffsetDateTimePlaceholders timeProperty(String name, MonoResolver<OffsetDateTime> timeResolver) {
        return this.property(name, (entity, data) -> TextUtils.appendDigit(Objects.toString(timeResolver.resolve(entity, data))));
    }

    public OffsetDateTimePlaceholders timeProperty(String name, PairResolver<OffsetDateTime, Locale> timeResolver) {
        return this.property(name, (entity, data) -> {
			Locale locale = Locale.GERMAN;
            return Objects.toString(timeResolver.resolve(entity, data, locale));
        });
    }

    @Override
    public OffsetDateTimePlaceholders create() {
        return new OffsetDateTimePlaceholders();
    }

}
