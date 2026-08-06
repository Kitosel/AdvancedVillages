package pl.kiosel.villages.manager;

import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.meta.FireworkMeta;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.storage.DatabaseVillageSerializer;

import java.sql.SQLException;
import java.util.Objects;
import java.util.Random;

public class VillageRemoveManager {

	private final AdvancedVillages plugin;
	private final Random random = new Random();

	public VillageRemoveManager(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	public void destroyVillage(Village village, boolean animation) {
		Location location = village.getLocation().get().clone();
		if (animation)
			summonFireworkAnimation(location);

		if (location.getWorld()==null) return;
		location.getWorld().spawnParticle(Particle.SMOKE, location, 2);
		location.getWorld().spawnParticle(Particle.ASH, location, 5);
	}

	public void removeVillage(Village village, boolean animation) {
		Location location = village.getLocation().get().clone();
		if (animation)
			summonTntAnimation(location);
		plugin.getVillageManager().deleteVillage(plugin, village);

		long delay = animation ? 50L : 5L;

		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			location.getWorld().spawnParticle(Particle.EXPLOSION, location, 2);
			plugin.getUpgradeManager().remove(village);
			plugin.getVillageManager().deleteVillage(village);
			try {
				DatabaseVillageSerializer.delete(village, plugin.getDataManager(), plugin.getDataloader().getVillagesTable());
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}, delay);
	}

	private void summonTntAnimation(Location base) {
		Location location = base.clone().add(
				random.nextDouble() * 0.4 + 0.1,
				0,
				random.nextDouble() * 0.4 + 0.1
		);

		TNTPrimed tnt = Objects.requireNonNull(location.getWorld()).spawn(location, TNTPrimed.class);
		tnt.setYield(0);
		tnt.setGlowing(true);
		tnt.setFireTicks(40);
		tnt.setFuseTicks(2 * 25);
		tnt.setIsIncendiary(false);
		tnt.setVelocity(location.getDirection().multiply(0).setY(0.7));

		location.getWorld().spawnParticle(Particle.SMOKE, location, 30, 0.3, 0.3, 0.3, 0.01);
	}

	private void summonFireworkAnimation(Location base) {
		Location location = base.clone().add(
				random.nextDouble() * 0.5,
				0.5,
				random.nextDouble() * 0.5
		);

		Firework firework = Objects.requireNonNull(location.getWorld()).spawn(location, Firework.class);
		FireworkMeta fireworkMeta = firework.getFireworkMeta();
		fireworkMeta.addEffect(FireworkEffect.builder().withColor(Color.RED).withFade(Color.PURPLE).with(FireworkEffect.Type.BALL_LARGE).withFlicker().build());
		fireworkMeta.setPower(2);
		firework.setFireworkMeta(fireworkMeta);
	}
}
