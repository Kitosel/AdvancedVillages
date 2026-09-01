package pl.kiosel.villages.manager;

import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import pl.kiosel.rosacore.compatibility.ZParticle;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.storage.DatabaseVillageSerializer;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class VillageRemoveManager {

	private final AdvancedVillages plugin;

	public VillageRemoveManager(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	public void destroyVillage(Village village, boolean animation) {
		Location location = village.getLocation().get().clone();
		boolean playAnimation = animation && plugin.getVillageAnimationManager().isEnabledFor(village);
		if (playAnimation)
			summonFireworkAnimation(location);

		if (location.getWorld()==null) return;
		if (playAnimation) {
			ZParticle.SMOKE.spawn(location, 2);
			ZParticle.ASH.spawn(location, 5);
		}
	}

	public void removeVillage(Village village, boolean animation) {
		Location location = village.getLocation().get().clone();
		long animationDuration = plugin.getVillageAnimationManager().playRemoval(village, animation);
		plugin.getVillageManager().deleteVillage(plugin, village);

		long delay = animationDuration > 0 ? animationDuration : 5L;

		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			if (animationDuration == 0 && location.getWorld() != null) {
				ZParticle.EXPLOSION.spawn(location, 2);
			}
			plugin.getUpgradeManager().remove(village);
			plugin.getVillageManager().deleteVillage(village);
			DatabaseVillageSerializer.delete(village);
		}, delay);
	}

	private void summonFireworkAnimation(Location base) {
		Location location = base.clone().add(
				ThreadLocalRandom.current().nextDouble() * 0.5,
				0.5,
				ThreadLocalRandom.current().nextDouble() * 0.5
		);

		Firework firework = Objects.requireNonNull(location.getWorld()).spawn(location, Firework.class);
		FireworkMeta fireworkMeta = firework.getFireworkMeta();
		fireworkMeta.addEffect(FireworkEffect.builder().withColor(Color.RED).withFade(Color.PURPLE).with(FireworkEffect.Type.BALL_LARGE).withFlicker().build());
		fireworkMeta.setPower(2);
		firework.setFireworkMeta(fireworkMeta);
	}
}
