package pl.kiosel.villages.gui;

import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import pl.kiosel.core.gui.Gui;
import pl.kiosel.core.gui.GuiManager;
import pl.kiosel.core.gui.SimplePagedGui;
import pl.kiosel.core.gui.events.GuiClickEvent;
import pl.kiosel.core.gui.methods.Clickable;
import pl.kiosel.core.utils.TimeUtils;
import pl.kiosel.dependencies.com.cryptomorin.xseries.XSound;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.GuiItemConfig;
import pl.kiosel.villages.config.GuiMenuConfig;
import pl.kiosel.villages.config.VillageMessages;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.enums.Permission;
import pl.kiosel.villages.manager.UpgradeManager;
import pl.kiosel.villages.manager.VillageUtilsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static pl.kiosel.core.utils.ColorUtils.tl;

public abstract class VillageMenu extends SimplePagedGui {

	protected final AdvancedVillages plugin;
	protected final VillageGUIManager menus;
	protected final Village village;
	protected final Player viewer;
	protected final GuiMenuConfig menuConfig;
	@Getter protected final VillageMessages messages;
	private final boolean paged;

	protected VillageMenu(AdvancedVillages plugin, VillageGUIManager menus, Village village,
	                      Player viewer, GUIS type, Gui parent) {
		this(plugin, menus, village, viewer, type, parent, false);
	}

	protected VillageMenu(AdvancedVillages plugin, VillageGUIManager menus, Village village,
	                      Player viewer, GUIS type, Gui parent, boolean paged) {
		super(parent);
		this.plugin = plugin;
		this.menus = menus;
		this.village = village;
		this.viewer = viewer;
		this.paged = paged;
		this.messages = plugin.getMessages();
		this.menuConfig = plugin.getGuiSettings().menu(type);

		setTitle(tl(this.menuConfig.getTitle()));
		setRows(this.menuConfig.getRows());
		setDefaultItem(null);
		setUseHeader(paged);

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
		setNextPage(Item.blank(Item.Blank.NEXT_PAGE));
		setPrevPage(Item.blank(Item.Blank.PREVIUS_PAGE));
	}

	@Override
	public void update() {
		if (paged) {
			super.update();
			return;
		}
		if (inventory == null)
			return;

		for (int slot = 0; slot < inventory.getSize(); slot++) {
			inventory.setItem(slot, cellItems.get(slot));
		}
	}

	@Override
	protected boolean onClick(@NotNull GuiManager manager, @NotNull Player player, @NotNull Inventory inventory, @NotNull InventoryClickEvent event) {
		if (paged) {
			super.onClick(manager, player, inventory, event);
			return false;
		}

		Map<ClickType, Clickable> actions = conditionalButtons.get(event.getSlot());
		if (actions == null) {
			return false;
		}
		Clickable action = actions.get(event.getClick());
		if (action == null)
			action = actions.get(null);

		if (action == null)
			return false;

		action.onClick(new GuiClickEvent(manager, this, player, event, event.getSlot(), true));
		return false;
	}

	protected void addBackButton(int slot) {
		Gui destination = getParent();
		if (destination != null) {
			setButton(slot, Item.blank(Item.Blank.BACK),
					event -> {
						playSound(XSound.UI_TOAST_IN, 1.0f, 2.0f);
						event.manager.showGUI(event.player, destination);
					});
		}
	}

	protected void addBackButton() {
		this.addBackButton(this.menuConfig.getBackSlot());
	}

	protected GuiItemConfig configuredItem(GUIS menu, String path, int defaultSlot,
	                                       org.bukkit.Material material, String name,
	                                       List<String> lore) {
		return this.plugin.getGuiSettings().item(menu, path, defaultSlot, material, name, lore);
	}

	protected void openFromMain(GUIS type) {
		playSound(XSound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 2.0f);
		reopen(type);
	}

	protected void playToggleSound() {
		playSound(XSound.UI_BUTTON_CLICK, 1.0f, 2.0f);
	}

	protected void playSound(XSound sound, float volume, float pitch) {
		if (sound.get() == null)
			return;
		viewer.playSound(viewer.getLocation(), sound.get(), volume, pitch);
	}

	protected void reopen(GUIS type) {
		menus.openGui(village, viewer, type);
	}

	protected boolean hasPermission(Permission permission) {
		if (plugin.getPermissionManager().hasPermission(viewer.getUniqueId(), permission))
			return true;

		plugin.getMessages().get(Lang.VILLAGE_NO_PERMISSION).sendPrefixedMessage(viewer);
		return false;
	}

	protected List<String> replaceWith(List<String> strings) {
		return VillageUtilsManager.replaceWithList(village, strings);
	}

	protected List<String> replaceWithLevelInfo(List<String> strings, int level) {
		List<String> list = new ArrayList<>();
		for (String line : strings) {
			list.add(line
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
			list.add(line
					.replace("%price%", price)
					.replace("%amplifier%", amplifier)
					.replace("%effect%", effect));
		}
		return list;
	}

	protected List<String> replacePlayer(List<String> strings, OfflinePlayer player) {
		String playerName = player.getName() == null ? player.getUniqueId().toString() : player.getName();
		String lastOnline = TimeUtils.getStringDate(player.getLastPlayed());
		String nowOnline = plugin.getMessages().get(Lang.PLAYER_ONLINE).toString();
		List<String> list = new ArrayList<>();
		for (String line : strings) {
			list.add(line
					.replace("%PLAYER%", playerName)
					.replace("%player%", playerName)
					.replace("%PLAYER_LAST_ONLINE%", player.isOnline() ? nowOnline : lastOnline)
					.replace("%player_last_online%", player.isOnline() ? nowOnline : lastOnline)
					.replace("%PLAYER_UUID%", player.getUniqueId().toString())
					.replace("%player_uuid%", player.getUniqueId().toString()));
		}
		return list;
	}

}
