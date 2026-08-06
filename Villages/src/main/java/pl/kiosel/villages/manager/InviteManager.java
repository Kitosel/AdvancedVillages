package pl.kiosel.villages.manager;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.kiosel.core.chat.AdventureUtils;
import pl.kiosel.core.dependencies.net.kyori.adventure.text.Component;
import pl.kiosel.core.dependencies.net.kyori.adventure.text.event.ClickEvent;
import pl.kiosel.core.dependencies.net.kyori.adventure.text.event.HoverEvent;
import pl.kiosel.core.locale.Locale;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.CommandLang;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.enums.Permission;
import pl.kiosel.villages.settings.Settings;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static pl.kiosel.core.utils.ColorUtils.tl;

public class InviteManager {

	private final AdvancedVillages plugin;
	private final Map<UUID, Village> invitedPlayers = new HashMap<>();

	public InviteManager(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	public void invitePlayer(Village village, Player invite) {
		Locale locale = plugin.getLocale();
		UUID playerId = invite.getUniqueId();
		invitedPlayers.put(playerId, village);

		String request = "/" + plugin.getCommandLang().getCommandName() + " " + plugin.getCommandLang().getCommand(CommandLang.REQUEST);

		String accept = plugin.getCommandLang().getCommand(CommandLang.REQUEST_ACCEPT);
		String deny = plugin.getCommandLang().getCommand(CommandLang.REQUEST_DENY);

		Component confirm_message = Component
				.text(locale.getMessage(Lang.INVITE_CONFIRM.getPath()).toText())
				.hoverEvent(HoverEvent.showText(Component.text(locale.getMessage(Lang.INVITE_CONFIRM_HOVER.getPath()).toText())))
				.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, request + " " + accept));

		Component cancel_message = Component
				.text(locale.getMessage(Lang.INVITE_CANCEL.getPath()).toText())
				.hoverEvent(HoverEvent.showText(Component.text(locale.getMessage(Lang.INVITE_CANCEL_HOVER.getPath()).toText())))
				.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, request + " " + deny));

		invite.sendMessage(tl("&8——————————————————————————"));
		AdventureUtils.sendMessage(confirm_message, invite);
		AdventureUtils.sendMessage(cancel_message, invite);
		invite.sendMessage(tl("&8——————————————————————————"));

		new BukkitRunnable() {
			@Override
			public void run() {
				invitedPlayers.remove(playerId, village);
			}
		}.runTaskLater(plugin, Settings.VILLAGE_INVITE_EXPIRE.getLong() * 20L);
	}

	public void acceptInvite(Player player) {
		Locale locale = plugin.getLocale();
		Village village = invitedPlayers.get(player.getUniqueId());
		if (village == null) {
			locale.getMessage(Lang.NO_INVITE.getPath()).sendPrefixedMessage(player);
			return;
		}

		Set<Permission> defaultPerms = plugin.getPermissionManager().getDefaultPermission();
		User user = plugin.getUserManager().findByUuid(player.getUniqueId()).orNull();
		if (user == null) {
			locale.getMessage(Lang.PLAYER_NOT_FOUND.getPath()).sendPrefixedMessage(player);
			invitedPlayers.remove(player.getUniqueId());
			return;
		}

		user.setVillage(village);
		user.setPermissions(defaultPerms);
		village.addMember(user);
		village.broadcast(locale.getMessage(Lang.TARGET_JOIN_MEMBER.getPath()).processPlaceholder("player", player.getName()).toText());

		invitedPlayers.remove(player.getUniqueId());
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