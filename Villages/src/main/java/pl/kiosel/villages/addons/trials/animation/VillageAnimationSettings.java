package pl.kiosel.villages.addons.trials.animation;

import lombok.Getter;
import org.bukkit.Particle;
import pl.kiosel.core.configuration.Config;
import pl.kiosel.villages.data.village.level.LevelManager;
import pl.kiosel.villages.settings.Settings;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Getter
public final class VillageAnimationSettings {

    private final boolean enabled;
    private final Creation creation;
    private final Central central;
    private final Removal removal;

    private VillageAnimationSettings(boolean enabled, Creation creation, Central central, Removal removal) {
        this.enabled = enabled;
        this.creation = creation;
        this.central = central;
        this.removal = removal;
    }

    public static VillageAnimationSettings load(Config config) {
        Creation creation = new Creation(
                config.getBoolean("creation.enabled", true),
                clamp(config.getInt("creation.duration-ticks", 28), 1, 200),
                particle(config.getString("creation.particle", "END_ROD"), Particle.END_ROD),
                clamp(config.getInt("creation.count", 4), 1, 100),
                Math.max(0.1, config.getDouble("creation.radius", 1.25))
        );

        Map<Integer, LevelAnimationStyle> styles = new HashMap<>();
        for (int level = 1; level <= LevelManager.MAX_LEVEL; level++) {
            String path = "central-block.levels." + level;
            Particle primary = particle(defaultParticle(level), Particle.FLAME);
            styles.put(level, new LevelAnimationStyle(
                    config.getBoolean(path + ".enabled", true),
                    normalizePattern(config.getString(path + ".pattern", defaultPattern(level))),
                    particle(config.getString(path + ".particle", defaultParticle(level)), primary),
                    particle(config.getString(path + ".secondary-particle", defaultSecondaryParticle(level)), primary),
                    clamp(config.getInt(path + ".count", 8 + level * 2), 1, 100),
                    Math.max(0.1, config.getDouble(path + ".radius", 0.65 + level * 0.12)),
                    Math.max(0.05, config.getDouble(path + ".height", 0.35 + level * 0.08)),
                    Math.max(0.1, config.getDouble(path + ".vertical-span", defaultVerticalSpan(level))),
                    config.getDouble(path + ".vertical-offset", 0.05),
                    Math.max(0.001, config.getDouble(path + ".speed", defaultSpeed(level)))
            ));
        }

        Central central = new Central(
                config.getBoolean("central-block.enabled", true),
                clamp(config.getInt("central-block.interval-ticks", 2), 1, 20 * 60),
                Math.max(1, config.getDouble("central-block.view-distance", 32)),
                clamp(config.getInt("central-block.upgrade-duration-ticks", 24), 4, 100),
                Collections.unmodifiableMap(styles)
        );

        Removal removal = new Removal(
                config.getBoolean("removal.enabled", true),
                clamp(config.getInt("removal.duration-ticks", 50), 1, 200),
                Math.max(1, config.getInt("removal.beep-interval-ticks", 5)),
                config.getString("removal.beep-sound", "BLOCK_NOTE_BLOCK_HAT"),
                (float) config.getDouble("removal.beep-volume", 1.0),
                config.getDouble("removal.beep-pitch-start", 0.65),
                config.getDouble("removal.beep-pitch-rise", 0.8),
                Math.max(0.1, config.getDouble("removal.knockback-radius", 4.5)),
                Math.max(0, config.getDouble("removal.knockback-strength", 0.75)),
                Math.max(0, config.getDouble("removal.knockback-y", 0.4))
        );

        return new VillageAnimationSettings(Settings.ADDONS_VILLAGE_ANIMATIONS_ENABLE.getBoolean(), creation, central, removal);
    }

	private static String normalizePattern(String value) {
        return value == null ? "HALO" : value.trim().toUpperCase(Locale.ROOT).replace('-', '_');
    }

