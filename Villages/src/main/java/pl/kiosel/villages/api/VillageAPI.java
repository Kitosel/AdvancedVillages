package pl.kiosel.villages.api;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import pl.kiosel.rosacore.dependencies.nbtapi.NBT;
import pl.kiosel.rosacore.dependencies.nbtapi.iface.ReadWriteItemNBT;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.buildeditor.VillageBuildEditorManager;
import pl.kiosel.villages.addons.trials.VillageAnimationManager;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.UserManager;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.VillageManager;
import pl.kiosel.villages.data.village.features.diplomacy.DiplomacyManager;
import pl.kiosel.villages.data.village.features.logs.VillageLogManager;
import pl.kiosel.villages.data.village.features.quest.VillageQuestManager;
import pl.kiosel.villages.data.village.features.ranking.RankingManager;
import pl.kiosel.villages.data.village.features.rent.VillageUpkeepManager;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.manager.RoleManager;
import pl.kiosel.villages.manager.SpecializationManager;
import pl.kiosel.villages.manager.UpgradeManager;
import pl.kiosel.villages.manager.VillageRemoveManager;
import pl.kiosel.villages.manager.teleport.TeleportManager;

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
	public UserManager getUserManager() {
		return plugin.getUserManager();
	}

	@NotNull
	public RoleManager getRoleManager() {
		return plugin.getRoleManager();
	}

	@NotNull
	public SpecializationManager getSpecializationManager() {
		return plugin.getSpecializationManager();
	}

	public boolean isDataReady() {
		return plugin.isDataReady();
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
	public TeleportManager getTeleportManager() {
		return plugin.getTeleportManager();
	}

    @NotNull
	public VillageAnimationManager getAnimationManager() {
		return plugin.getVillageAnimationManager();
	}

	@Nullable
	public VillageBuildEditorManager getBuildEditorManager() {
		return plugin.getVillageBuildEditorManager();
	}

	@NotNull
	public VillageQuestManager getQuestManager() {
		return plugin.getQuestManager();
	}

	public VillageLogManager getLogManager() {
		return plugin.getLogManager();
	}

	public RankingManager getRankingManager() {
		return plugin.getRankingManager();
	}

	public DiplomacyManager getDiplomacyManager() {
		return plugin.getDiplomacyManager();
	}

	public VillageUpkeepManager getUpkeepManager() {
		return plugin.getUpkeepManager();
	}

	@Nullable
	public Village getVillage(String playerName) {
		if (playerName == null) {
			return null;
		}
		return plugin.getUserManager().findByName(playerName)
				.map(User::getPresentVillage)
				.orElse(null);
	}

	@Nullable
	public Village getVillage(Player player) {
		return player == null ? null : getVillage(player.getUniqueId());
	}

	@Nullable
	public Village getVillage(UUID uuid) {
		if (uuid == null) {
			return null;
		}
		return plugin.getUserManager().findByUuid(uuid)
				.map(User::getPresentVillage)
				.orElse(null);
	}

	@Nullable
	public Village getVillageAt(Location location) {
		return location == null ? null : plugin.getVillageUtils().getVillageAt(location);
	}

	public boolean isVillageAt(Location location) {
		return getVillageAt(location) != null;
	}

    public ItemStack createVillageBlock() {
		ItemStack item = Item.create(Material.NOTE_BLOCK,
				plugin.getVillageMessages().get(Lang.VILLAGE_BLOCK_NAME).toString(),
				List.of(plugin.getVillageMessages().get(Lang.VILLAGE_BLOCK_LORE).toString()), true);
		NBT.modify(item, (Consumer<ReadWriteItemNBT>) nbt -> nbt.setBoolean("villageBlock", true));
		return item;
    }

	public ItemStack createDestroyer() {
		return Item.createDestroyer(Material.GOLDEN_PICKAXE,
				plugin.getVillageMessages().get(Lang.VILLAGE_DESTROYER_NAME).toString(),
				List.of(plugin.getVillageMessages().get(Lang.VILLAGE_DESTROYER_LORE).toString()));
	}

	public ItemStack createHearth() {
		ItemStack item = Item.createNoPlaceNoCraft(Material.BARRIER,
				plugin.getVillageMessages().get(Lang.VILLAGE_HEARTH_BLOCK_NAME).toString(),
				List.of(plugin.getVillageMessages().get(Lang.VILLAGE_HEARTH_BLOCK_LORE).toString()));
		NBT.modify(item, (Consumer<ReadWriteItemNBT>) nbt -> nbt.setBoolean("villageHearth", true));
		return item;
	}

	public ItemStack createHearthPart() {
		return Item.createNoPlaceNoCraft(Material.REDSTONE_BLOCK,
				plugin.getVillageMessages().get(Lang.VILLAGE_PART_HEARTH_BLOCK_NAME).toString(),
				List.of(plugin.getVillageMessages().get(Lang.VILLAGE_PART_HEARTH_BLOCK_LORE).toString()));
	}

	public ItemStack createDestroyerHearth() {
		return Item.createNoPlaceNoCraft(Material.GOLD_BLOCK,
				plugin.getVillageMessages().get(Lang.VILLAGE_DESTROYER_HEARTH_NAME).toString(),
				List.of(plugin.getVillageMessages().get(Lang.VILLAGE_DESTROYER_HEARTH_LORE).toString()));
	}
}
