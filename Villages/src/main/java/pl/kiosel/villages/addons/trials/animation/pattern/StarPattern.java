package pl.kiosel.villages.addons.trials.animation.pattern;

import pl.kiosel.villages.addons.trials.animation.CentralAnimationFrame;
import pl.kiosel.villages.addons.trials.animation.LevelAnimationStyle;

public final class StarPattern implements AnimationPattern {

    private static final int VERTICES = 10;

    @Override
    public String name() {
        return "STAR";
    }

    @Override
    public void render(CentralAnimationFrame frame) {
        LevelAnimationStyle style = frame.getStyle();
        for (int index = 0; index < frame.getCount(); index++) {
            double path = index * VERTICES / (double) frame.getCount();
            int from = (int) Math.floor(path);
            int to = (from + 1) % VERTICES;
            double progress = path - from;
            double startX = vertexX(from, style.getRadius(), frame.getPhase());
            double startZ = vertexZ(from, style.getRadius(), frame.getPhase());
            double endX = vertexX(to, style.getRadius(), frame.getPhase());
            double endZ = vertexZ(to, style.getRadius(), frame.getPhase());
            double x = (startX + (endX - startX) * progress) * frame.getScale();
            double z = (startZ + (endZ - startZ) * progress) * frame.getScale();
            double y = style.getVerticalOffset() + style.getHeight()
                    * (0.5 + 0.5 * Math.sin(path * Math.PI + frame.getPhase()));
            frame.emit(frame.alternatingParticle(index, 3), x, y, z);
        }
    }

    private double vertexX(int index, double radius, double phase) {
        double vertexRadius = index % 2 == 0 ? radius : radius * 0.42;
        double angle = phase + index * Math.PI / 5 - Math.PI / 2;
        return Math.cos(angle) * vertexRadius;
    }

    private double vertexZ(int index, double radius, double phase) {
        double vertexRadius = index % 2 == 0 ? radius : radius * 0.42;
        double angle = phase + index * Math.PI / 5 - Math.PI / 2;
        return Math.sin(angle) * vertexRadius;
    }
}
