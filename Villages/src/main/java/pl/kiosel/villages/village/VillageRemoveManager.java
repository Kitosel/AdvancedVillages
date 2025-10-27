package pl.kiosel.villages.village;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.TNTPrimed;
import pl.kiosel.villages.Wioski;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class VillageRemoveManager {

	private final Wioski plugin;
	private final Random random = new Random();

	public VillageRemoveManager(Wioski plugin) {
		this.plugin = plugin;
	}

	public void removeVillage(Village village, boolean animation) {
		Location location = village.getLocation().clone();
		if (animation)
			summonTntAnimation(location);
		DataManager manager = plugin.getPlayerDataManager();

		for (UUID memberName : new ArrayList<>(village.getMembers())) {
			manager.removePlayerMember(memberName);
			plugin.getUserManager().removeUserFromVillage(village, memberName);
		}

		VillageMember ownerMember = manager.getVillageMember(village.getOwnerUUID());
		if (ownerMember != null) {
			ownerMember.setOwner(null);
			ownerMember.setVillage(null);
		}

		long delay = animation ? 50L : 5L;

		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			location.getWorld().spawnParticle(Particle.EXPLOSION, location, 2);
			plugin.getUpgradeManager().remove(village);
			plugin.getVillageManager().deleteVillage(village);
			manager.getVillages().remove(village.getVillageName());
			manager.getVillages().remove(village.getOwner());
			manager.removeVillage(village);
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
}