    private static Particle particle(String value, Particle fallback) {
        try {
            return Particle.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static String defaultParticle(int level) {
        switch (level) {
            case 2: return "ENCHANT";
            case 3: return "END_ROD";
            case 4: return "SOUL_FIRE_FLAME";
            case 5: return "TOTEM_OF_UNDYING";
            case 6: return "DRAGON_BREATH";
            case 7: return "ELECTRIC_SPARK";
            case 8: return "SOUL_FIRE_FLAME";
            case 9: return "TOTEM_OF_UNDYING";
            case 10: return "DRAGON_BREATH";
            default: return "FLAME";
        }
    }

    private static String defaultSecondaryParticle(int level) {
        switch (level) {
            case 2: return "WITCH";
            case 3: return "ELECTRIC_SPARK";
            case 4: return "SOUL";
            case 5: return "END_ROD";
            case 6: return "PORTAL";
            case 7: return "END_ROD";
            case 8: return "WITCH";
            case 9: return "ELECTRIC_SPARK";
            case 10: return "END_ROD";
            default: return "SMOKE";
        }
    }

    private static String defaultPattern(int level) {
        switch (level) {
            case 2: return "DOUBLE_RING";
            case 3: return "HELIX";
            case 4: return "CROWN";
            case 5: return "ORBITS";
            case 6: return "SPIRAL";
            case 7: return "VORTEX";
            case 8: return "PILLARS";
            case 9: return "STAR";
            case 10: return "WAVE";
            default: return "HALO";
        }
    }

    private static double defaultVerticalSpan(int level) {
        return level >= 5 ? 1.3 : level >= 3 ? 1.15 : 0.65;
    }

    private static double defaultSpeed(int level) {
        return 0.09 + level * 0.012;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @Getter
    public static final class Creation {
        private final boolean enabled;
        private final int durationTicks;
        private final Particle particle;
        private final int count;
        private final double radius;

        private Creation(boolean enabled, int durationTicks, Particle particle, int count, double radius) {
            this.enabled = enabled;
            this.durationTicks = durationTicks;
            this.particle = particle;
            this.count = count;
            this.radius = radius;
        }

	}

    public static final class Central {
        @Getter
        private final boolean enabled;
        @Getter
        private final int intervalTicks;
        @Getter
        private final double viewDistance;
        @Getter
        private final int upgradeDurationTicks;
        private final Map<Integer, LevelAnimationStyle> styles;

        private Central(boolean enabled, int intervalTicks, double viewDistance,
                        int upgradeDurationTicks, Map<Integer, LevelAnimationStyle> styles) {
            this.enabled = enabled;
            this.intervalTicks = intervalTicks;
            this.viewDistance = viewDistance;
            this.upgradeDurationTicks = upgradeDurationTicks;
            this.styles = styles;
        }

		public LevelAnimationStyle styleFor(int level) {
			return styles.getOrDefault(Math.max(1, Math.min(LevelManager.MAX_LEVEL, level)), styles.get(1));
        }
    }

    @Getter
    public static final class Removal {
        private final boolean enabled;
        private final int durationTicks;
        private final int beepIntervalTicks;
        private final String beepSound;
        private final float beepVolume;
        private final double beepPitchStart;
        private final double beepPitchRise;
        private final double knockbackRadius;
        private final double knockbackStrength;
        private final double knockbackY;

        private Removal(boolean enabled, int durationTicks, int beepIntervalTicks, String beepSound,
                        float beepVolume, double beepPitchStart, double beepPitchRise,
                        double knockbackRadius, double knockbackStrength, double knockbackY) {
            this.enabled = enabled;
            this.durationTicks = durationTicks;
            this.beepIntervalTicks = Math.min(durationTicks, beepIntervalTicks);
            this.beepSound = beepSound;
            this.beepVolume = beepVolume;
            this.beepPitchStart = beepPitchStart;
            this.beepPitchRise = beepPitchRise;
            this.knockbackRadius = knockbackRadius;
            this.knockbackStrength = knockbackStrength;
            this.knockbackY = knockbackY;
        }

	}
}
