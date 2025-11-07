package pl.kiosel.villages.manager;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.core.locale.Locale;
import pl.kiosel.core.utils.PlayerUtils;
import pl.kiosel.dependencies.com.cryptomorin.xseries.XMaterial;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.turets.internal.*;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.enums.Upgrade;
import pl.kiosel.villages.data.village.turets.Turret;
import pl.kiosel.villages.data.village.level.Level;
import pl.kiosel.villages.data.village.turets.worldedit.TurretSetWE;
import pl.kiosel.villages.data.village.turets.worldedit.WorldEditTurret;
import pl.kiosel.villages.settings.Settings;
import pl.kiosel.villages.data.village.Village;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UpgradeManager {

    private final AdvancedVillages plugin;
    private final Map<Upgrade, Turret> turretMap = new HashMap<>();
	private WorldEditTurret worldEditTurret;

    public UpgradeManager(AdvancedVillages plugin) {
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

	public boolean canUpgrade(Player player, Level level) {
		int costEco = level.getCostEconomy();
		int costXp = level.getCostExperience();
		List<ItemStack> costMaterial = level.getItemMaterials();
		Locale locale = plugin.getLocale();

		boolean ecoEnabled = Settings.VILLAGE_UPGRADE_ECO.getBoolean();
		boolean xpEnabled = Settings.VILLAGE_UPGRADE_XP.getBoolean();
		boolean itemsEnabled = Settings.VILLAGE_UPGRADE_ITEMS.getBoolean();

		boolean hasMoney = !ecoEnabled || plugin.getApi().hasMoney(player, costEco);
		boolean hasXp = !xpEnabled || PlayerUtils.getTotalExperience(player) >= costXp;
		boolean hasItems = !itemsEnabled || PlayerUtils.hasEnoughItems(player, costMaterial);

		if (!hasMoney || !hasXp || !hasItems) {
			locale.getMessage(Lang.VILLAGE_NO_REQ_UPGRADE.getPath()).sendPrefixedMessage(player);

			if (!hasMoney)
				locale.getMessage(Lang.NO_MONEY.getPath()).sendPrefixedMessage(player);

			if (!hasXp)
				locale.getMessage(Lang.NO_XP.getPath())
						.processPlaceholder("xp", costXp)
						.sendPrefixedMessage(player);

			if (!hasItems) {
				locale.getMessage(Lang.NO_ITEMS.getPath()).sendPrefixedMessage(player);
				Map<XMaterial, Integer> missing = PlayerUtils.getMissingItems(player, costMaterial);
				for (Map.Entry<XMaterial, Integer> entry : missing.entrySet()) {
					player.sendMessage("  §7• §f" + entry.getKey().name() + " §cx" + entry.getValue());
				}
			}

			player.closeInventory();
			return false;
		}

		if (ecoEnabled) {
			plugin.getApi().removeMoney(player, costEco);
			locale.getMessage(Lang.MONEY_REMOVE.getPath())
					.processPlaceholder("money", costEco)
					.sendPrefixedMessage(player);
		}

		if (xpEnabled) {
			PlayerUtils.removeExperience(player, costXp);
			locale.getMessage(Lang.TAKE_XP.getPath())
					.processPlaceholder("xp", costXp)
					.sendPrefixedMessage(player);
		}

		if (itemsEnabled) {
			PlayerUtils.removeItem(player, costMaterial);
			for (ItemStack stack : costMaterial) {
				locale.getMessage(Lang.TAKE_ITEMS.getPath())
						.processPlaceholder("item", stack.getType().name())
						.processPlaceholder("amount", stack.getAmount())
						.sendPrefixedMessage(player);
			}
		}
		return true;
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
		plugin.getServer().getScheduler().runTaskLater(plugin, () -> upgrade(village, village.getLevel().getLevel()), 4L);
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
		return AdvancedVillages.getInstance().getLevelManager().getLevel(level).getCostEconomy();
	}
}