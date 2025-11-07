package pl.kiosel.villages.api;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.core.hooks.EconomyManager;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.manager.VillageManager;

import javax.annotation.Nullable;
import java.util.*;

public class VillageAPI {

    private final AdvancedVillages plugin;

    public VillageAPI(AdvancedVillages plugin) {
        this.plugin = plugin;
    }

    public boolean hasMoney(Player player, double has) {
		return EconomyManager.hasBalance(player, has);
	}
	public void addMoney(Player player, double remove) {
		EconomyManager.deposit(player, remove);
	}
	public void removeMoney(Player player, double remove) {
		EconomyManager.withdrawBalance(player, remove);
	}

	@Nullable
	public Village getVillage(Player player) {
		return getVillage(player.getName());
	}

	@Nullable
	public Village getVillage(String playerName) {
		return VillageManager.getVillageByOfflineOwner(playerName);
	}

    public ItemStack createVillageBlock() {
        return Item.create(Material.NOTE_BLOCK, plugin.getLocale().getMessage(Lang.VILLAGE_BLOCK_NAME.getPath()).toString(), List.of("&7Place on ground to create village"));
    }

	public ItemStack createDestroyer() {
		return Item.createDestroyer(Material.GOLDEN_PICKAXE, plugin.getLocale().getMessage(Lang.VILLAGE_DESTROYER_NAME.getPath()).toString(), List.of("&7Destroy village with this"));
	}

	public ItemStack createHearth() {
		return Item.createNoPlaceNoCraft(Material.BARRIER, plugin.getLocale().getMessage(Lang.VILLAGE_HEARTH_BLOCK_NAME.getPath()).toString(), List.of("&7Use it to add hearth to your village"));
	}

	public ItemStack createVillageHearth() {
		return Item.createNoPlaceNoCraft(Material.REDSTONE_BLOCK, plugin.getLocale().getMessage(Lang.VILLAGE_PART_HEARTH_BLOCK_NAME.getPath()).toString(), List.of("&7Use it to create hearth"));
	}

	public ItemStack createDestroyerHearth() {
		return Item.createNoPlaceNoCraft(Material.GOLD_BLOCK, plugin.getLocale().getMessage(Lang.VILLAGE_DESTROYER_HEARTH_NAME.getPath()).toString(), List.of("&7Use it to create destroyer"));
	}
}