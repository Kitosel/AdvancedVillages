package pl.kiosel.villages.config.validation;

import org.bukkit.configuration.ConfigurationSection;
import pl.kiosel.rosacore.config.ConfigValidator;
import pl.kiosel.rosacore.config.ConfigView;
import pl.kiosel.rosacore.config.ValidationContext;
import pl.kiosel.rosacore.utils.TimeUtils;
import pl.kiosel.villages.data.user.VillageSpecialization;

public final class SpecializationConfigValidator implements ConfigValidator {

    @Override
    public void validate(ConfigView config, ValidationContext context) {
        ConfigChecks.bool(config, "enabled", context);
        Object cooldown = config.get("change-cooldown");
        if (cooldown != null) {
            try {
                if (!(cooldown instanceof String text) || TimeUtils.parseTimeDuration(text).isNegative()) {
                    throw new IllegalArgumentException();
                }
            } catch (RuntimeException exception) {
                context.error("change-cooldown", "Expected a duration such as 1h, 30m or 0s; got: " + cooldown);
            }
        }

        ConfigurationSection specializations = ConfigChecks.section(config, "specializations", context);
        if (specializations == null) return;
        for (String id : specializations.getKeys(false)) {
            String path = "specializations." + id;
            if (VillageSpecialization.fromId(id).filter(value -> value.getId().equals(id)).isEmpty()) {
                context.error(path, "Unknown specialization: " + id);
                continue;
            }
            ConfigurationSection section = ConfigChecks.section(config, path, context);
            if (section == null) continue;
            for (String key : section.getKeys(false)) {
                if (!key.equals("enabled") && !key.equals("bonus-percent")) {
                    context.error(path + "." + key, "Unknown specialization setting; expected enabled or bonus-percent");
                }
            }
            ConfigChecks.bool(config, path + ".enabled", context);
            ConfigChecks.number(config, path + ".bonus-percent", 0, 100, false, context);
        }
    }
}
