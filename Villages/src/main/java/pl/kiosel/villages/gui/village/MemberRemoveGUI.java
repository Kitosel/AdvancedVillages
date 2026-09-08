package pl.kiosel.villages.gui.village;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.compatibility.RosaSound;
import pl.kiosel.rosacore.compatibility.ZSound;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.features.logs.VillageLogType;
import pl.kiosel.villages.api.events.VillageMemberRemoveEvent;
import pl.kiosel.villages.config.GuiItemConfig;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.VillagePermission;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.gui.GUIS;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.gui.VillageConfirmationMenu;

import java.util.List;
import java.util.UUID;

public final class MemberRemoveGUI extends VillageConfirmationMenu {

	private final UUID memberId;

	public MemberRemoveGUI(AdvancedVillages plugin, Village village, Player player,
						   Gui parent, UUID memberId) {
		super(plugin, village, player, GUIS.DELETE_MEMBER, parent);
		this.memberId = memberId;

		OfflinePlayer memberPlayer = Bukkit.getOfflinePlayer(memberId);
		String memberName = memberPlayer.getName() == null ? memberId.toString() : memberPlayer.getName();
		GuiItemConfig confirm = item("confirm", 12, Material.LIME_CONCRETE,
				"&aYes", List.of());
		GuiItemConfig member = item("member", 13, Material.PLAYER_HEAD, "%player%",
				List.of("&7Remove: &6%player%"));
		GuiItemConfig cancel = item("cancel", 14, Material.RED_CONCRETE,
				"&cNo", List.of());

		setConfirmSlot(confirm.getSlot()).confirmItem(confirm.createItem()).showConfirm(confirm.isEnabled());
		setInformationSlot(member.getSlot()).informationItem(Item.createHead(memberPlayer,
				member.getName().replace("%player%", memberName),
				replaceMember(member.getLore(), memberName))).showInformation(member.isEnabled());
		setCancelSlot(cancel.getSlot()).cancelItem(cancel.createItem()).showCancel(cancel.isEnabled());
		playSoundOnClick(true);
		setConfirmSound(new RosaSound.SoundHolder(ZSound.BLOCK_NOTE_BLOCK_FLUTE, 2.0f, 0.0f));
		setCancelSound(new RosaSound.SoundHolder(ZSound.BLOCK_ANVIL_HIT, 2.0f, 0.0f));
		setCloseSound(null);
		onConfirm(event -> confirm(event.getGui()));
		onCancel(event -> event.getManager().showGUI(event.getPlayer(), parent));
		setItems();
	}

	private List<String> replaceMember(List<String> lore, String memberName) {
		return lore.stream().map(line -> line.replace("%player%", memberName)).toList();
	}

	private void confirm(Gui gui) {
		if (!hasPermission(VillagePermission.OWNER)) return;
		User member = plugin.getUserManager().findByUuid(memberId).orElse(null);
		if (member == null || member.getPresentVillage() != village) {
			messages().get(Lang.PLAYER_NOT_FOUND).sendPrefixed(viewer);
			return;
		}

		VillageMemberRemoveEvent removeEvent = new VillageMemberRemoveEvent(village, viewer);
		plugin.getServer().getPluginManager().callEvent(removeEvent);
		if (removeEvent.isCancelled()) {
			return;
		}
		village.removeMember(member);
		plugin.getLogManager().record(village, VillageLogType.MEMBER_KICK, viewer,
				"member", member.getName());
		messages().get(Lang.VILLAGE_REMOVE_MEMBER)
				.with("player", member.getName()).sendPrefixed(viewer);
		gui.exit();
	}
}
