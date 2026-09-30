package pl.kiosel.villages.config.validation;

import pl.kiosel.rosacore.compatibility.ZMaterial;
import pl.kiosel.rosacore.config.ConfigValidator;
import pl.kiosel.rosacore.config.ConfigView;
import pl.kiosel.rosacore.config.ValidationContext;

public final class GuiConfigValidator implements ConfigValidator {

    @Override
    public void validate(ConfigView config, ValidationContext context) {
        for (String path : config.getKeys(true)) {
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
