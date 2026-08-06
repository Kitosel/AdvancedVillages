package pl.kiosel.villages.api;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import pl.kiosel.core.dependencies.de.tr7zw.nbtapi.NBT;
import pl.kiosel.core.dependencies.de.tr7zw.nbtapi.iface.ReadWriteItemNBT;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.trials.VillageAnimationManager;
import pl.kiosel.villages.addons.buildeditor.VillageBuildEditorManager;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.VillageManager;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.manager.UpgradeManager;
import pl.kiosel.villages.manager.VillageRemoveManager;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class VillageAPI {

    private final AdvancedVillages plugin;

    public VillageAPI(AdvancedVillages plugin) {
        this.plugin = plugin;
    }

	@NotNull
    public AdvancedVillages getPlugin() {
        return plugin;
    }

    @NotNull
    public VillageManager getVillageManager() {
        return plugin.getVillageManager();
    }

    @NotNull
    public VillageRemoveManager getVillageRemoveManager() {
        return plugin.getVillageRemoveManager();
    }

    @NotNull
    public UpgradeManager getUpgradeManager() {
        return plugin.getUpgradeManager();
    }

    @NotNull
	public VillageAnimationManager getAnimationManager() {
		return plugin.getVillageAnimationManager();
	}

	@Nullable
	public VillageBuildEditorManager getBuildEditorManager() {
		return plugin.getVillageBuildEditorManager();
	}

	@Nullable
	public Village getVillage(String playerName) {
		return plugin.getUserManager().findByName(playerName)
				.map(User::getPresentVillage)
				.orNull();
	}

	@Nullable
	public Village getVillage(Player player) {
		return getVillage(player.getUniqueId());
	}

	@Nullable
	public Village getVillage(UUID uuid) {
		return plugin.getUserManager().findByUuid(uuid)
				.map(User::getPresentVillage)
				.orNull();
	}

	@Nullable
	public Village getVillageAt(Location location) {
		return location == null ? null : plugin.getVillageUtilsManager().getVillageAt(location);
	}

	public boolean isVillageAt(Location location) {
		return getVillageAt(location) != null;
	}

    public ItemStack createVillageBlock() {
		ItemStack item = Item.create(Material.NOTE_BLOCK,
				plugin.getLocale().getMessage(Lang.VILLAGE_BLOCK_NAME.getPath()).toString(),
				List.of(plugin.getLocale().getMessage(Lang.VILLAGE_BLOCK_LORE.getPath()).toString()), true);
		NBT.modify(item, (Consumer<ReadWriteItemNBT>) nbt -> nbt.setBoolean("villageBlock", true));
		return item;
    }

	public ItemStack createDestroyer() {
		return Item.createDestroyer(Material.GOLDEN_PICKAXE,
				plugin.getLocale().getMessage(Lang.VILLAGE_DESTROYER_NAME.getPath()).toString(),
				List.of(plugin.getLocale().getMessage(Lang.VILLAGE_DESTROYER_LORE.getPath()).toString()));
	}

	public ItemStack createHearth() {
		return Item.createNoPlaceNoCraft(Material.BARRIER,
				plugin.getLocale().getMessage(Lang.VILLAGE_HEARTH_BLOCK_NAME.getPath()).toString(),
				List.of(plugin.getLocale().getMessage(Lang.VILLAGE_HEARTH_BLOCK_LORE.getPath()).toString()));
	}

	public ItemStack createVillageHearth() {
		return Item.createNoPlaceNoCraft(Material.REDSTONE_BLOCK,
				plugin.getLocale().getMessage(Lang.VILLAGE_PART_HEARTH_BLOCK_NAME.getPath()).toString(),
				List.of(plugin.getLocale().getMessage(Lang.VILLAGE_PART_HEARTH_BLOCK_LORE.getPath()).toString()));
	}

	public ItemStack createDestroyerHearth() {
		return Item.createNoPlaceNoCraft(Material.GOLD_BLOCK,
				plugin.getLocale().getMessage(Lang.VILLAGE_DESTROYER_HEARTH_NAME.getPath()).toString(),
				List.of(plugin.getLocale().getMessage(Lang.VILLAGE_DESTROYER_HEARTH_LORE.getPath()).toString()));
	}
}
