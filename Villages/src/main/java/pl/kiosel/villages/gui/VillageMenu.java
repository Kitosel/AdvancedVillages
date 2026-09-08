package pl.kiosel.villages.gui;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.rosacore.compatibility.ZSound;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.rosacore.gui.PagedGui;
import pl.kiosel.rosacore.utils.TimeUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.GuiItemConfig;
import pl.kiosel.villages.config.GuiMenuConfig;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.config.VillageMessages;
import pl.kiosel.villages.data.user.VillagePermission;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.manager.UpgradeManager;
import pl.kiosel.villages.manager.VillageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class VillageMenu extends PagedGui {

	protected final AdvancedVillages plugin;
	protected final VillageGUIManager menus;
	protected final Village village;
	protected final Player viewer;
	protected final GuiMenuConfig menuConfig;
	@Getter protected final VillageMessages messages;

	protected VillageMenu(AdvancedVillages plugin, VillageGUIManager menus, Village village, Player viewer, GUIS type, Gui parent) {
		this(plugin, menus, village, viewer, type, parent, false);
	}

	protected VillageMenu(AdvancedVillages plugin, VillageGUIManager menus, Village village,
						  Player viewer, GUIS type, Gui parent, boolean paged) {
		super(parent);
		this.plugin = plugin;
		this.menus = menus;
		this.village = village;
		this.viewer = viewer;
		this.messages = plugin.getVillageMessages();
		this.menuConfig = plugin.getGuiSettings().menu(type);

		setTitle(this.menuConfig.getTitle());
		setRows(this.menuConfig.getRows());
		setDefaultItem(null);
		setUseHeader(paged);
		setUseFooter(paged);

		ItemStack headerItem = Item.blank(Item.Blank.WHITE);
		if (paged) {
			setHeaderBackItem(headerItem);
			setFooterBackItem(null);
			setItem(Math.max(0, this.menuConfig.getSize() - 10), null);
		} else {
			for (int slot = 0; slot < Math.min(9, this.menuConfig.getSize()); slot++) {
				setItem(slot, headerItem);
			}
			setItem(Math.max(0, this.menuConfig.getSize() - 1), null);
		}
		setNextPageItem(Item.blank(Item.Blank.NEXT_PAGE));
		setPreviousPageItem(Item.blank(Item.Blank.PREVIUS_PAGE));
	}

	protected void addBackButton(int slot) {
		Gui destination = getParent();
		if (destination != null) {
			setButton(slot, Item.blank(Item.Blank.BACK),
					event -> {
						playSound(ZSound.UI_TOAST_IN, 1.0f, 2.0f);
						event.getManager().openGUI(event.getPlayer(), destination);
					});
		}
	}

	protected VillageMessages getVillageMessages() {
		return this.messages;
	}

	protected void addBackButton() {
		this.addBackButton(this.menuConfig.getBackSlot());
	}

	protected GuiItemConfig item(GUIS menu, String path, int defaultSlot, Material material, String name, List<String> lore) {
		return this.plugin.getGuiSettings().item(menu, path, defaultSlot, material, name, lore);
	}

	protected void openFromMain(GUIS type) {
		playSound(ZSound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 2.0f);
		reopen(type);
	}

	protected void playToggleSound() {
		playSound(ZSound.UI_BUTTON_CLICK, 1.0f, 2.0f);
	}

	protected void playSound(ZSound sound, float volume, float pitch) {
		if (sound.getSound().isEmpty())
			return;
		markCustomSoundPlayed();
		viewer.playSound(viewer.getLocation(), sound.getSound().orElseThrow(), volume, pitch);
	}

	protected void playSound(Sound sound, float volume, float pitch) {
		if (sound == null) return;
		markCustomSoundPlayed();
		viewer.playSound(viewer.getLocation(), sound, volume, pitch);
	}

	protected void reopen(GUIS type) {
		menus.openGui(village, viewer, type);
	}

	protected boolean hasPermission(VillagePermission permission) {
		if (plugin.getRoleManager().hasPermission(viewer.getUniqueId(), permission))
			return true;

		plugin.getVillageMessages().get(Lang.VILLAGE_NO_PERMISSION).sendPrefixed(viewer);
		return false;
	}

	protected List<String> replaceWith(List<String> strings) {
		return VillageUtils.replaceWithList(village, strings);
	}

	protected List<String> replaceWithLevelInfo(List<String> strings, int level) {
		List<String> list = new ArrayList<>();
		for (String line : strings) {
			list.add(line.toLowerCase(Locale.ROOT)
					.replace("%village_cost%", Integer.toString(UpgradeManager.getCostForLevel(level + 1)))
					.replace("%village_level%", Integer.toString(level))
					.replace("%village_next_level%", Integer.toString(level + 1))
					.replace("%village_size%", Integer.toString(UpgradeManager.getSizeForLevel(level)))
					.replace("%village_next_size%", Integer.toString(UpgradeManager.getSizeForLevel(level + 1))));
		}
		return list;
	}

	protected List<String> replaceEffects(List<String> strings, String price, String amplifier, String effect) {
		List<String> list = new ArrayList<>();
		for (String line : strings) {
			list.add(line.toLowerCase(Locale.ROOT)
					.replace("%price%", price)
					.replace("%amplifier%", amplifier)
					.replace("%effect%", effect));
		}
		return list;
	}

	protected List<String> replacePlayer(List<String> strings, OfflinePlayer player) {
		String playerName = player.getName() == null ? player.getUniqueId().toString() : player.getName();
		String lastOnline = TimeUtils.getStringDate(player.getLastPlayed());
		String nowOnline = plugin.getGuiSettings().text("guis.common.player-online", "&aThe player is online");

		List<String> list = new ArrayList<>();
		for (String line : strings) {
			list.add(line.toLowerCase(Locale.ROOT)
					.replace("%player%", playerName)
					.replace("%player_last_online%", player.isOnline() ? nowOnline : lastOnline)
					.replace("%player_uuid%", player.getUniqueId().toString()));
		}
		return list;
	}

}
