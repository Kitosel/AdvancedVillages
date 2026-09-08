package pl.kiosel.villages.config.validation;

import org.bukkit.configuration.ConfigurationSection;
import pl.kiosel.rosacore.compatibility.ZMaterial;
import pl.kiosel.rosacore.config.ConfigValidator;
import pl.kiosel.rosacore.config.ConfigView;
import pl.kiosel.rosacore.config.ValidationContext;
public final class GuiConfigValidator implements ConfigValidator {

    @Override
    public void validate(ConfigView config, ValidationContext context) {
        ConfigurationSection layouts = ConfigChecks.section(config, "layout", context);
        if (layouts != null) {
            for (String menu : layouts.getKeys(false)) {
                String path = "layout." + menu;
                ConfigChecks.number(config, path + ".rows", 1, 6, true, context);
                int rows = Math.max(1, Math.min(6, config.getInt(path + ".rows", 1)));
                ConfigChecks.number(config, path + ".back-slot", 0, rows * 9 - 1, true, context);
            }
        }
        for (String path : config.getKeys(true)) {
            if (!path.startsWith("guis.")) continue;
            if (path.endsWith(".slot")) {
                ConfigChecks.number(config, path, 0, 53, true, context);
                continue;
            }
            if (path.endsWith(".amount")) {
                ConfigChecks.number(config, path, 1, 64, true, context);
                continue;
            }
            if (!path.endsWith(".material")) continue;
            Object value = config.get(path);
            ZMaterial compatible = value instanceof String text
                    ? ZMaterial.match(text.trim()).orElse(null) : null;
            if (compatible == null || compatible.resolveForItem().isEmpty()) {
                context.error(path, "Unknown GUI item material: " + value);
            }
        }
    }
}
