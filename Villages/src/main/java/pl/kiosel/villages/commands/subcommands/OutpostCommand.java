package pl.kiosel.villages.commands.subcommands;

import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.commands.AVSubCommand;
import pl.kiosel.villages.config.CommandLang;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.outpost.Outpost;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.VillagePermission;
import pl.kiosel.villages.data.village.level.Level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class OutpostCommand extends AVSubCommand {

	public OutpostCommand(AdvancedVillages plugin) {
		super(plugin, CommandLang.OUTPOST);
	}

	@Override
	public String getDescription() {
		return plugin.getVillageMessages().text(Lang.OUTPOST_COMMAND_DESCRIPTION);
	}

	@Override
	public String getUsage() {
		return getFixedUsage() + "<number>";
	}

	@Override
	public String getPermission() {
		return "advancedvillages.command.outpost";
	}

	@Override
	public boolean requireVillage() {
		return true;
	}

	@Override
	public VillagePermission getVillagePermission() {
		return VillagePermission.UNSET;
	}

	@Override
	public void run(Player player, User user, String[] args) {
		List<Outpost> outposts = plugin.getOutpostManager().getOutposts(user.getPresentVillage());
		if (outposts.isEmpty()) {
			sendLocalized(player, Lang.OUTPOST_LIST_EMPTY);
			return;
		}
		if (args.length < 2) {
			String list = IntStream.range(0, outposts.size())
					.mapToObj(index -> "&f" + (index + 1) + "&7. &b" + outposts.get(index).getName())
					.collect(Collectors.joining("&8, "));
			plugin.getVillageMessages().get(Lang.OUTPOST_LIST).with("outposts", list).sendPrefixed(player);
			return;
		}
		boolean upgrade = args[1].equalsIgnoreCase("upgrade");
		if (upgrade && !plugin.getRoleManager().hasPermission(user, VillagePermission.OUTPOST_MANAGE)) {
			sendLocalized(player, Lang.VILLAGE_NO_PERMISSION);
			return;
		}
		int argumentStart = upgrade ? 2 : 1;
		if (args.length <= argumentStart) {
			sendLocalized(player, Lang.OUTPOST_NOT_FOUND);
			return;
		}
		Outpost selected = find(outposts,
				String.join(" ", Arrays.copyOfRange(args, argumentStart, args.length)));
		if (selected == null) {
			sendLocalized(player, Lang.OUTPOST_NOT_FOUND);
			return;
		}
		if (!upgrade) {
			plugin.getTeleportManager().teleportPlayerToOutpost(player, selected);
			return;
		}
		Level next = plugin.getOutpostLevelManager()
				.getLevel(selected.getLevel().getLevel() + 1);
		if (next == null) {
			sendLocalized(player, Lang.OUTPOST_MAX_LEVEL);
			return;
		}
		if (!plugin.getOutpostManager().canPasteLevel(next.getLevel())) {
			sendLocalized(player, Lang.OUTPOST_SCHEMATIC_MISSING);
			return;
		}
		if (!plugin.getUpgradeManager().canUpgrade(player, next)) return;
		if (plugin.getOutpostManager().upgrade(selected)) {
			plugin.getVillageMessages().get(Lang.OUTPOST_UPGRADED)
					.with("outpost", selected.getName()).with("level", selected.getLevel().getLevel())
					.sendPrefixed(player);
		}
	}

	@Override
	public List<String> tabComplete(Player player, User user, String[] args) {
		if (plugin.getOutpostManager() == null) return List.of();
		List<Outpost> outposts = plugin.getOutpostManager().getOutposts(user.getPresentVillage());
		List<String> values = new ArrayList<>();
		for (int index = 0; index < outposts.size(); index++) values.add(String.valueOf(index + 1));
		return args.length == 3 ? values : List.of();
	}

	private Outpost find(List<Outpost> outposts, String query) {
		try {
			int index = Integer.parseInt(query) - 1;
			if (index >= 0 && index < outposts.size()) return outposts.get(index);
		} catch (NumberFormatException ignored) {
		}
		String expected = query.toLowerCase(Locale.ROOT);
		return outposts.stream()
				.filter(outpost -> outpost.getName().toLowerCase(Locale.ROOT).equals(expected))
				.findFirst().orElse(null);
	}
}
