package pl.kiosel.villages.commands.subcommands;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import pl.kiosel.core.locale.Locale;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.commands.AVSubCommand;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.enums.CommandLang;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.manager.VillageUtilsManager;

public class RequestCommand extends AVSubCommand {

    @Override
    public String getName() { return "request"; }

    @Override
    public String getDescription() { return "confirm/cancel"; }

    @Override
    public String getUsage() { return "/village confirm/cancel"; }

	@Override
	public String getPermission() { return "villages.command.request"; }

	private final AdvancedVillages plugin;

	public RequestCommand(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run(Player player, User user, String[] args) {
		Locale locale = plugin.getLocale();
        if (!plugin.getInviteManager().isPlayerInvited(player)) {
			locale.getMessage(Lang.NO_INVITE.getPath()).sendPrefixedMessage(player);
            return;
        }

        Village village = user.getPresentVillage();
        if (village != null) {
			locale.getMessage(Lang.VILLAGE_IN.getPath()).sendPrefixedMessage(player);
            return;
        }

        if (args.length == 1) {
			player.sendMessage("accept/cancel");
		}
		if (args.length == 2) {
			if (args[1].equalsIgnoreCase(plugin.getCommandLang().getCommand(CommandLang.REQUEST_ACCEPT))) {
				Village villageInvited = plugin.getInviteManager().getVillageInvited(player);
				player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1.0f, 1.0f);
				VillageUtilsManager.replaceWith(player, villageInvited, Lang.INVITE_CONFIRMED.getPath()).sendPrefixedMessage(player);
				plugin.getInviteManager().acceptInvite(player);
			}

			if (args[1].equalsIgnoreCase(plugin.getCommandLang().getCommand(CommandLang.REQUEST_DENY))) {
				locale.getMessage(Lang.INVITE_CANCELED.getPath()).sendPrefixedMessage(player);
				plugin.getInviteManager().denyInvite(player);
			}
		}
    }
}