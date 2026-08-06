package pl.kiosel.villages.data.village.placeholders;

import pl.kiosel.core.utils.TimeUtils;
import pl.kiosel.core.utils.format.TimeFormatter;
import pl.kiosel.villages.addons.tablist.placeholders.Placeholders;
import pl.kiosel.villages.addons.tablist.placeholders.placeholder.FallbackPlaceholder;
import pl.kiosel.villages.addons.tablist.placeholders.resolver.*;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.VillageRank;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Function;

public class VillagePlaceholders extends Placeholders<Village, VillagePlaceholders> {

    public VillagePlaceholders property(String name, LocaleMonoResolver<Village> resolver, LocaleSimpleResolver fallbackResolver) {
        return this.property(name, new FallbackPlaceholder<>(resolver, fallbackResolver));
    }

    public VillagePlaceholders property(String name, MonoResolver<Village> resolver, LocaleSimpleResolver fallbackResolve) {
        return this.property(name, (entity, data) -> resolver.resolve(data), fallbackResolve);
    }

    public VillagePlaceholders rankProperty(String name, LocalePairResolver<Village, VillageRank> resolver, LocaleSimpleResolver fallbackResolver) {
        return this.property(name, (entity, village) -> resolver.resolve(entity, village, village.getRank()), fallbackResolver);
    }

    public VillagePlaceholders rankProperty(String name, MonoResolver<VillageRank> resolver, Number fallbackValue) {
        return this.property(name,
                (entity, village) -> {
                    Object value = resolver.resolve(village.getRank());
                    if (value instanceof Float || value instanceof Double) {
                        return String.format("%.2f", ((Number) value).floatValue());
                    }
                    return Objects.toString(value);
                },
                entity -> fallbackValue
        );
    }

    public VillagePlaceholders timeProperty(String name, Function<Village, Instant> timeSupplier, String nov) {
        String noValue = Objects.toString(nov, "");
        SimpleResolver fallbackResolver = () -> noValue;
        return this.property(name, (entity, village) -> formatDate(village, timeSupplier, new TimeFormatter("dd.MM.yyyy HH:mm:ss"), noValue), fallbackResolver)
                .property(name + "-time", (entity, village) -> formatTime(village, timeSupplier, noValue), fallbackResolver);
    }

    private static String formatDate(Village village, Function<Village, Instant> timeSupplier, TimeFormatter formatter, String noValue) {
        Instant endTime = timeSupplier.apply(village);
        return endTime.isBefore(Instant.now())
                ? noValue
                : formatter.format(endTime);
    }

    private static String formatTime(Village village, Function<Village, Instant> timeSupplier, String noValue) {
        Instant endTime = timeSupplier.apply(village);
        return endTime.isBefore(Instant.now())
                ? noValue
                : TimeUtils.formatTime(Duration.between(Instant.now(), endTime));
    }

    @Override
    public VillagePlaceholders create() {
        return new VillagePlaceholders();
    }

}
