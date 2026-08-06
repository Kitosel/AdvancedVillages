package pl.kiosel.villages.addons.trials.animation;

import pl.kiosel.villages.addons.trials.animation.pattern.*;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

final class CentralAnimationPatterns {

    private final Map<String, AnimationPattern> patterns = new LinkedHashMap<>();

    CentralAnimationPatterns() {
        register(new HaloPattern());
        register(new DoubleRingPattern());
        register(new HelixPattern());
        register(new CrownPattern());
        register(new OrbitsPattern());
        register(new SpiralPattern());
        register(new VortexPattern());
        register(new PillarsPattern());
        register(new WavePattern());
        register(new StarPattern());
    }

    void render(String name, CentralAnimationFrame frame) {
        AnimationPattern pattern = patterns.get(normalize(name));
        if (pattern == null) {
            pattern = patterns.get("HALO");
        }
        pattern.render(frame);
    }

    private void register(AnimationPattern pattern) {
        patterns.put(pattern.name(), pattern);
    }

    private String normalize(String name) {
        return name == null ? "HALO" : name.trim().toUpperCase(Locale.ROOT).replace('-', '_');
    }
}
