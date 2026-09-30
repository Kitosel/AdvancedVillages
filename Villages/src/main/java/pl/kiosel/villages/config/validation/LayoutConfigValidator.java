package pl.kiosel.villages.config.validation;

import org.bukkit.configuration.ConfigurationSection;
import pl.kiosel.rosacore.config.ConfigValidator;
import pl.kiosel.rosacore.config.ConfigView;
import pl.kiosel.rosacore.config.ValidationContext;
import pl.kiosel.villages.data.village.level.LevelManager;

public final class LayoutConfigValidator implements ConfigValidator {

    @Override
    public void validate(ConfigView config, ValidationContext context) {
        ConfigurationSection layouts = ConfigChecks.section(config, "layout", context);
        if (layouts != null) {
            for (String menu : layouts.getKeys(false)) {
                String path = "layout." + menu;
                ConfigChecks.number(config, path + ".rows", 1, 6, true, context);
                int rows = Math.max(1, Math.min(6, config.getInt(path + ".rows", 1)));
                ConfigChecks.number(config, path + ".back-slot", 0, rows * 9 - 1, true, context);
                ConfigChecks.number(config, path + ".required-level", 0, LevelManager.MAX_LEVEL, true, context);
            }
        }
    }
}
