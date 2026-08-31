package pl.kiosel.villages.addons.trials.animation;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.compatibility.ZParticle;

import java.util.List;

public final class CentralAnimationFrame {

    private final Location center;
    private final List<Player> viewers;
    @Getter
    private final LevelAnimationStyle style;
    @Getter
    private final double phase;
    @Getter
    private final int count;
    @Getter
    private final double scale;

    CentralAnimationFrame(Location center, List<Player> viewers, LevelAnimationStyle style,
                          double phase, int count, double scale) {
        this.center = center;
        this.viewers = viewers;
        this.style = style;
        this.phase = phase;
        this.count = count;
        this.scale = scale;
    }

	public ZParticle alternatingParticle(int index, int secondaryEvery) {
        return index % secondaryEvery == 0 ? style.getSecondaryParticle() : style.getParticle();
    }

    public void emit(ZParticle particle, double offsetX, double offsetY, double offsetZ) {
        double x = center.getX() + offsetX;
        double y = center.getY() + offsetY;
        double z = center.getZ() + offsetZ;
        for (Player viewer : viewers) {
            if (particle.getName() != null)
                viewer.spawnParticle(Particle.valueOf(particle.getName()), x, y, z, 1, 0, 0, 0, 0);
        }
    }
}
