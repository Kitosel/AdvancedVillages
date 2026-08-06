package pl.kiosel.villages.gui.village;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import pl.kiosel.core.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.GuiConfig;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.enums.Permission;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;
import pl.kiosel.villages.manager.TeleportManager;

public final class SettingsInventory extends VillageMenu {

	public SettingsInventory(AdvancedVillages plugin, VillageGUIManager menus, Village village,
	                         Player player, Gui parent) {
		super(plugin, menus, village, player, GUIS.SETTINGS, parent);
		addBackButton(4);

		setButton(13, Item.create(Material.AMETHYST_SHARD, 1,
				GuiConfig.guis_village_setting_animations,
				replaceWith(GuiConfig.guis_village_setting_animations_lore),
				village.isAnimationsEnabled()), event -> toggleAnimations());
		setButton(19, Item.create(Material.FEATHER, 1,
				GuiConfig.guis_village_setting_teleport,
				replaceWith(GuiConfig.guis_village_setting_teleport_lore), false), event -> toggleTeleport());
		setButton(21, Item.create(Material.MOJANG_BANNER_PATTERN, 1,
				GuiConfig.guis_village_setting_tag,
				replaceWith(GuiConfig.guis_village_setting_tag_lore), false), event -> openTag(event.gui));
		setButton(23, Item.create(Material.TNT, 1,
				GuiConfig.guis_village_setting_tnt,
				replaceWith(GuiConfig.guis_village_setting_tnt_lore), village.isTnt()), event -> toggleTnt());
		setButton(25, Item.create(Material.NETHERITE_SWORD, 1,
				GuiConfig.guis_village_setting_pvp,
				replaceWith(GuiConfig.guis_village_setting_pvp_lore), village.isPvp()), event -> togglePvp());
		setButton(31, Item.create(Material.ORANGE_BED, 1,
				GuiConfig.guis_village_setting_delete,
				GuiConfig.guis_village_setting_delete_lore, false), event -> openRemove());
	}

	private boolean canChangeSettings() {
		return hasPermission(Permission.SETTINGS);
	}

	private void toggleAnimations() {
		if (!canChangeSettings()) return;
		village.toggleAnimations();
		playToggleSound();
		reopen(GUIS.SETTINGS);
	}

	private void toggleTeleport() {
		if (!canChangeSettings()) return;
		TeleportManager teleportManager = plugin.getTeleportManager();
		exit();
		if (!teleportManager.isTeleportTask(viewer)) {
			teleportManager.setTeleportTask(viewer);
			teleportManager.sendHoverSet(viewer);
		} else {
			teleportManager.removeTeleportTask(viewer);
			plugin.getLocale().getMessage(Lang.TELEPORT_SET_CANCEL.getPath()).sendPrefixedMessage(viewer);
		}
	}

	private void openTag(Gui current) {
		if (!canChangeSettings()) return;
		if (village.isTag()) {
			exit();
			plugin.getLocale().getMessage(Lang.TAG_VILLAGE.getPath())
					.processPlaceholder("tag", village.getTag()).sendPrefixedMessage(viewer);
			return;
		}
		plugin.getGuiManager().showGUI(viewer, new TagInventory(plugin, village, viewer, current));
	}

	private void toggleTnt() {
		if (!canChangeSettings()) return;
		village.setTnt(!village.isTnt());
		playToggleSound();
		reopen(GUIS.SETTINGS);
	}

	private void togglePvp() {
		if (!canChangeSettings()) return;
		village.togglePvP();
		playToggleSound();
		reopen(GUIS.SETTINGS);
	}

	private void openRemove() {
		User user = plugin.getUserManager().findByUuid(viewer.getUniqueId()).orNull();
		if (user != null && village.isOwner(user)) {
			reopen(GUIS.REMOVE);
			return;
		}
		plugin.getLocale().getMessage(Lang.VILLAGE_NO_PERMISSION.getPath()).sendPrefixedMessage(viewer);
		playSound(Sound.BLOCK_NOTE_BLOCK_COW_BELL, 2.0f, 0.0f);
	}
}
