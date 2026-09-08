package pl.kiosel.villages.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.commands.AVSubCommand;
import pl.kiosel.villages.config.CommandLang;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.VillagePermission;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.features.diplomacy.DiplomacyManager;
import pl.kiosel.villages.data.village.features.diplomacy.DiplomacyResult;
import pl.kiosel.villages.manager.VillageUtils;

import java.util.ArrayList;
import java.util.List;

public final class AllianceCommand extends AVSubCommand {

	private final DiplomacyManager diplomacyManager;

	public AllianceCommand(AdvancedVillages plugin) {
		super(plugin, CommandLang.ALLIANCE);
		this.diplomacyManager = plugin.getDiplomacyManager();
	}

	@Override
	public String getDescription() { return plugin.getVillageMessages().text(Lang.DIPLOMACY_ALLIANCE_COMMAND_DESCRIPTION); }

	@Override
	public String getUsage() {
		return "/" + plugin.getCommandLang().getCommandName() + " "
				+ plugin.getCommandLang().getCommand(CommandLang.ALLIANCE)
				+ " <" + String.join("|", actions()) + "> [village]";
	}

	@Override
	public String getPermission() { return "advancedvillages.command.alliance"; }

	@Override
	public boolean requireVillage() { return true; }

	@Override
	public boolean isAvailable(CommandSender sender) {
		if (!diplomacyManager.isEnabled()
				|| !diplomacyManager.getSettings().isAlliancesEnabled()) {
			sendLocalized(sender, Lang.ADDON_DISABLED, "addon", "diplomacy/alliance");
			return false;
		}
		return true;
	}

	@Override
	public VillagePermission getVillagePermission() { return VillagePermission.UNSET; }

	@Override
	public void run(Player player, User user, String[] args) {
		Village village = user.getPresentVillage();
		if (village == null) return;
		if (!village.isTag()) {
			sendLocalized(player, Lang.TAG_NO);
			return;
		}

		String list = getCommand(CommandLang.ALLIANCE_LIST);
		if (args.length == 1 || args[1].equalsIgnoreCase(list)) {
			this.showList(player, village);
			return;
		}
		if (args.length != 3) {
			sendLocalized(player, Lang.DIPLOMACY_ALLIANCE_USAGE, "usage", getUsage());
			return;
		}
		if (!plugin.getRoleManager().hasPermission(user, VillagePermission.ALLIANCE_MANAGE)) {
			sendLocalized(player, Lang.VILLAGE_NO_PERMISSION);
			return;
		}

		Village target = plugin.getVillageManager().findByTag(args[2], true).orElse(null);
		if (target == null) {
			sendLocalized(player, Lang.COMMAND_VILLAGE_NOT_FOUND);
			return;
		}

		DiplomacyResult result;
		if (args[1].equalsIgnoreCase(getCommand(CommandLang.ALLIANCE_INVITE))) {
			result = diplomacyManager.requestAlliance(village, target);
		} else if (args[1].equalsIgnoreCase(getCommand(CommandLang.ALLIANCE_ACCEPT))) {
			result = diplomacyManager.acceptAlliance(village, target);
		} else if (args[1].equalsIgnoreCase(getCommand(CommandLang.ALLIANCE_DENY))) {
			result = diplomacyManager.denyAlliance(village, target);
		} else if (args[1].equalsIgnoreCase(getCommand(CommandLang.ALLIANCE_LEAVE))) {
			result = diplomacyManager.breakAlliance(village, target);
		} else {
			sendLocalized(player, Lang.DIPLOMACY_ALLIANCE_USAGE, "usage", getUsage());
			return;
		}
		if (result != DiplomacyResult.SUCCESS) {
			this.sendFailure(player, result);
		}
	}

	@Override
	public boolean isTabCompleteAvailable(Player player, User user) {
		Village village = user.getPresentVillage();
		return super.isTabCompleteAvailable(player, user)
				&& village != null
				&& village.isTag();
	}

