package pl.kiosel.villages.addons.trials.animation;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.Village;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class CentralBlockAnimator {

    private final AdvancedVillages plugin;
    private final VillageAnimationSettings.Central settings;
    private final VillageAnimationSettings.Upgraded upgraded;
    private final Predicate<Village> canAnimate;
    private final CentralAnimationPatterns patterns = new CentralAnimationPatterns();
    private final double viewDistanceSquared;

    private BukkitTask task;
    private long animationTick;

    public CentralBlockAnimator(AdvancedVillages plugin, VillageAnimationSettings.Central settings,
                                VillageAnimationSettings.Upgraded upgraded, Predicate<Village> canAnimate) {
        this.plugin = plugin;
        this.settings = settings;
        this.upgraded = upgraded;
        this.canAnimate = canAnimate;
        this.viewDistanceSquared = settings.getViewDistance() * settings.getViewDistance();
    }

    public void start() {
        if (task != null) {
            return;
        }
        task = new BukkitRunnable() {
            @Override
            public void run() {
                renderLoadedVillages();
            }
        }.runTaskTimer(plugin, settings.getIntervalTicks(), settings.getIntervalTicks());
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        animationTick = 0;
    }

    public void playUpgrade(Village village) {
        LevelAnimationStyle style = styleFor(village);
        Location center = village.getAnimation();
        World world = center.getWorld();
        if (world == null || !style.isEnabled()) {
            return;
        }
        upgraded.getSound().play(village.getLocation().get(), upgraded.getVolume(), upgraded.getPitch());

        new BukkitRunnable() {
            private int tick;

            @Override
            public void run() {
                if (!canAnimate.test(village) || center.getWorld() == null) {
                    cancel();
                    return;
                }

                List<Player> viewers = nearbyViewers(world, center);
                if (!viewers.isEmpty()) {
                    double progress = tick / (double) settings.getUpgradeDurationTicks();
                    double pulse = Math.sin(progress * Math.PI);
                    render(center, viewers, style, tick * style.getSpeed() * 1.8,
                            2, 1.0 + pulse * 0.45);
                }

                double progress = tick / (double) upgraded.getDurationTicks();
                double radius = upgraded.getRadius() * (0.2 + progress);
                for (int index = 0; index < 3; index++) {
                    double angle = progress * Math.PI * 4 + index * Math.PI * 2 / 3;
                    Location point = center.clone().add(
                            Math.cos(angle) * radius,
                            0.15 + progress * 1.6,
                            Math.sin(angle) * radius
                    );
                    upgraded.getParticle().spawn(point, upgraded.getCount(), 0, 0, 0, 0);
                }

                tick += 2;
                if (tick > settings.getUpgradeDurationTicks()) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void renderLoadedVillages() {
        if (plugin.getVillageManager() == null) {
            return;
        }

        for (Village village : plugin.getVillageManager().getVillagesView()) {
            if (!canAnimate.test(village) || village.getLocation().isEmpty()) {
                continue;
            }

            Location center = village.getAnimation();
            World world = center.getWorld();
            if (world == null || !world.isChunkLoaded(center.getBlockX() >> 4, center.getBlockZ() >> 4)) {
                continue;
            }

            List<Player> viewers = nearbyViewers(world, center);
            LevelAnimationStyle style = styleFor(village);
            if (viewers.isEmpty() || !style.isEnabled()) {
                continue;
            }

            double phase = animationTick * style.getSpeed() % (Math.PI * 2);
            render(center, viewers, style, phase, 1, 1.0);
        }
        animationTick++;
    }

    private void render(Location center, List<Player> viewers, LevelAnimationStyle style,
                        double phase, int multiplier, double scale) {
        int count = Math.max(1, Math.min(100, style.getCount() * multiplier));
        patterns.render(style.getPattern(),
                new CentralAnimationFrame(center, viewers, style, phase, count, scale));
    }

    private LevelAnimationStyle styleFor(Village village) {
        int level = village.getLevel() == null ? 1 : village.getLevel().getLevel();
        return settings.styleFor(level);
    }

    private List<Player> nearbyViewers(World world, Location center) {
        List<Player> viewers = new ArrayList<>();
        for (Player player : world.getPlayers()) {
            Location location = player.getLocation();
            double x = location.getX() - center.getX();
            double y = location.getY() - center.getY();
            double z = location.getZ() - center.getZ();
            if (x * x + y * y + z * z <= viewDistanceSquared) {
                viewers.add(player);
            }
        }
        return viewers;
    }
}
