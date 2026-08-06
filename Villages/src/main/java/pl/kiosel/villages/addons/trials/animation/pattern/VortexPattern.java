package pl.kiosel.villages.addons.trials.animation.pattern;

import pl.kiosel.villages.addons.trials.animation.CentralAnimationFrame;
import pl.kiosel.villages.addons.trials.animation.LevelAnimationStyle;

public final class VortexPattern implements AnimationPattern {

    @Override
    public String name() {
        return "VORTEX";
    }

    @Override
    public void render(CentralAnimationFrame frame) {
        LevelAnimationStyle style = frame.getStyle();
        double movement = frame.getPhase() / (Math.PI * 2);
        for (int index = 0; index < frame.getCount(); index++) {
            double progress = (index / (double) frame.getCount() + movement) % 1.0;
            double angle = frame.getPhase() * 1.5 + progress * Math.PI * 6;
            double radius = style.getRadius() * (0.18 + progress * 0.82) * frame.getScale();
            frame.emit(frame.alternatingParticle(index, 3),
                    Math.cos(angle) * radius,
                    style.getVerticalOffset() + progress * style.getVerticalSpan(),
                    Math.sin(angle) * radius);
        }
    }
}
