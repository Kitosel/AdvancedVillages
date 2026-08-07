package pl.kiosel.villages.gui.village;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
import pl.kiosel.villages.events.VillageMemberRemoveEvent;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;

import java.util.List;
import java.util.UUID;

public final class MemberRemoveInventory extends VillageMenu {

	private final UUID memberId;

	public MemberRemoveInventory(AdvancedVillages plugin, VillageGUIManager menus, Village village,
	                             Player player, Gui parent, UUID memberId) {
		super(plugin, menus, village, player, GUIS.DELETE_MEMBER, parent);
		this.memberId = memberId;
		for (int slot = 0; slot < GUIS.DELETE_MEMBER.getSize(); slot++) {
			setItem(slot, Item.blank(Item.Blank.WHITE));
		}

		OfflinePlayer memberPlayer = Bukkit.getOfflinePlayer(memberId);
		String memberName = memberPlayer.getName() == null ? memberId.toString() : memberPlayer.getName();
		setButton(12, Item.create(Material.LIME_CONCRETE, GuiConfig.yes), event -> confirm(event.gui));
		setItem(13, Item.createHead(memberPlayer, memberName, List.of("&7Remove: &6" + memberName)));
		setButton(14, Item.create(Material.RED_CONCRETE, GuiConfig.no),
				event -> {
					playSound(Sound.BLOCK_ANVIL_HIT, 2.0f, 0.0f);
					event.manager.showGUI(event.player, parent);
				});
	}

	private void confirm(Gui gui) {
		if (!hasPermission(Permission.OWNER)) return;
		User member = plugin.getUserManager().findByUuid(memberId).orNull();
		if (member == null || member.getPresentVillage() != village) {
			plugin.getLocale().getMessage(Lang.PLAYER_NOT_FOUND.getPath()).sendPrefixedMessage(viewer);
			return;
		}

		VillageMemberRemoveEvent removeEvent = new VillageMemberRemoveEvent(village, viewer);
		plugin.getServer().getPluginManager().callEvent(removeEvent);
		if (removeEvent.isCancelled()) {
			return;
		}
		village.removeMember(member);
		member.removeVillage();
		plugin.getLocale().getMessage(Lang.VILLAGE_REMOVE_MEMBER.getPath())
				.processPlaceholder("player", member.getName()).sendPrefixedMessage(viewer);
		playSound(Sound.BLOCK_NOTE_BLOCK_FLUTE, 2.0f, 0.0f);
		gui.exit();
	}
}