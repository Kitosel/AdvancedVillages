package pl.kiosel.villages.addons.trials.animation;

import lombok.Getter;
import org.bukkit.Particle;

@Getter
public final class LevelAnimationStyle {

    private final boolean enabled;
    private final String pattern;
    private final Particle particle;
    private final Particle secondaryParticle;
    private final int count;
    private final double radius;
    private final double height;
    private final double verticalSpan;
    private final double verticalOffset;
    private final double speed;

    LevelAnimationStyle(boolean enabled, String pattern, Particle particle, Particle secondaryParticle,
                        int count, double radius, double height, double verticalSpan,
                        double verticalOffset, double speed) {
        this.enabled = enabled;
        this.pattern = pattern;
        this.particle = particle;
        this.secondaryParticle = secondaryParticle;
        this.count = count;
        this.radius = radius;
        this.height = height;
        this.verticalSpan = verticalSpan;
        this.verticalOffset = verticalOffset;
        this.speed = speed;
    }

}
