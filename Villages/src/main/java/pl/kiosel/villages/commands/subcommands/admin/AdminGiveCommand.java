package pl.kiosel.villages.commands.subcommands.admin;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.kiosel.rosacore.utils.PlayerUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.CommandLang;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.user.User;

import java.util.List;

public final class AdminGiveCommand extends AdminSubCommand {

	public AdminGiveCommand(AdvancedVillages plugin) {
		super(plugin, CommandLang.ADMIN_GIVE);
	}

	@Override
	public String getDescription() {
		return "Gives an administrative village item";
	}

	@Override
	public String getUsage() {
		return "/village admin give <item>";
	}

	@Override
	public void run(Player player, User user, String[] args) {
		if (args.length != 2) {
			sendAdminUsage(player, Lang.COMMAND_ADMIN_USAGE_GIVE, null);
			return;
		}

		ItemStack item = createItem(args[1]);
		if (item == null) {
			sendAdminUsage(player, Lang.COMMAND_ADMIN_USAGE_GIVE, null);
			return;
		}

		PlayerUtils.giveItem(player, item);
		ItemMeta meta = item.getItemMeta();
		String itemName = meta == null || !meta.hasDisplayName()
				? item.getType().name()
				: meta.getDisplayName();
		sendLocalized(player, Lang.COMMAND_ADMIN_GIVE_ITEM, "item", itemName);
	}

	@Override
	public List<String> tabComplete(Player player, User user, String[] args) {
		if (args.length != 2) return List.of();
		return List.of(
				getCommand(CommandLang.ADMIN_GIVE_DESTROYER),
				getCommand(CommandLang.ADMIN_GIVE_VILLAGE),
				getCommand(CommandLang.ADMIN_GIVE_DESTROYER_HEARTH),
				getCommand(CommandLang.ADMIN_GIVE_VILLAGE_HEARTH),
				getCommand(CommandLang.ADMIN_GIVE_VILLAGE_HEARTH_PART)
		);
	}

	private ItemStack createItem(String input) {
		if (isCommand(input, CommandLang.ADMIN_GIVE_VILLAGE)) {
			return this.plugin.getApi().createVillageBlock();
		}
		if (isCommand(input, CommandLang.ADMIN_GIVE_DESTROYER)) {
			return this.plugin.getApi().createDestroyer();
		}
		if (isCommand(input, CommandLang.ADMIN_GIVE_DESTROYER_HEARTH)) {
			return this.plugin.getApi().createDestroyerHearth();
		}
		if (isCommand(input, CommandLang.ADMIN_GIVE_VILLAGE_HEARTH)) {
			return this.plugin.getApi().createHearth();
		}
		if (isCommand(input, CommandLang.ADMIN_GIVE_VILLAGE_HEARTH_PART)) {
			return this.plugin.getApi().createHearthPart();
		}
		return null;
	}
}
