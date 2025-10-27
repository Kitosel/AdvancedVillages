package pl.kiosel.villages.models;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.kiosel.villages.Wioski;
import pl.kiosel.villages.config.Language;
import pl.kiosel.villages.enums.CommandLang;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.enums.Permission;
import pl.kiosel.villages.village.Village;
import pl.kiosel.villages.village.VillageManager;
import pl.kiosel.villages.village.VillageMember;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static pl.kiosel.common.utils.ColorUtils.tl;

public class InviteManager {

	private final Wioski plugin;
	private final Map<UUID, Village> invite_players = new HashMap<>();

	public InviteManager(Wioski plugin) {
		this.plugin = plugin;
	}

	public void invitePlayer(Village village, Player invite) {
		Language lang = plugin.getLang();
		invite_players.put(invite.getUniqueId(), village);

		TextComponent confirm_message = new TextComponent(lang.getMessage(Lang.INVITE_CONFIRM));
		TextComponent cancel_message = new TextComponent(lang.getMessage(Lang.INVITE_CANCEL));

		String request = "/" + plugin.getCommandLang().getCommandName() + " " + plugin.getCommandLang().getCommand(CommandLang.REQUEST);

		String accept = plugin.getCommandLang().getCommand(CommandLang.REQUEST_ACCEPT);
		String deny = plugin.getCommandLang().getCommand(CommandLang.REQUEST_DENY);

		confirm_message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(lang.getMessage(Lang.INVITE_CONFIRM_HOVER)).create()));
		confirm_message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, request + " " + accept));

		cancel_message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(lang.getMessage(Lang.INVITE_CANCEL_HOVER)).create()));
		cancel_message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, request + " " + deny));

		invite.sendMessage(tl("&8——————————————————————————"));
		invite.spigot().sendMessage(confirm_message);
		invite.spigot().sendMessage(cancel_message);
		invite.sendMessage(tl("&8——————————————————————————"));

		new BukkitRunnable() {
			@Override
			public void run() {
				invite_players.remove(invite.getUniqueId());
			}
		}.runTaskLaterAsynchronously(plugin, 400L);
	}

	public void acceptInvite(Player player) {
		Language lang = plugin.getLang();
		VillageManager villageManager = plugin.getVillageManager();

		Village village = invite_players.remove(player.getUniqueId());
		if (village == null) {
			player.sendMessage(lang.getMessage(Lang.NO_INVITE));
			return;
		}

		for (UUID memberUUID : village.getMembers()) {
			Player member = Bukkit.getPlayer(memberUUID);
			if (member != null && member.isOnline()) {
				member.sendMessage(lang.getMessage(Lang.TARGET_JOIN_MEMBER).replace("%PLAYER%", player.getName()));
			}
		}
		villageManager.addMember(village, player);

		List<Permission> defaultPerms = plugin.getPermissionManager().getDefaultPermission();
		VillageMember villageMember = new VillageMember(player.getUniqueId(), village, defaultPerms);

		plugin.getUserManager().addUserToVillage(villageMember, player, false);
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