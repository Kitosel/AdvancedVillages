package pl.kiosel.villages.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.features.diplomacy.DiplomacyManager;
import pl.kiosel.villages.data.village.features.diplomacy.DiplomacyResult;
import pl.kiosel.villages.data.village.features.diplomacy.VillageWar;
import pl.kiosel.villages.data.village.features.diplomacy.WarState;
import pl.kiosel.villages.commands.AVSubCommand;
import pl.kiosel.villages.config.CommandLang;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.VillagePermission;
import pl.kiosel.villages.data.village.Village;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class WarCommand extends AVSubCommand {

	private final DiplomacyManager diplomacyManager;

	public WarCommand(AdvancedVillages plugin) {
		super(plugin, CommandLang.WAR);
		this.diplomacyManager = plugin.getDiplomacyManager();
	}

	@Override
	public String getDescription() { return plugin.getVillageMessages().text(Lang.DIPLOMACY_WAR_COMMAND_DESCRIPTION); }

	@Override
	public String getUsage() {
		return "/" + plugin.getCommandLang().getCommandName() + " "
				+ getCommand(CommandLang.WAR) + " <" + String.join("|", actions()) + "> [village]";
	}

	@Override
	public String getPermission() { return "villages.command.war"; }

	@Override
	public boolean requireVillage() { return true; }

	@Override
	public boolean isAvailable(CommandSender sender) {
		if (!diplomacyManager.isEnabled()
				|| !diplomacyManager.getSettings().isWarsEnabled()) {
			sendLocalized(sender, Lang.ADDON_DISABLED, "addon", "diplomacy/war");
			return false;
		}
		return true;
	}

	@Override
	public VillagePermission getVillagePermission() { return VillagePermission.UNSET; }

	@Override
	public void run(Player player, User user, String[] args) {
		Village village = user.getPresentVillage();
		if (!village.isTag()) {
			sendLocalized(player, Lang.TAG_NO);
			return;
		}

		String info = getCommand(CommandLang.WAR_INFO);
		if (args.length == 1 || args[1].equalsIgnoreCase(info)) {
			Village target = args.length >= 3
					? plugin.getVillageManager().findByTag(args[2], true).orElse(null) : null;
			if (args.length >= 3 && target == null) {
				sendLocalized(player, Lang.COMMAND_VILLAGE_NOT_FOUND);
				return;
			}
			this.showWars(player, village, target);
			return;
		}
		if (args.length != 3) {
			sendLocalized(player, Lang.DIPLOMACY_WAR_USAGE, "usage", getUsage());
			return;
		}
		if (!plugin.getRoleManager().hasPermission(user, VillagePermission.WAR_MANAGE)) {
			sendLocalized(player, Lang.VILLAGE_NO_PERMISSION);
			return;
		}
		Village target = plugin.getVillageManager().findByTag(args[2], true).orElse(null);
		if (target == null) {
			sendLocalized(player, Lang.COMMAND_VILLAGE_NOT_FOUND);
			return;
		}

		DiplomacyResult result;
		if (args[1].equalsIgnoreCase(getCommand(CommandLang.WAR_DECLARE))) {
			result = diplomacyManager.declareWar(village, target);
		} else if (args[1].equalsIgnoreCase(getCommand(CommandLang.WAR_SURRENDER))) {
			result = diplomacyManager.surrender(village, target);
		} else {
			sendLocalized(player, Lang.DIPLOMACY_WAR_USAGE, "usage", getUsage());
			return;
		}
		if (result != DiplomacyResult.SUCCESS) this.sendFailure(player, result);
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
		if (args[1].equalsIgnoreCase(getCommand(CommandLang.WAR_SURRENDER))
				|| args[1].equalsIgnoreCase(getCommand(CommandLang.WAR_INFO))) {
			return diplomacyManager.getWars(village).stream()
					.filter(war -> war.getState(Instant.now()) != WarState.FINISHED)
					.map(war -> diplomacyManager.getOtherVillage(war, village))
					.filter(Objects::nonNull)
					.filter(Village::isTag)
					.map(Village::getTag).distinct().toList();
		}
		if (args[1].equalsIgnoreCase(getCommand(CommandLang.WAR_DECLARE))) {
			return plugin.getVillageManager().getVillagesView().stream()
					.filter(other -> !other.equals(village))
					.filter(Village::isTag)
					.filter(other -> !diplomacyManager.areAllied(village, other))
					.map(Village::getTag).sorted(String.CASE_INSENSITIVE_ORDER).toList();
		}
		return List.of();
	}

	private void showWars(Player player, Village village, Village filter) {
		List<VillageWar> wars = diplomacyManager.getWars(village).stream()
				.filter(war -> filter == null || war.contains(filter.getUUID()))
				.toList();
		plugin.getVillageMessages().send(player, Lang.SEPARATOR);
		plugin.getVillageMessages().send(player, Lang.DIPLOMACY_WAR_LIST_HEADER, "count", wars.size());
		if (wars.isEmpty()) {
			plugin.getVillageMessages().send(player, Lang.DIPLOMACY_WAR_LIST_EMPTY);
		} else {
			Instant now = Instant.now();
			for (VillageWar war : wars) {
				Village enemy = diplomacyManager.getOtherVillage(war, village);
				if (enemy == null) continue;
				VillageWar.Snapshot snapshot = war.snapshot();
				boolean attacker = snapshot.getAttackerVillageId().equals(village.getUUID());
				WarState state = war.getState(now);
				Duration remaining;
				if (state == WarState.PREPARING) {
					remaining = Duration.between(now, snapshot.getStartsAt());
				} else if (state == WarState.ACTIVE) {
					remaining = Duration.between(now, snapshot.getScheduledEndsAt());
				} else {
					remaining = snapshot.getCooldownUntil() == null
							? Duration.ZERO : Duration.between(now, snapshot.getCooldownUntil());
				}
				if (remaining.isNegative()) remaining = Duration.ZERO;
				plugin.getVillageMessages().send(player, Lang.DIPLOMACY_WAR_LIST_ENTRY,
						"village", enemy.getName(),
						"state", stateName(state),
						"time", plugin.getVillageMessages().formatDuration(remaining),
						"our_score", attacker ? snapshot.getAttackerScore() : snapshot.getDefenderScore(),
						"enemy_score", attacker ? snapshot.getDefenderScore() : snapshot.getAttackerScore());
			}
		}
		plugin.getVillageMessages().send(player, Lang.SEPARATOR);
	}

	private String stateName(WarState state) {
		switch (state) {
			case PREPARING: return plugin.getVillageMessages().text(Lang.DIPLOMACY_WAR_STATE_PREPARING);
			case ACTIVE: return plugin.getVillageMessages().text(Lang.DIPLOMACY_WAR_STATE_ACTIVE);
			default: return plugin.getVillageMessages().text(Lang.DIPLOMACY_WAR_STATE_FINISHED);
		}
	}

	private void sendFailure(Player player, DiplomacyResult result) {
		Lang message;
		switch (result) {
			case SAME_VILLAGE: message = Lang.DIPLOMACY_SAME_VILLAGE; break;
			case ALREADY_ALLIED: message = Lang.DIPLOMACY_WAR_ALLIED; break;
			case WAR_EXISTS: message = Lang.DIPLOMACY_WAR_EXISTS; break;
			case WAR_COOLDOWN: message = Lang.DIPLOMACY_WAR_COOLDOWN; break;
			case WAR_LIMIT: message = Lang.DIPLOMACY_WAR_LIMIT; break;
			case NOT_ENOUGH_MEMBERS: message = Lang.DIPLOMACY_WAR_NOT_ENOUGH_MEMBERS; break;
			case NOT_ENOUGH_ONLINE: message = Lang.DIPLOMACY_WAR_NOT_ENOUGH_ONLINE; break;
			case NOT_ENOUGH_BANK: message = Lang.DIPLOMACY_WAR_NOT_ENOUGH_BANK; break;
			case NO_WAR: message = Lang.DIPLOMACY_WAR_NONE; break;
			default: message = Lang.DIPLOMACY_UNAVAILABLE;
		}
		sendLocalized(player, message,
				"cost", diplomacyManager.getSettings().getDeclarationCost());
	}

	private List<String> actions() {
		List<String> actions = new ArrayList<>();
		actions.add(getCommand(CommandLang.WAR_DECLARE));
		actions.add(getCommand(CommandLang.WAR_SURRENDER));
		actions.add(getCommand(CommandLang.WAR_INFO));
		return actions;
	}
}
