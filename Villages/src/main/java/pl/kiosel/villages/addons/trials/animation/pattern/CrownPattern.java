package pl.kiosel.villages.addons.trials.animation.pattern;

import pl.kiosel.villages.addons.trials.animation.CentralAnimationFrame;
import pl.kiosel.villages.addons.trials.animation.LevelAnimationStyle;

public final class CrownPattern implements AnimationPattern {

    @Override
    public String name() {
        return "CROWN";
    }

    @Override
    public void render(CentralAnimationFrame frame) {
        LevelAnimationStyle style = frame.getStyle();
        int count = frame.getCount();
        for (int index = 0; index < count; index++) {
            double angle = Math.PI * 2 * index / count + frame.getPhase();
            double peak = 0.5 + 0.5 * Math.sin(angle * 5 - frame.getPhase() * 2);
            peak *= peak;
            double radius = style.getRadius() * (0.88 + peak * 0.12) * frame.getScale();
            frame.emit(frame.alternatingParticle(index, 3), Math.cos(angle) * radius,
                    style.getVerticalOffset() + style.getHeight() * (0.15 + peak * 0.85),
                    Math.sin(angle) * radius);
        }
        frame.emit(style.getSecondaryParticle(), 0,
                style.getVerticalOffset() + style.getHeight() + 0.12, 0);
    }
}
