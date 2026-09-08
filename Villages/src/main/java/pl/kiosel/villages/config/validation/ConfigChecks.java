package pl.kiosel.villages.config.validation;

import org.bukkit.configuration.ConfigurationSection;
import pl.kiosel.rosacore.config.ConfigView;
import pl.kiosel.rosacore.config.ValidationContext;

import java.util.Collections;
import java.util.List;

final class ConfigChecks {

    private ConfigChecks() {
    }

    static ConfigurationSection section(ConfigView config, String path, ValidationContext context) {
        Object value = config.get(path);
        if (value == null) return null;
        if (value instanceof ConfigurationSection section) return section;
        context.error(path, "Expected a configuration section");
        return null;
    }

    static void bool(ConfigView config, String path, ValidationContext context) {
        Object value = config.get(path);
        if (value != null && !(value instanceof Boolean)) {
            context.error(path, "Expected true or false");
        }
    }

    static void number(ConfigView config, String path, double minimum, double maximum,
                       boolean integer, ValidationContext context) {
        Object value = config.get(path);
        if (value == null) return;
        if (value instanceof Number number) {
            double parsed = number.doubleValue();
            if (Double.isFinite(parsed) && parsed >= minimum && parsed <= maximum
                    && (!integer || parsed == Math.rint(parsed))) return;
        }
        context.error(path, "Expected " + (integer ? "an integer" : "a finite number")
                + " between " + minimum + " and " + maximum + "; got: " + value);
    }

    static List<?> list(ConfigView config, String path, ValidationContext context) {
        Object value = config.get(path);
        if (value == null) return Collections.emptyList();
        if (value instanceof List<?> list) return list;
        context.error(path, "Expected a list");
        return Collections.emptyList();
    }
}
