package pl.kiosel.villages.village;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import pl.kiosel.villages.Wioski;
import pl.kiosel.villages.config.Config;
import pl.kiosel.villages.enums.Upgrade;
import pl.kiosel.villages.models.turets.*;
import pl.kiosel.villages.models.turets.internal.*;
import pl.kiosel.villages.models.turets.worldedit.TurretSetWE;
import pl.kiosel.villages.models.turets.worldedit.WorldEditTurret;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UpgradeManager {

    private final Wioski plugin;
    private final Map<Upgrade, Turret> turretMap = new HashMap<>();
	private WorldEditTurret worldEditTurret;

    public UpgradeManager(Wioski plugin) {
        this.plugin = plugin;
        setTurret();
		if (plugin.isWorldedit())
			worldEditTurret = new WorldEditTurret(plugin);
    }

    private void setTurret() {
        turretMap.put(Upgrade.IRON, new TurretSetOak());
        turretMap.put(Upgrade.GOLD, new TurretSetBrick());
        turretMap.put(Upgrade.EMERALD, new TurretSetNether());
        turretMap.put(Upgrade.DIAMOND, new TurretSetPrismarine());
        turretMap.put(Upgrade.NETHERITE, new TurretSetEnd());
		turretMap.put(Upgrade.RESET, new TurretReset());
		turretMap.put(Upgrade.WORLDEDIT, new TurretSetWE());
    }

    public void upgrade(Village village) {
        upgrade(village, Upgrade.getByLevel(1));
    }

    public void upgrade(Village village, Upgrade upgrade) {
        Location location = village.getLocation();
        reset(village);

		if (plugin.isWorldedit()) {
			worldEditTurret.pasteVillage(location, upgrade.getLevel());
			plugin.getServer().getScheduler().runTaskLater(plugin, () ->
					turretMap.get(Upgrade.WORLDEDIT).setTurret(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ()),
			2);
		} else {
			turretMap.get(upgrade).setTurret(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
		}
    }

    public void reset(Village village) {
        ((TurretReset) turretMap.get(Upgrade.RESET)).setAir(village.getLocation());
    }

    public void remove(Village village) {
        ((TurretReset) turretMap.get(Upgrade.RESET)).removeVillage(village.getLocation());
    }

	public void upgradeVillage(Village village) {
		village.upgrade();
		Location loc = village.getLocation();
		Objects.requireNonNull(loc.getWorld()).spawnParticle(Particle.FLAME, loc, 50, 1, 1, 1);
		loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 10, 1);
		plugin.getServer().getScheduler().runTaskLater(plugin, () -> upgrade(village, village.getLevel()), 4L);
	}

	public void upgrade(Village village, int level) {
		switch (level) {
			case 1:
				upgrade(village, Upgrade.IRON);
				break;
			case 2:
				upgrade(village, Upgrade.GOLD);
				break;
			case 3:
				upgrade(village, Upgrade.EMERALD);
				break;
			case 4:
				upgrade(village, Upgrade.DIAMOND);
				break;
			case 5:
				upgrade(village, Upgrade.NETHERITE);
				break;
		}
	}

	public static int getCostForLevel(int level) {
		int cost = 0;
		switch (level) {
			case 1:
				cost = Config.cost_set;
				break;
			case 2:
				cost = Config.cost_upgrade_1;
				break;
			case 3:
				cost = Config.cost_upgrade_2;
				break;
			case 4:
				cost = Config.cost_upgrade_3;
				break;
			case 5:
				cost = Config.cost_upgrade_4;
				break;
		}
		return cost;
	}

	public static int getSizeForLevel(int level) {
		int cost = 0;
		switch (level) {
			case 1:
				cost = Config.size_set;
				break;
			case 2:
				cost = Config.size_upgrade_1;
				break;
			case 3:
				cost = Config.size_upgrade_2;
				break;
			case 4:
				cost = Config.size_upgrade_3;
				break;
			case 5:
				cost = Config.size_upgrade_4;
				break;
		}
		return cost;
	}
}