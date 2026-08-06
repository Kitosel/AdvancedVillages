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
import pl.kiosel.villages.enums.CommandLang;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.enums.Permission;
import pl.kiosel.villages.data.village.Village;

import java.util.*;

import static pl.kiosel.core.utils.ColorUtils.tl;

public class InviteManager {

	private final AdvancedVillages plugin;
	private final Map<UUID, Village> invite_players = new HashMap<>();

	public InviteManager(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	public void invitePlayer(Village village, Player invite) {
		Locale locale = plugin.getLocale();
		invite_players.put(invite.getUniqueId(), village);


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
				invite_players.remove(invite.getUniqueId());
			}
		}.runTaskLaterAsynchronously(plugin, 400L);
	}

	public void acceptInvite(Player player) {
		Locale locale = plugin.getLocale();
		Village village = invite_players.get(player.getUniqueId());
		if (village == null) {
			locale.getMessage(Lang.NO_INVITE.getPath()).sendPrefixedMessage(player);
			return;
		}

		village.broadcast(locale.getMessage(Lang.TARGET_JOIN_MEMBER.getPath()).processPlaceholder("player", player.getName()).toText());

		Set<Permission> defaultPerms = plugin.getPermissionManager().getDefaultPermission();
		User user = plugin.getUserManager().findByUuid(player.getUniqueId()).get();

		user.setVillage(village);
		user.setPermissions(defaultPerms);
		village.addMember(user);

		invite_players.remove(player.getUniqueId());
	}

	public Village getVillageInvited(Player player) {
		return invite_players.get(player.getUniqueId());
	}

	public void denyInvite(Player player) {
		invite_players.remove(player.getUniqueId());
	}

	public boolean isPlayerInvited(Player invite) {
		return invite_players.containsKey(invite.getUniqueId());
	}
}