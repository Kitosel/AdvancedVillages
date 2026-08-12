package pl.kiosel.villages.addons.trials.animation;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.Village;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public final class RemovalAnimator {

    private final AdvancedVillages plugin;
    private final VillageAnimationSettings.Removal settings;

    public RemovalAnimator(AdvancedVillages plugin, VillageAnimationSettings.Removal settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    public long play(Village village) {
        Location center = village.getAnimation();
        World world = center.getWorld();
        if (world == null) {
            return 0L;
        }

        int duration = settings.getDurationTicks();
        TNTPrimed tnt = world.spawn(center.clone().add(0.25, 0.1, 0.25), TNTPrimed.class);
        tnt.setYield(0);
        tnt.setSilent(true);
        tnt.setGlowing(true);
        tnt.setFireTicks(duration);
        tnt.setFuseTicks(duration);
        tnt.setIsIncendiary(false);
        tnt.setVelocity(new Vector(0, 0.65, 0));
        world.spawnParticle(Particle.SMOKE, center, 24, 0.3, 0.3, 0.3, 0.01);

        new BukkitRunnable() {
            private int tick;

            @Override
            public void run() {
                if (center.getWorld() == null) {
                    cancel();
                    return;
                }

                if (tick % settings.getBeepIntervalTicks() == 0) {
                    float pitch = (float) (settings.getBeepPitchStart()
                            + tick / (double) duration * settings.getBeepPitchRise());
                    playSound(center, settings.getBeepSound(), settings.getBeepVolume(), pitch);
                    world.spawnParticle(Particle.CRIT, center.clone().add(0, 0.8, 0),
                            8, 0.2, 0.2, 0.2, 0.01);
                }

                if (tick >= duration) {
                    world.spawnParticle(Particle.EXPLOSION, center, 3, 0.25, 0.25, 0.25, 0);
                    pushPlayers(center);
                    cancel();
                    return;
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        return duration;
    }

    private void pushPlayers(Location center) {
        World world = center.getWorld();
        if (world == null) {
            return;
        }

        double radiusSquared = settings.getKnockbackRadius() * settings.getKnockbackRadius();
        for (Player player : world.getPlayers()) {
            Vector direction = player.getLocation().toVector().subtract(center.toVector());
            if (direction.lengthSquared() > radiusSquared) {
                continue;
            }
            if (direction.lengthSquared() < 0.01) {
                direction = new Vector(ThreadLocalRandom.current().nextDouble(-1, 1), 0,
                        ThreadLocalRandom.current().nextDouble(-1, 1));
            }
            direction.normalize().multiply(settings.getKnockbackStrength()).setY(settings.getKnockbackY());
            player.setVelocity(direction);
        }
    }

    private void playSound(Location location, String soundName, float volume, float pitch) {
        Sound sound;
        try {
            sound = Sound.valueOf(soundName.toUpperCase(Locale.ROOT));
        } catch (Exception ignored) {
            return;
        }
        if (location.getWorld() != null) {
            location.getWorld().playSound(location, sound, Math.max(0, volume), Math.max(0, pitch));
        }
    }
}
