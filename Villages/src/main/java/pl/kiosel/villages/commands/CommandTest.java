package pl.kiosel.villages.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import panda.std.Option;
import pl.kiosel.core.chat.AdventureUtils;
import pl.kiosel.core.commands.SimpleCommand;
import pl.kiosel.core.dependencies.net.kyori.adventure.title.Title;
import pl.kiosel.core.hooks.WorldEditHook;
import pl.kiosel.core.utils.TabUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.Permission;
import pl.kiosel.villages.manager.VillageNameGenerator;

import java.io.File;
import java.util.List;

public class CommandTest extends SimpleCommand {

	private final AdvancedVillages plugin;
	private final VillageNameGenerator generator;

	public CommandTest(AdvancedVillages plugin) {
		super(plugin, "test");
		this.plugin = plugin;
		this.generator = new VillageNameGenerator();
	}

	@Override
	public boolean onExecute(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(tl("You can't"));
			return false;
		}
		Player player = (Player) sender;
		User user = plugin.getUserManager().findByUuid(player.getUniqueId()).get();

		if (args.length == 1) {
			switch (args[0]) {
				case "villages":
					for (Village village : plugin.getVillageManager().getVillages()) {
						player.sendMessage(village.getName() + " " + village.getTag());
					}
					break;
				case "adv_title":
					Title title = AdventureUtils.createTitle(AdventureUtils.formatComponent("test1"), AdventureUtils.formatComponent("test2"));
					AdventureUtils.sendTitle(title, player);
					break;
				case "adv_actionbar":
					AdventureUtils.sendActionBar(AdventureUtils.formatComponent("test1"), player);
					break;
				case "test1":
//					player.sendMessage("teststeststest");
//					OfflinePlayer player1 = Bukkit.getOfflinePlayer(UUID.fromString("cb8b7c68-1787-3a6e-aebb-221c0218b1bb"));
//					player.sendMessage("player: " + player1.getName());
					break;
				case "memory_test":
					player.sendMessage(player.toString());
					player.sendMessage("test1");
					player.sendMessage(player + " ");
					break;
				case "worldedit_test":
					File file = new File(plugin.getDataFolder(), "schematics/Turret" + "1" + ".schem");
					Bukkit.getScheduler().runTask(plugin, () -> WorldEditHook.pasteSchematic(file, player.getLocation()));
					break;
				case "addpermission":
					if (user == null) {
						player.sendMessage("member is null");
						break;
					}
					plugin.getPermissionManager().addPermission(user, Permission.EFFECTS_TOGGLE);
					player.sendMessage("added perm");
					break;
				case "getPermissions":
					if (user == null) {
						player.sendMessage("member is null");
						break;
					}
					player.sendMessage(plugin.getPermissionManager().toString(user.getPermissions()));
					break;
				case "villagemembers":
					player.sendMessage("provide a village");
					break;
				case "local":
					player.sendMessage("provide a node");
					break;
			}
		}
		if (args.length == 2) {
			switch (args[1]) {
				case "random_name":
					player.sendMessage(tl("&7Random: &c" + generator.getRandomName()));
					break;
				case "villages_owner":
					player.sendMessage(tl("&7Villages:"));
					for (String s : plugin.getVillageManager().getVillageOwners()) {
						player.sendMessage(s);
					}
					break;
				case "villages_names":
					player.sendMessage(tl("&7Villages:"));
					for (String s : plugin.getVillageManager().getVillageNamesAsList()) {
						player.sendMessage(s);
					}
					break;
				case "villagemembers":
					Option<Village> village = plugin.getVillageManager().findByName(args[1]);
					if (village == null) {
						player.sendMessage("village is null");
						return false;
					}
					for (User member : village.get().getMembers())
						player.sendMessage(member.getName());
					break;
			}
			if (args[0].equalsIgnoreCase("getPermissions")) {
				Player another = Bukkit.getPlayer(args[1]);
				if (another == null) {
					player.sendMessage("another is null");
					return false;
				}
				User member2 = plugin.getUserManager().findByPlayer(another).get();
				if (member2 == null) {
					player.sendMessage("member is null");
					return false;
				}
				player.sendMessage(plugin.getPermissionManager().toString(member2.getPermissions()));
			}
			if (args[0].equalsIgnoreCase("local")) {
				plugin.getMessages().get(args[1]).sendPrefixedMessage(player);
			}
		}
		if (args.length == 3) {
			if (args[1].equals("region")) {
				Option<Village> village2 = plugin.getVillageManager().findByName(args[2]);
				if (village2 == null) {
					player.sendMessage("village is null");
					return false;
				}
				player.sendMessage(village2.get().getRegion().toString());
				player.sendMessage(village2.get().getRegion().get().toString());
				player.sendMessage(village2.get().getRegion().get().getCenter().toString());
				player.sendMessage(village2.get().getRegion().get().getSize() + "");
			}
		}
		return false;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (args.length == 1) {
			return TabUtils.returnWith(args[0], List.of(
					"villages", "adv_title", "adv_actionbar", "test1", "worldedit_test",
					"addpermission", "villagemembers", "getPermissions", "memory_test",
					"local"
			));
		}
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("villages"))
				return TabUtils.returnWith(args[1], List.of("random_name", "villages_owner", "villages_names", "region"));
			if (args[0].equalsIgnoreCase("getPermissions"))
				return TabUtils.onlinePlayers();
			if (args[0].equalsIgnoreCase("villagemembers"))
				return TabUtils.returnWith(args[1], plugin.getVillageManager().getVillageNamesAsList());
		}
		if (args.length == 3) {
			if (args[1].equalsIgnoreCase("villages_owner"))
				return TabUtils.returnWith(args[2], plugin.getVillageManager().getVillageOwners());
			if (args[1].equalsIgnoreCase("villages_names") || args[1].equalsIgnoreCase("region"))
				return TabUtils.returnWith(args[2], plugin.getVillageManager().getVillageNamesAsList());
		}
		return TabUtils.returnEmpty();
	}
}
