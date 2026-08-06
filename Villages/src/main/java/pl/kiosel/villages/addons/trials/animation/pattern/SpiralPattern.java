package pl.kiosel.villages.addons.trials.animation.pattern;

import pl.kiosel.villages.addons.trials.animation.CentralAnimationFrame;
import pl.kiosel.villages.addons.trials.animation.LevelAnimationStyle;

public final class SpiralPattern implements AnimationPattern {

    @Override
    public String name() {
        return "SPIRAL";
    }

    @Override
    public void render(CentralAnimationFrame frame) {
        LevelAnimationStyle style = frame.getStyle();
        int count = Math.max(2, frame.getCount());
        for (int index = 0; index < count; index++) {
            double progress = index / (double) (count - 1);
            double angle = frame.getPhase() + progress * Math.PI * 4;
            double radius = style.getRadius() * progress * frame.getScale();
            double y = style.getVerticalOffset() + style.getHeight() * progress
                    + Math.sin(angle + frame.getPhase()) * style.getHeight() * 0.08;
            frame.emit(frame.alternatingParticle(index, 4),
                    Math.cos(angle) * radius, y, Math.sin(angle) * radius);
        }
    }
}
