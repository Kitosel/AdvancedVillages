package pl.kiosel.villages.commands.subcommands;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import pl.kiosel.villages.Wioski;
import pl.kiosel.common.command.SubCommand;
import pl.kiosel.villages.config.Language;
import pl.kiosel.villages.enums.CommandLang;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.village.Village;
import pl.kiosel.villages.village.VillageManager;

public class RequestCommand extends SubCommand {

    @Override
    public String getName() { return "request"; }

    @Override
    public String getDescription() { return "confirm/cancel"; }

    @Override
    public String getUsage() { return "/village confirm/cancel"; }

	@Override
	public String getPermission() { return "villages.command.request"; }

	@Override
    public void run(Player player, Wioski plugin, String[] args) {
        Language lang = plugin.getLang();
        if (!plugin.getInviteManager().isPlayerInvited(player)) {
            player.sendMessage(lang.getMessage(Lang.NO_INVITE));
            return;
        }

        Village village = VillageManager.getVillageByOfflineOwner(player.getName());
        if (village != null) {
            player.sendMessage(lang.getMessage(Lang.VILLAGE_IN));
            return;
        }

        if (args.length == 1) {
			player.sendMessage("accept/cancel");
		}
		if (args.length == 2) {
			if (args[1].equalsIgnoreCase(plugin.getCommandLang().getCommand(CommandLang.REQUEST_ACCEPT))) {
				Village inviter = plugin.getInviteManager().getVillageInvited(player);
				player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1.0f, 1.0f);
				player.sendMessage(lang.getMessage(Lang.INVITE_CONFIRMED).replace("%OWNER%", inviter.getOwner()));
				plugin.getInviteManager().acceptInvite(player);
			}

			if (args[1].equalsIgnoreCase(plugin.getCommandLang().getCommand(CommandLang.REQUEST_DENY))) {
				player.sendMessage(lang.getMessage(Lang.INVITE_CANCELED));
				plugin.getInviteManager().denyInvite(player);
			}
		}
    }
}