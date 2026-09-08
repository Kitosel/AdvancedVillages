package pl.kiosel.villages.gui.village;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.compatibility.ZSound;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.rosacore.gui.GuiClickEvent;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.features.logs.VillageLogType;
import pl.kiosel.villages.config.GuiItemConfig;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.VillagePermission;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.gui.GUIS;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;
import pl.kiosel.villages.manager.teleport.TeleportManager;

import java.util.List;
import java.util.function.Consumer;

public final class SettingsGUI extends VillageMenu {

	public SettingsGUI(AdvancedVillages plugin, VillageGUIManager menus, Village village,
					   Player player, Gui parent) {
		super(plugin, menus, village, player, GUIS.SETTINGS, parent);
		addBackButton();

		GuiItemConfig animations = item("animations", 13, Material.AMETHYST_SHARD,
				"&d&lAnimations", List.of("&7Animations: %village_animations%"));
		button(animations, replaceWith(animations.getLore()), village.isAnimationsEnabled(), event -> toggleAnimations());

		GuiItemConfig teleport = item("teleport", 19, Material.FEATHER,
				"&b&lTeleport", List.of("%village_teleport%", "&7Click to set"));
		button(teleport, replaceWith(teleport.getLore()), false, event -> setTeleport());

		GuiItemConfig tag = item("tag", 21, Material.MOJANG_BANNER_PATTERN,
				"&c&lTag", List.of("&e&lTag: &c%village_tag%", "%istagset%"));
		button(tag, replaceWith(tag.getLore()), false, event -> openTag(event.getGui()));

		GuiItemConfig tnt = item("tnt", 23, Material.TNT,
				"&c&lTNT", List.of("&7TNT: %village_tnt%"));
		button(tnt, replaceWith(tnt.getLore()), village.isTnt(), event -> toggleTnt());

		GuiItemConfig pvp = item("pvp", 25, Material.NETHERITE_SWORD,
				"&f&lPVP", List.of("&7Pvp: %village_pvp%"));
		button(pvp, replaceWith(pvp.getLore()), village.isPvp(), event -> togglePvp());

		GuiItemConfig logs = item("logs", 27, Material.BOOK,
				"&6Activity logs", List.of("&7View important village actions"));
		if (logs.isEnabled() && plugin.getLogManager().canView(player, village) && plugin.isDev()) {
			setButton(logs.getSlot(), logs.createItem(), event -> openFromMain(GUIS.LOGS));
		}

		GuiItemConfig delete = item("delete", 31, Material.ORANGE_BED,
				"&c&l&nDelete village", List.of("&7Click to delete"));
		button(delete, delete.getLore(), false, event -> openRemove());
	}

	private GuiItemConfig item(String id, int slot, Material material, String name, List<String> lore) {
		return plugin.getGuiSettings().item(GUIS.SETTINGS, "guis.village.settings." + id,
				slot, material, name, lore);
	}

	private void button(GuiItemConfig item, List<String> lore, boolean stateGlow, Consumer<GuiClickEvent> action) {
		if (item.isEnabled()) {
			setButton(item.getSlot(), item.createItem(item.getName(), lore, item.isGlow() || stateGlow), action);
		}
	}

	private boolean canChangeSettings() {
		return hasPermission(VillagePermission.SETTINGS);
	}

	private void toggleAnimations() {
		if (!canChangeSettings()) return;
		village.toggleAnimations();
		plugin.getLogManager().record(village, VillageLogType.SETTING_CHANGED, viewer,
				"setting", "animations", "value", village.isAnimationsEnabled());
		playToggleSound();
		reopen(GUIS.SETTINGS);
	}

	private void setTeleport() {
		if (!canChangeSettings()) return;
		TeleportManager teleportManager = plugin.getTeleportManager();
		exit();
		if (!teleportManager.isTeleportTask(viewer)) {
			teleportManager.setTeleportTask(viewer);
			teleportManager.sendHoverSet(viewer);
		} else {
			teleportManager.removeTeleportTask(viewer);
			getVillageMessages().get(Lang.TELEPORT_SET_CANCEL).sendPrefixed(viewer);
		}
	}

	private void openTag(Gui current) {
		if (!canChangeSettings()) return;
		if (village.isTag()) {
			exit();
			getVillageMessages().get(Lang.TAG_VILLAGE)
					.with("tag", village.getTag()).sendPrefixed(viewer);
			return;
		}
		plugin.getGuiManager().showGUI(viewer, new TagGUI(plugin, village, viewer, current));
	}

	private void toggleTnt() {
		if (!canChangeSettings()) return;
		village.setTnt(!village.isTnt());
		plugin.getLogManager().record(village, VillageLogType.SETTING_CHANGED, viewer,
				"setting", "tnt", "value", village.isTnt());
		playToggleSound();
		reopen(GUIS.SETTINGS);
	}

	private void togglePvp() {
		if (!canChangeSettings()) return;
		village.togglePvP();
		plugin.getLogManager().record(village, VillageLogType.SETTING_CHANGED, viewer,
				"setting", "pvp", "value", village.isPvp());
		playToggleSound();
		reopen(GUIS.SETTINGS);
	}

	private void openRemove() {
		User user = plugin.getUserManager().findByUuid(viewer.getUniqueId()).orElse(null);
		if (user != null && village.isOwner(user)) {
			reopen(GUIS.REMOVE);
			return;
		}
		getVillageMessages().get(Lang.VILLAGE_NO_PERMISSION).sendPrefixed(viewer);
		playSound(ZSound.BLOCK_NOTE_BLOCK_COW_BELL, 2.0f, 0.0f);
	}
}
