package pl.kiosel.villages.config.validation;

import pl.kiosel.rosacore.compatibility.ZMaterial;
import pl.kiosel.rosacore.config.ConfigValidator;
import pl.kiosel.rosacore.config.ConfigView;
import pl.kiosel.rosacore.config.ValidationContext;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.level.LevelManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LevelsConfigValidator implements ConfigValidator {

    private final AdvancedVillages plugin;

    private static final Pattern LEVEL = Pattern.compile("level-([1-9][0-9]*)", Pattern.CASE_INSENSITIVE);

    public LevelsConfigValidator(AdvancedVillages plugin) {
        this.plugin = plugin;
    }

    @Override
    public void validate(ConfigView config, ValidationContext context) {
        Set<Integer> levels = new HashSet<>();
        for (String path : config.getKeys(false)) {
            if (path.equals("config-version")) continue;
            Matcher match = LEVEL.matcher(path);
            int level = 0;
            if (match.matches()) {
                try {
                    level = Integer.parseInt(match.group(1));
                } catch (NumberFormatException ignored) {
                }
            }
            if (level < 1 || level > LevelManager.MAX_LEVEL) {
                context.error(path, "Expected Level-1 through Level-" + LevelManager.MAX_LEVEL);
                continue;
            }
            if (!levels.add(level)) context.error(path, "Duplicate definition of level " + level);
            if (ConfigChecks.section(config, path, context) == null) continue;
            if (!config.contains(path + ".Size")) {
                context.error(path + ".Size", "Region size is required");
            }
            ConfigChecks.number(config, path + ".Size", 1, Integer.MAX_VALUE, true, context);
            ConfigChecks.number(config, path + ".Cost-xp", 0, Integer.MAX_VALUE, true, context);
            ConfigChecks.number(config, path + ".Cost-eco", 0, Integer.MAX_VALUE, true, context);
            validateMaterials(config, path + ".Cost-item", context);
        }
        int highest = levels.stream().mapToInt(Integer::intValue).max().orElse(0);
        for (int level = 1; level <= Math.max(1, highest); level++) {
            if (!levels.contains(level)) context.error("Level-" + level, "Missing level; levels must be consecutive from 1");
        }
        if (this.plugin != null && this.plugin.getVillageManager() != null) {
            int usedLevel = this.plugin.getVillageManager().getVillages().stream()
                    .mapToInt(village -> village.getLevel().getLevel()).max().orElse(1);
            if (highest < usedLevel) {
                context.error("Level-" + usedLevel,
                        "This level is used by an existing village and cannot be removed");
            }
        }
    }

    private void validateMaterials(ConfigView config, String path, ValidationContext context) {
        List<?> entries = ConfigChecks.list(config, path, context);
        Set<ZMaterial> materials = new HashSet<>();
        for (int index = 0; index < entries.size(); index++) {
            Object entry = entries.get(index);
            String itemPath = path + "[" + index + "]";
            String[] parts = entry instanceof String text ? text.split(":", 2) : new String[0];
            if (parts.length != 2) {
                context.error(itemPath, "Expected MATERIAL:amount; got: " + entry);
                continue;
            }
            ZMaterial material = ZMaterial.match(parts[0].trim()).orElse(null);
            if (material == null || material.resolveForItem().isEmpty()) {
                context.error(itemPath, "Unknown or unavailable item material: " + parts[0]);
            } else if (!materials.add(material)) {
                context.error(itemPath, "Duplicate material: " + parts[0] + "; combine its amounts in one entry");
            }
            try {
                if (Integer.parseInt(parts[1].trim()) > 0) continue;
            } catch (NumberFormatException ignored) {
            }
            context.error(itemPath, "Item amount must be an integer between 1 and " + Integer.MAX_VALUE);
        }
    }
}
