package pl.kiosel.villages.addons.trials.animation;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.Village;

import java.util.function.Predicate;

public final class CreationAnimator {

    private final AdvancedVillages plugin;
    private final VillageAnimationSettings.Creation settings;
    private final Predicate<Village> canAnimate;

    public CreationAnimator(AdvancedVillages plugin, VillageAnimationSettings.Creation settings,
                            Predicate<Village> canAnimate) {
        this.plugin = plugin;
        this.settings = settings;
        this.canAnimate = canAnimate;
    }

    public void play(Village village) {
        Location center = village.getAnimation();
        settings.getSound().play(village.getLocation().get(), settings.getVolume(), settings.getPitch());
        new BukkitRunnable() {
            private int tick;

            @Override
            public void run() {
                if (!canAnimate.test(village) || center.getWorld() == null) {
                    cancel();
                    return;
                }

                double progress = tick / (double) settings.getDurationTicks();
                double radius = settings.getRadius() * (0.2 + progress);
                for (int index = 0; index < 3; index++) {
                    double angle = progress * Math.PI * 4 + index * Math.PI * 2 / 3;
                    Location point = center.clone().add(
                            Math.cos(angle) * radius,
                            0.15 + progress * 1.6,
                            Math.sin(angle) * radius
                    );
	                settings.getParticle().spawn(point, settings.getCount(), 0, 0, 0, 0);
                }

                tick++;
                if (tick > settings.getDurationTicks()) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