	@Override
	public List<String> tabComplete(Player player, User user, String[] args) {
		if (args.length == 2) return actions();
		if (args.length != 3 || user.getPresentVillage() == null) return List.of();
		Village village = user.getPresentVillage();
		String selected = args[1];
		if (selected.equalsIgnoreCase(getCommand(CommandLang.ALLIANCE_ACCEPT))
				|| selected.equalsIgnoreCase(getCommand(CommandLang.ALLIANCE_DENY))) {
			return diplomacyManager.getPendingAllianceSenders(village).stream()
					.filter(Village::isTag)
					.map(Village::getTag).toList();
		}
		if (selected.equalsIgnoreCase(getCommand(CommandLang.ALLIANCE_LEAVE))) {
			return diplomacyManager.getAllies(village).stream()
					.filter(Village::isTag)
					.map(Village::getTag).toList();
		}
		if (selected.equalsIgnoreCase(getCommand(CommandLang.ALLIANCE_INVITE))) {
			return plugin.getVillageManager().getVillagesView().stream()
					.filter(other -> !other.equals(village))
					.filter(Village::isTag)
					.filter(other -> !diplomacyManager.areAllied(village, other))
					.map(Village::getTag).sorted(String.CASE_INSENSITIVE_ORDER).toList();
		}
		return List.of();
	}

	private void showList(Player player, Village village) {
		List<Village> allies = diplomacyManager.getAllies(village);
		List<Village> pending = diplomacyManager.getPendingAllianceSenders(village);
		plugin.getVillageMessages().send(player, Lang.SEPARATOR);
		plugin.getVillageMessages().send(player, Lang.DIPLOMACY_ALLIANCE_LIST_HEADER,
				"count", allies.size(), "max", diplomacyManager.getSettings().getMaximumAlliances());
		if (allies.isEmpty()) {
			plugin.getVillageMessages().send(player, Lang.DIPLOMACY_ALLIANCE_LIST_EMPTY);
		} else {
			for (Village ally : allies) {
				VillageUtils.replaceWithM(ally, plugin.getVillageMessages().text(Lang.DIPLOMACY_ALLIANCE_LIST_ENTRY)).sendMessage(player);
			}
		}
		if (!pending.isEmpty()) {
			plugin.getVillageMessages().send(player, Lang.DIPLOMACY_ALLIANCE_PENDING_HEADER);
			for (Village sender : pending) {
				VillageUtils.replaceWithM(sender, plugin.getVillageMessages().text(Lang.DIPLOMACY_ALLIANCE_LIST_ENTRY)).sendMessage(player);
			}
		}
		plugin.getVillageMessages().send(player, Lang.SEPARATOR);
	}

	private void sendFailure(Player player, DiplomacyResult result) {
		Lang message;
		switch (result) {
			case SAME_VILLAGE: message = Lang.DIPLOMACY_SAME_VILLAGE; break;
			case ALREADY_ALLIED: message = Lang.DIPLOMACY_ALLIANCE_ALREADY; break;
			case NOT_ALLIED: message = Lang.DIPLOMACY_ALLIANCE_NOT_ALLIED; break;
			case REQUEST_EXISTS: message = Lang.DIPLOMACY_ALLIANCE_REQUEST_EXISTS; break;
			case NO_REQUEST: message = Lang.DIPLOMACY_ALLIANCE_NO_REQUEST; break;
			case ALLIANCE_LIMIT: message = Lang.DIPLOMACY_ALLIANCE_LIMIT; break;
			case WAR_EXISTS:
			case WAR_COOLDOWN: message = Lang.DIPLOMACY_ALLIANCE_WAR_CONFLICT; break;
			default: message = Lang.DIPLOMACY_UNAVAILABLE;
		}
		sendLocalized(player, message);
	}

	private List<String> actions() {
		List<String> actions = new ArrayList<>();
		actions.add(getCommand(CommandLang.ALLIANCE_INVITE));
		actions.add(getCommand(CommandLang.ALLIANCE_ACCEPT));
		actions.add(getCommand(CommandLang.ALLIANCE_DENY));
		actions.add(getCommand(CommandLang.ALLIANCE_LEAVE));
		actions.add(getCommand(CommandLang.ALLIANCE_LIST));
		return actions;
	}
}
