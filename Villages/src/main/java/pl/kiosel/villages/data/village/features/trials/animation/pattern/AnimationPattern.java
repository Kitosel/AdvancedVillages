package pl.kiosel.villages.data.village.features.trials.animation.pattern;

import pl.kiosel.villages.data.village.features.trials.animation.CentralAnimationFrame;

public interface AnimationPattern {

    String name();

    void render(CentralAnimationFrame frame);
}
