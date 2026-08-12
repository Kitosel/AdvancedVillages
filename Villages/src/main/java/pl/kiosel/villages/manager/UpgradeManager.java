package pl.kiosel.villages.manager;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.core.utils.PlayerUtils;
import pl.kiosel.dependencies.com.cryptomorin.xseries.XMaterial;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.VillageMessages;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.level.Level;
import pl.kiosel.villages.data.village.turets.Turret;
import pl.kiosel.villages.data.village.turets.internal.*;
import pl.kiosel.villages.data.village.turets.worldedit.TurretSetWE;
import pl.kiosel.villages.data.village.turets.worldedit.WorldEditTurret;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.enums.Upgrade;
import pl.kiosel.villages.settings.Settings;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpgradeManager {

    private final AdvancedVillages plugin;
    private final Map<Upgrade, Turret> turretMap = new HashMap<>();
	private WorldEditTurret worldEditTurret;

    public UpgradeManager(AdvancedVillages plugin) {
        this.plugin = plugin;
        setTurret();
		this.refreshWorldEditIntegration();
    }

	public void refreshWorldEditIntegration() {
		this.worldEditTurret = this.plugin.isWorldedit()
				? new WorldEditTurret(this.plugin)
				: null;
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
		VillageMessages messages = plugin.getMessages();

		boolean ecoEnabled = Settings.VILLAGE_UPGRADE_ECO.getBoolean();
		boolean xpEnabled = Settings.VILLAGE_UPGRADE_XP.getBoolean();
		boolean itemsEnabled = Settings.VILLAGE_UPGRADE_ITEMS.getBoolean();

		boolean hasMoney = !ecoEnabled || plugin.getEconomy().hasBalance(player, costEco);
		boolean hasXp = !xpEnabled || PlayerUtils.getTotalExperience(player) >= costXp;
		boolean hasItems = !itemsEnabled || PlayerUtils.hasEnoughItems(player, costMaterial);

		if (!hasMoney || !hasXp || !hasItems) {
			messages.sendPrefixed(player, Lang.VILLAGE_NO_REQ_UPGRADE);

			if (!hasMoney) {
				double more_money = costEco - plugin.getEconomy().getBalance(player);
				messages.sendPrefixed(player, Lang.NO_MONEY, "money", more_money);
			}

			if (!hasXp)
				messages.sendPrefixed(player, Lang.NO_XP, "xp", costXp);

			if (!hasItems) {
				messages.sendPrefixed(player, Lang.NO_ITEMS);
				Map<XMaterial, Integer> missing = PlayerUtils.getMissingItems(player, costMaterial);
				for (Map.Entry<XMaterial, Integer> entry : missing.entrySet()) {
					messages.send(player, Lang.MISSING_ITEM,
							"item", entry.getKey().name(),
							"amount", entry.getValue());
				}
			}

			player.closeInventory();
			return false;
		}

		if (ecoEnabled) {
			if (!plugin.getEconomy().withdrawBalance(player, costEco)) {
				messages.sendPrefixed(player, Lang.NO_MONEY, "money", costEco);
				return false;
			}
			messages.sendPrefixed(player, Lang.MONEY_REMOVE, "money", costEco);
		}

		if (xpEnabled) {
			PlayerUtils.removeExperience(player, costXp);
			messages.sendPrefixed(player, Lang.TAKE_XP, "xp", costXp);
		}

		if (itemsEnabled) {
			PlayerUtils.removeItem(player, costMaterial);
			for (ItemStack stack : costMaterial) {
				messages.sendPrefixed(player, Lang.TAKE_ITEMS,
						"item", stack.getType().name(),
						"amount", stack.getAmount());
			}
		}
		return true;
	}

    public void upgrade(Village village) {
        upgrade(village, Upgrade.getByLevel(1));
    }

    public void upgrade(Village village, Upgrade upgrade) {
        Location location = village.getLocation().get();
		if (!canPasteLevel(upgrade.getLevel())) {
			plugin.getLogger().warning("Cannot paste village level " + upgrade.getLevel() + ": compatible turret build is missing.");
			return;
		}
        reset(village);

		if (this.canUseWorldEdit()) {
			worldEditTurret.pasteVillage(location, upgrade.getLevel());
			plugin.getServer().getScheduler().runTaskLater(plugin, () ->
					turretMap.get(Upgrade.WORLDEDIT).setTurret(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ()),
			2);
		} else {
			turretMap.get(upgrade).setTurret(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
		}
    }

    public void reset(Village village) {
        ((TurretReset) turretMap.get(Upgrade.RESET)).setAir(village.getLocation().get());
    }

    public void remove(Village village) {
        ((TurretReset) turretMap.get(Upgrade.RESET)).removeVillage(village.getLocation().get());
    }

	public boolean upgradeVillage(Village village) {
		Level nextLevel = plugin.getLevelManager().getLevel(village.getLevel().getLevel() + 1);
		if (nextLevel == null || !canPasteLevel(nextLevel.getLevel())) {
			return false;
		}
		village.setLevel(nextLevel);
		village.getRegion().get().setSize(nextLevel.getSize());
		plugin.getVillageAnimationManager().playLevelUpgrade(village);
		plugin.getServer().getScheduler().runTaskLater(plugin, () -> upgrade(village, village.getLevel().getLevel()), 4L);
		return true;
	}

	public boolean canPasteLevel(int level) {
		if (this.canUseWorldEdit()) {
			return new File(plugin.getDataFolder(), "schematics/Turret" + level + ".schem").isFile();
		}
		Upgrade upgrade = Upgrade.getByLevel(level);
		return upgrade.getLevel() == level && turretMap.containsKey(upgrade);
	}

	public void upgrade(Village village, int level) {
		upgrade(village, Upgrade.getByLevel(level));
	}

	public static int getCostForLevel(int level) {
		Level configured = AdvancedVillages.getInstance().getLevelManager().getLevel(level);
		return configured == null ? 0 : configured.getCostEconomy();
	}

	public static int getSizeForLevel(int level) {
		Level configured = AdvancedVillages.getInstance().getLevelManager().getLevel(level);
		return configured == null ? 0 : configured.getSize();
	}

	private boolean canUseWorldEdit() {
		return this.plugin.isWorldedit() && this.worldEditTurret != null;
	}
}
