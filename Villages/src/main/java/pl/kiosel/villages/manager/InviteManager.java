package pl.kiosel.villages.manager;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.kiosel.core.chat.AdventureUtils;
import pl.kiosel.core.dependencies.net.kyori.adventure.text.Component;
import pl.kiosel.core.dependencies.net.kyori.adventure.text.event.ClickEvent;
import pl.kiosel.core.dependencies.net.kyori.adventure.text.event.HoverEvent;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.logs.VillageLogType;
import pl.kiosel.villages.config.VillageMessages;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.CommandLang;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.enums.Permission;
import pl.kiosel.villages.settings.Settings;

import java.util.*;

public class InviteManager {

	private final AdvancedVillages plugin;
	private final Map<UUID, Village> invitedPlayers = new HashMap<>();
	private final Set<UUID> confirmLeave = new HashSet<>();

	public InviteManager(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	public void invitePlayer(Village village, Player invite) {
		VillageMessages messages = plugin.getMessages();
		UUID playerId = invite.getUniqueId();
		invitedPlayers.put(playerId, village);

		String request = "/" + plugin.getCommandLang().getCommandName() + " " + plugin.getCommandLang().getCommand(CommandLang.REQUEST);

		String accept = plugin.getCommandLang().getCommand(CommandLang.REQUEST_ACCEPT);
		String deny = plugin.getCommandLang().getCommand(CommandLang.REQUEST_DENY);

		Component confirm_message = Component
				.text(messages.get(Lang.INVITE_CONFIRM).toText())
				.hoverEvent(HoverEvent.showText(Component.text(messages.get(Lang.INVITE_CONFIRM_HOVER).toText())))
				.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, request + " " + accept));

		Component cancel_message = Component
				.text(messages.get(Lang.INVITE_CANCEL).toText())
				.hoverEvent(HoverEvent.showText(Component.text(messages.get(Lang.INVITE_CANCEL_HOVER).toText())))
				.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, request + " " + deny));

		messages.send(invite, Lang.SEPARATOR);
		AdventureUtils.sendMessage(confirm_message, invite);
		AdventureUtils.sendMessage(cancel_message, invite);
		messages.send(invite, Lang.SEPARATOR);

		new BukkitRunnable() {
			@Override
			public void run() {
				invitedPlayers.remove(playerId, village);
			}
		}.runTaskLater(plugin, Settings.VILLAGE_INVITE_EXPIRE.getLong() * 20L);
	}

	public void acceptInvite(Player player) {
		VillageMessages messages = plugin.getMessages();
		Village village = invitedPlayers.get(player.getUniqueId());
		if (village == null) {
			messages.sendPrefixed(player, Lang.NO_INVITE);
			return;
		}

		Set<Permission> defaultPerms = plugin.getPermissionManager().getDefaultPermission();
		User user = plugin.getUserManager().findByUuid(player.getUniqueId()).orNull();
		if (user == null) {
			messages.sendPrefixed(player, Lang.PLAYER_NOT_FOUND);
			invitedPlayers.remove(player.getUniqueId());
			return;
		}
		village.broadcast(messages.text(Lang.TARGET_JOIN_MEMBER, "player", player.getName()));

		user.setVillage(village);
		user.setPermissions(defaultPerms);
		village.addMember(user);
		plugin.getLogManager().record(village, VillageLogType.MEMBER_JOIN, player,
				"member", player.getName());

		invitedPlayers.remove(player.getUniqueId());
	}

	public void addConfirm(UUID uuid) {
		confirmLeave.add(uuid);
	}

	public void removeConfirm(UUID uuid) {
		confirmLeave.remove(uuid);
	}

	public boolean isConfirm(UUID uuid) {
		return confirmLeave.contains(uuid);
	}

	public Village getVillageInvited(Player player) {
		return invitedPlayers.get(player.getUniqueId());
	}

	public void denyInvite(Player player) {
		if (isPlayerInvited(player))
			invitedPlayers.remove(player.getUniqueId());
	}

	public boolean isPlayerInvited(Player invite) {
		return invitedPlayers.containsKey(invite.getUniqueId());
	}
}