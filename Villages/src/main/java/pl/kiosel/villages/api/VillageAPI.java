package pl.kiosel.villages.api;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.villages.Wioski;
import pl.kiosel.villages.config.Config;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.common.Item;
import pl.kiosel.villages.village.Village;
import pl.kiosel.villages.village.VillageManager;

import javax.annotation.Nullable;
import java.util.*;

public class VillageAPI {

    private final Wioski plugin;

    public VillageAPI(Wioski plugin) {
        this.plugin = plugin;
    }

    public double getMoney(Player player) { return plugin.getEco().getBalance(player); }
    public boolean hasMoney(Player player, double has) { return plugin.getEco().has(player, has); }
	public void addMoney(Player player, double remove) { plugin.getEco().depositPlayer(player, remove); }
	public void removeMoney(Player player, double remove) { plugin.getEco().withdrawPlayer(player, remove); }

    public List<String> getEnabledWorlds() {
		return Config.enabled_worlds;
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
        return Item.create(Material.NOTE_BLOCK, plugin.getLang().getMessage(Lang.VILLAGE_BLOCK_NAME), List.of("&7Place on ground to create village"));
    }

	public ItemStack createDestroyer() {
		return Item.create(Material.GOLDEN_PICKAXE, plugin.getLang().getMessage(Lang.VILLAGE_DESTROYER_NAME), List.of("&7Destroy village with this"));
	}

	public ItemStack createVillageHearth() {
		return Item.create(Material.REDSTONE_BLOCK, plugin.getLang().getMessage(Lang.VILLAGE_HEARTH_BLOCK_NAME), List.of("&7Use it to create hearth"));
	}

	public ItemStack createDestroyerHearth() {
		return Item.create(Material.GOLD_BLOCK, plugin.getLang().getMessage(Lang.VILLAGE_DESTROYER_HEARTH_NAME), List.of("&7Use it to create destroyer"));
	}
}