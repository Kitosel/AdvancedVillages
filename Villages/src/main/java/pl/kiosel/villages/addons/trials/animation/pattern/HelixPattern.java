package pl.kiosel.villages.addons.trials.animation.pattern;

import pl.kiosel.rosacore.compatibility.ZParticle;
import pl.kiosel.villages.addons.trials.animation.CentralAnimationFrame;
import pl.kiosel.villages.addons.trials.animation.LevelAnimationStyle;

public final class HelixPattern implements AnimationPattern {

    @Override
    public String name() {
        return "HELIX";
    }

    @Override
    public void render(CentralAnimationFrame frame) {
        LevelAnimationStyle style = frame.getStyle();
        int perStrand = Math.max(4, frame.getCount() / 2);
        double movement = frame.getPhase() / (Math.PI * 2);
        for (int strand = 0; strand < 2; strand++) {
            ZParticle particle = strand == 0 ? style.getParticle() : style.getSecondaryParticle();
            for (int index = 0; index < perStrand; index++) {
                double progress = (index / (double) perStrand + movement) % 1.0;
                double angle = progress * Math.PI * 4 + strand * Math.PI;
                double radius = style.getRadius() * (0.82 + Math.sin(progress * Math.PI) * 0.18)
                        * frame.getScale();
                frame.emit(particle, Math.cos(angle) * radius,
                        style.getVerticalOffset() + progress * style.getVerticalSpan(),
                        Math.sin(angle) * radius);
            }
        }
    }
}
