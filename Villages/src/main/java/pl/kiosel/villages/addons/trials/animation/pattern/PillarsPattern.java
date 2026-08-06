package pl.kiosel.villages.addons.trials.animation.pattern;

import pl.kiosel.villages.addons.trials.animation.CentralAnimationFrame;
import pl.kiosel.villages.addons.trials.animation.LevelAnimationStyle;

public final class PillarsPattern implements AnimationPattern {

    private static final int PILLARS = 4;

    @Override
    public String name() {
        return "PILLARS";
    }

    @Override
    public void render(CentralAnimationFrame frame) {
        LevelAnimationStyle style = frame.getStyle();
        int rows = Math.max(2, (int) Math.ceil(frame.getCount() / (double) PILLARS));
        double movement = frame.getPhase() / (Math.PI * 2);
        for (int index = 0; index < frame.getCount(); index++) {
            int pillar = index % PILLARS;
            int row = index / PILLARS;
            double progress = (row / (double) rows + movement) % 1.0;
            double angle = pillar * Math.PI / 2 + frame.getPhase() * 0.25;
            frame.emit(frame.alternatingParticle(pillar, 2),
                    Math.cos(angle) * style.getRadius() * frame.getScale(),
                    style.getVerticalOffset() + progress * style.getVerticalSpan(),
                    Math.sin(angle) * style.getRadius() * frame.getScale());
        }
    }
}
