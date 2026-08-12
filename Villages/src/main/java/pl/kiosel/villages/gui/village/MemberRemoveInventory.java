package pl.kiosel.villages.gui.village;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import pl.kiosel.core.gui.Gui;
import pl.kiosel.dependencies.com.cryptomorin.xseries.XSound;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.logs.VillageLogType;
import pl.kiosel.villages.config.GuiItemConfig;
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
		for (int slot = 0; slot < menuConfig.getSize(); slot++) {
			setItem(slot, Item.blank(Item.Blank.WHITE));
		}

		OfflinePlayer memberPlayer = Bukkit.getOfflinePlayer(memberId);
		String memberName = memberPlayer.getName() == null ? memberId.toString() : memberPlayer.getName();
		GuiItemConfig confirm = item("confirm", 12, Material.LIME_CONCRETE,
				getMessages().get(Lang.YES).toText(), List.of());
		if (confirm.isEnabled()) {
			setButton(confirm.getSlot(), confirm.createItem(), event -> confirm(event.gui));
		}
		GuiItemConfig member = item("member", 13, Material.PLAYER_HEAD, "%player%",
				List.of("&7Remove: &6%player%"));
		if (member.isEnabled()) {
			setItem(member.getSlot(), Item.createHead(memberPlayer,
					member.getName().replace("%player%", memberName),
					replaceMember(member.getLore(), memberName)));
		}
		GuiItemConfig cancel = item("cancel", 14, Material.RED_CONCRETE,
				getMessages().get(Lang.NO).toText(), List.of());
		if (cancel.isEnabled()) {
			setButton(cancel.getSlot(), cancel.createItem(), event -> {
				playSound(XSound.BLOCK_ANVIL_HIT, 2.0f, 0.0f);
				event.manager.showGUI(event.player, parent);
			});
		}
	}

	private GuiItemConfig item(String id, int slot, Material material, String name, List<String> lore) {
		return plugin.getGuiSettings().item(GUIS.DELETE_MEMBER, "guis.delete-member." + id,
				slot, material, name, lore);
	}

	private List<String> replaceMember(List<String> lore, String memberName) {
		return lore.stream().map(line -> line.replace("%player%", memberName)).toList();
	}

	private void confirm(Gui gui) {
		if (!hasPermission(Permission.OWNER)) return;
		User member = plugin.getUserManager().findByUuid(memberId).orNull();
		if (member == null || member.getPresentVillage() != village) {
			getMessages().get(Lang.PLAYER_NOT_FOUND).sendPrefixedMessage(viewer);
			return;
		}

		VillageMemberRemoveEvent removeEvent = new VillageMemberRemoveEvent(village, viewer);
		plugin.getServer().getPluginManager().callEvent(removeEvent);
		if (removeEvent.isCancelled()) {
			return;
		}
		village.removeMember(member);
		member.removeVillage();
		plugin.getLogManager().record(village, VillageLogType.MEMBER_KICK, viewer,
				"member", member.getName());
		getMessages().get(Lang.VILLAGE_REMOVE_MEMBER)
				.processPlaceholder("player", member.getName()).sendPrefixedMessage(viewer);
		playSound(XSound.BLOCK_NOTE_BLOCK_FLUTE, 2.0f, 0.0f);
		gui.exit();
	}
}
