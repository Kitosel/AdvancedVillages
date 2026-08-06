package pl.kiosel.villages.addons.trials.animation.pattern;

import pl.kiosel.villages.addons.trials.animation.CentralAnimationFrame;

public interface AnimationPattern {

    String name();

    void render(CentralAnimationFrame frame);
}
