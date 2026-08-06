package pl.kiosel.villages.addons.trials.animation.pattern;

import pl.kiosel.villages.addons.trials.animation.CentralAnimationFrame;
import pl.kiosel.villages.addons.trials.animation.LevelAnimationStyle;

public final class HaloPattern implements AnimationPattern {

    @Override
    public String name() {
        return "HALO";
    }

    @Override
    public void render(CentralAnimationFrame frame) {
        LevelAnimationStyle style = frame.getStyle();
        int count = frame.getCount();
        double phase = frame.getPhase();
        for (int index = 0; index < count; index++) {
            double angle = Math.PI * 2 * index / count + phase;
            double wave = 0.5 + 0.5 * Math.sin(angle * 2 - phase);
            frame.emit(frame.alternatingParticle(index, 4),
                    Math.cos(angle) * style.getRadius() * frame.getScale(),
                    style.getVerticalOffset() + style.getHeight() * wave,
                    Math.sin(angle) * style.getRadius() * frame.getScale());
        }

        double wispAngle = phase * 1.7;
        frame.emit(style.getSecondaryParticle(),
                Math.cos(wispAngle) * style.getRadius() * 0.35 * frame.getScale(),
                style.getVerticalOffset() + style.getHeight() + 0.18,
                Math.sin(wispAngle) * style.getRadius() * 0.35 * frame.getScale());
    }
}
