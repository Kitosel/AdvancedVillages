package pl.kiosel.villages.commands.subcommands.admin;

import org.bukkit.entity.Player;
import pl.kiosel.rosacore.location.LocationUtils;
import pl.kiosel.rosacore.utils.TimeUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.CommandLang;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.level.Level;
import pl.kiosel.villages.manager.VillageUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class AdminDebugCommand extends AdminSubCommand {

	public AdminDebugCommand(AdvancedVillages plugin) {
		super(plugin, CommandLang.ADMIN_DEBUG);
	}

	@Override
	public String getDescription() {
		return "Displays village diagnostics";
	}

	@Override
	public String getUsage() {
		return "/village admin debug [owner]";
	}

	@Override
	public void run(Player player, User user, String[] args) {
		if (args.length > 2) {
			sendAdminUsage(player, Lang.COMMAND_ADMIN_USAGE_DEBUG, null);
			return;
		}

		Village village;
		if (args.length == 1) {
			village = this.plugin.getVillageUtils().getVillageAt(player.getLocation());
			if (village == null) {
				sendLocalized(player, Lang.COMMAND_ADMIN_VILLAGE_NOT_FOUND);
				return;
			}
		} else {
			String owner = args[1];
			Optional<Village> villageOption = this.plugin.getVillageManager().findByOwner(owner, true);
			if (villageOption.isEmpty()) {
				sendLocalized(player, Lang.COMMAND_VILLAGE_NOT_FOUND, "village_owner", owner);
				return;
			}
			village = villageOption.get();
		}
		sendVillageInfo(player, village);
	}

	@Override
	public List<String> tabComplete(Player player, User user, String[] args) {
		return args.length == 2 ? sortedVillageOwners() : List.of();
	}

	private void sendVillageInfo(Player player, Village village) {
		String notSet = this.plugin.getVillageMessages().text(Lang.COMMAND_ADMIN_DEBUG_NOT_SET);
		String none = this.plugin.getVillageMessages().text(Lang.COMMAND_ADMIN_DEBUG_NONE);
		String location = village.getLocation()
				.map(value -> LocationUtils.convertLocationToString(value, "&7-&c"))
				.orElse(notSet);
		String home = village.getHome()
				.map(value -> LocationUtils.convertLocationToString(value, "&7-&c"))
				.orElse(notSet);
		String villageLives = VillageUtils.getLivesSymbol(village.getLives(), false)
				+ " &7(&6" + village.getLives() + "&7)";
		String members = village.getMembersName().stream()
				.sorted(String.CASE_INSENSITIVE_ORDER)
				.collect(Collectors.joining("&7, &6"));
		String protection = village.getProtection().isAfter(Instant.now())
				? TimeUtils.getStringDate(village.getProtection().getEpochSecond())
				: none;
		String allies = none;
		int wars = 0;
		if (this.plugin.getDiplomacyManager() != null && this.plugin.getDiplomacyManager().isEnabled()) {
			String alliesTags = this.plugin.getDiplomacyManager().getAlliesTags(village);
			allies = alliesTags.isBlank() ? none : alliesTags;
			wars = this.plugin.getDiplomacyManager().countCurrentWars(village);
		}
		int missedPayments = this.plugin.getUpkeepManager() != null && this.plugin.getUpkeepManager().isEnabled()
				? this.plugin.getUpkeepManager().getMissedPayments(village)
				: 0;
		Level level = village.getLevel();
		User owner = village.getOwner();

		this.plugin.getVillageMessages().format(Lang.COMMAND_ADMIN_DEBUG_INFO,
				"name", village.getName(),
				"uuid", village.getID(),
				"tag", village.isTag() ? village.getTag() : none,
				"owner", owner == null ? notSet : owner.getName(),
				"level", level == null ? notSet : level.getLevel(),
				"size", level == null ? notSet : level.getSize(),
				"location", location,
				"home", home,
				"lives", villageLives,
				"bank", village.getBank(),
				"members", members.isBlank() ? none : members,
				"online_members", village.getOnlineMembers().size(),
				"protection", protection,
				"allies", allies,
				"wars", wars,
				"missed_payments", missedPayments,
				"pvp", state(village.hasPvPEnabled()),
				"tnt", state(village.hasTntEnabled()),
				"animations", state(village.isAnimationsEnabled()),
				"persistence", this.plugin.getVillageMessages().text(village.wasChanged()
						? Lang.COMMAND_ADMIN_DEBUG_DIRTY : Lang.COMMAND_ADMIN_DEBUG_SAVED),
				"version", village.getChangeVersion()
		).sendMessage(player);
	}

	private String state(boolean enabled) {
		return this.plugin.getVillageMessages().text(enabled ? Lang.ON : Lang.OFF);
	}
}
