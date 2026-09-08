package pl.kiosel.villages.commands.subcommands;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.commands.AVSubCommand;
import pl.kiosel.villages.config.CommandLang;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.config.VillageMessage;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.VillagePermission;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.manager.VillageUtils;

import java.util.List;

public class RequestCommand extends AVSubCommand {

    @Override
    public String getDescription() { return "confirm/cancel"; }

    @Override
    public String getUsage() { return "/village confirm/cancel"; }

	@Override
	public String getPermission() { return "villages.command.request"; }

	@Override
	public boolean requireVillage() { return false; }

	@Override
	public VillagePermission getVillagePermission() { return VillagePermission.UNSET; }

	private final AdvancedVillages plugin;

	public RequestCommand(AdvancedVillages plugin) {
		super(plugin, CommandLang.REQUEST);
		this.plugin = plugin;
	}

	@Override
	public void run(Player player, User user, String[] args) {
        if (!plugin.getInviteManager().isPlayerInvited(player)) {
			sendLocalized(player, Lang.NO_INVITE);
            return;
        }

        if (user.getPresentVillage() != null) {
			sendLocalized(player, Lang.VILLAGE_IN);
            return;
        }

		if (args.length != 2) {
			sendUsage(player);
			return;
		}
		String arg1 = args[1].toLowerCase();

		if (arg1.equals(getCommand(CommandLang.REQUEST_ACCEPT))) {
				Village villageInvited = plugin.getInviteManager().getVillageInvited(player);
				if (villageInvited == null) {
					sendLocalized(player, Lang.NO_INVITE);
					return;
				}
			if (villageInvited.getMembers().size()
					>= plugin.getDevelopmentManager().getMaxMembers(villageInvited)) {
					sendLocalized(player, Lang.MAX_MEMBERS);
					return;
				}
				player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1.0f, 1.0f);
				VillageUtils.replaceWith(player, villageInvited, Lang.INVITE_CONFIRMED).sendPrefixed(player);
				plugin.getInviteManager().acceptInvite(player);
				return;
			}

		if (arg1.equals(getCommand(CommandLang.REQUEST_DENY))) {
			Village villageInvited = plugin.getInviteManager().getVillageInvited(player);
			VillageMessage message = VillageUtils.replaceWith(player, villageInvited, Lang.INVITE_DECLINE);
			villageInvited.broadcast(message.getPrefixedMessage());

			sendLocalized(player, Lang.INVITE_CANCELED);
			plugin.getInviteManager().denyInvite(player);
			return;
		}

		sendUsage(player);
    }

	@Override
	public boolean isTabCompleteAvailable(Player player, User user) {
		return super.isTabCompleteAvailable(player, user)
				&& user.getPresentVillage() == null
				&& plugin.getInviteManager().isPlayerInvited(player);
	}

	@Override
	public List<String> tabComplete(Player player, User user, String[] args) {
		if (args.length != 2) return List.of();
		return List.of(
				getCommand(CommandLang.REQUEST_ACCEPT),
				getCommand(CommandLang.REQUEST_DENY)
		);
	}

	private void sendUsage(Player player) {
		getMessage(Lang.COMMAND_USAGE_REQUEST.getPath())
				.with("accept", getCommand(CommandLang.REQUEST_ACCEPT))
				.with("deny", getCommand(CommandLang.REQUEST_DENY))
				.sendPrefixed(player);
	}
}
