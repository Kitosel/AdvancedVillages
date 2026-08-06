package pl.kiosel.villages.addons.trials.animation.pattern;

import org.bukkit.Particle;
import pl.kiosel.villages.addons.trials.animation.CentralAnimationFrame;
import pl.kiosel.villages.addons.trials.animation.LevelAnimationStyle;

public final class DoubleRingPattern implements AnimationPattern {

    @Override
    public String name() {
        return "DOUBLE_RING";
    }

    @Override
    public void render(CentralAnimationFrame frame) {
        LevelAnimationStyle style = frame.getStyle();
        int perRing = Math.max(3, frame.getCount() / 2);
        for (int ring = 0; ring < 2; ring++) {
            double direction = ring == 0 ? 1.0 : -1.0;
            double radius = style.getRadius() * (ring == 0 ? 1.0 : 0.68) * frame.getScale();
            double baseY = style.getVerticalOffset() + style.getHeight() * (ring == 0 ? 0.2 : 0.8);
            Particle particle = ring == 0 ? style.getParticle() : style.getSecondaryParticle();
            for (int index = 0; index < perRing; index++) {
                double angle = Math.PI * 2 * index / perRing + frame.getPhase() * direction;
                double y = baseY + Math.sin(angle * 3 + frame.getPhase()) * style.getHeight() * 0.12;
                frame.emit(particle, Math.cos(angle) * radius, y, Math.sin(angle) * radius);
            }
        }
    }
}
