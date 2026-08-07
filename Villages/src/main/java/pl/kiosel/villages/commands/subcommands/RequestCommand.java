package pl.kiosel.villages.commands.subcommands;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.commands.AVSubCommand;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.CommandLang;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.enums.Permission;
import pl.kiosel.villages.manager.VillageUtilsManager;
import pl.kiosel.villages.settings.Settings;

public class RequestCommand extends AVSubCommand {

    @Override
    public String getName() { return "request"; }

    @Override
    public String getDescription() { return "confirm/cancel"; }

    @Override
    public String getUsage() { return "/village confirm/cancel"; }

	@Override
	public String getPermission() { return "villages.command.request"; }

	@Override
	public Permission getVillagePermission() { return Permission.UNSET; }

	private final AdvancedVillages plugin;

	public RequestCommand(AdvancedVillages plugin) {
		super(plugin);
		this.plugin = plugin;
	}

	@Override
	public void run(Player player, User user, String[] args) {
        if (!plugin.getInviteManager().isPlayerInvited(player)) {
			sendLocalized(player, Lang.NO_INVITE);
            return;
        }

        Village village = user.getPresentVillage();
        if (village != null) {
			sendLocalized(player, Lang.VILLAGE_IN);
            return;
        }

		if (args.length != 2) {
			sendUsage(player);
			return;
		}

		if (args[1].equalsIgnoreCase(plugin.getCommandLang().getCommand(CommandLang.REQUEST_ACCEPT))) {
				Village villageInvited = plugin.getInviteManager().getVillageInvited(player);
				if (villageInvited == null) {
					sendLocalized(player, Lang.NO_INVITE);
					return;
				}
				if (villageInvited.getMembers().size() >= Settings.VILLAGE_MAX_MEMBERS.getInt()) {
					sendLocalized(player, Lang.MAX_MEMBERS);
					return;
				}
				player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1.0f, 1.0f);
				VillageUtilsManager.replaceWith(player, villageInvited, Lang.INVITE_CONFIRMED).sendPrefixedMessage(player);
				plugin.getInviteManager().acceptInvite(player);
				return;
			}

		if (args[1].equalsIgnoreCase(plugin.getCommandLang().getCommand(CommandLang.REQUEST_DENY))) {
			sendLocalized(player, Lang.INVITE_CANCELED);
			plugin.getInviteManager().denyInvite(player);
			return;
		}

		sendUsage(player);
    }

	private void sendUsage(Player player) {
		getMessage(Lang.COMMAND_USAGE_REQUEST.getPath())
				.processPlaceholder("accept", plugin.getCommandLang().getCommand(CommandLang.REQUEST_ACCEPT))
				.processPlaceholder("deny", plugin.getCommandLang().getCommand(CommandLang.REQUEST_DENY))
				.sendPrefixedMessage(player);
	}
}
