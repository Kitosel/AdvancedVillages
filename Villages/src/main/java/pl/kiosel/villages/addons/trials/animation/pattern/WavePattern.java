package pl.kiosel.villages.addons.trials.animation.pattern;

import pl.kiosel.dependencies.com.cryptomorin.xseries.particles.XParticle;
import pl.kiosel.villages.addons.trials.animation.CentralAnimationFrame;
import pl.kiosel.villages.addons.trials.animation.LevelAnimationStyle;

public final class WavePattern implements AnimationPattern {

    @Override
    public String name() {
        return "WAVE";
    }

    @Override
    public void render(CentralAnimationFrame frame) {
        LevelAnimationStyle style = frame.getStyle();
        int perWave = Math.max(4, frame.getCount() / 2);
        for (int wave = 0; wave < 2; wave++) {
            XParticle particle = wave == 0 ? style.getParticle() : style.getSecondaryParticle();
            double direction = wave == 0 ? 1 : -1;
            for (int index = 0; index < perWave; index++) {
                double angle = Math.PI * 2 * index / perWave + frame.getPhase() * direction;
                double y = style.getVerticalOffset() + style.getHeight()
                        * (0.5 + 0.5 * Math.sin(angle * 3 + frame.getPhase() + wave * Math.PI));
                frame.emit(particle,
                        Math.cos(angle) * style.getRadius() * frame.getScale(), y,
                        Math.sin(angle) * style.getRadius() * frame.getScale());
            }
        }
    }
}
