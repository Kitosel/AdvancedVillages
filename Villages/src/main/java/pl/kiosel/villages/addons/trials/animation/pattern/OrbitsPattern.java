package pl.kiosel.villages.addons.trials.animation.pattern;

import pl.kiosel.dependencies.com.cryptomorin.xseries.particles.XParticle;
import pl.kiosel.villages.addons.trials.animation.CentralAnimationFrame;
import pl.kiosel.villages.addons.trials.animation.LevelAnimationStyle;

public final class OrbitsPattern implements AnimationPattern {

    @Override
    public String name() {
        return "ORBITS";
    }

    @Override
    public void render(CentralAnimationFrame frame) {
        LevelAnimationStyle style = frame.getStyle();
        int perOrbit = Math.max(3, frame.getCount() / 3);
        for (int orbit = 0; orbit < 3; orbit++) {
            double yaw = orbit * Math.PI * 2 / 3;
            XParticle particle = orbit == 1 ? style.getSecondaryParticle() : style.getParticle();
            for (int index = 0; index < perOrbit; index++) {
                double angle = Math.PI * 2 * index / perOrbit
                        + frame.getPhase() * (orbit == 1 ? -1 : 1);
                double longAxis = Math.cos(angle) * style.getRadius() * frame.getScale();
                double shortAxis = Math.sin(angle) * style.getRadius() * 0.34 * frame.getScale();
                double x = longAxis * Math.cos(yaw) - shortAxis * Math.sin(yaw);
                double z = longAxis * Math.sin(yaw) + shortAxis * Math.cos(yaw);
                double y = style.getVerticalOffset()
                        + style.getVerticalSpan() * (0.5 + 0.5 * Math.sin(angle));
                frame.emit(particle, x, y, z);
            }
        }

        double pulse = 0.06 + (0.5 + 0.5 * Math.sin(frame.getPhase() * 3)) * 0.08;
        for (int index = 0; index < 4; index++) {
            double angle = frame.getPhase() * 2 + index * Math.PI / 2;
            frame.emit(style.getSecondaryParticle(), Math.cos(angle) * pulse,
                    style.getVerticalOffset() + style.getVerticalSpan() * 0.5,
                    Math.sin(angle) * pulse);
        }
    }
}
