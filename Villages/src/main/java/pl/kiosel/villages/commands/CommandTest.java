package pl.kiosel.villages.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.command.RosaCommand;
import pl.kiosel.rosacore.utils.ColorUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.firststeps.TutorialGUI;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.VillagePermission;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.manager.VillageNameGenerator;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CommandTest extends RosaCommand {

	private final AdvancedVillages plugin;
	private final VillageNameGenerator generator;

	public CommandTest(AdvancedVillages plugin) {
		super(plugin, "test", List.of());
		this.plugin = plugin;
		this.generator = new VillageNameGenerator();
	}

	@Override
	public boolean isPlayerOnly() {
		return true;
	}

	@Override
	public boolean onExecute(CommandSender sender, String label, String[] args) {
		Player player = (Player) sender;
		if (!player.isOp()) return true;
		User user = plugin.getUserManager().getOrCreate(player);

		if (args.length == 1) {
			switch (args[0]) {
				case "villages":
					for (Village village : plugin.getVillageManager().getVillages()) {
						player.sendMessage(village.getName() + " " + village.getTag());
					}
					break;
				case "adv_title":
					plugin.getMessenger().title(player, "test1", "test2");
					break;
				case "adv_actionbar":
					plugin.getMessenger().actionBar(player, "test1");
					break;
				case "test1":
					player.sendMessage("teststeststest");
					OfflinePlayer player1 = Bukkit.getOfflinePlayer(UUID.fromString("cb8b7c68-1787-3a6e-aebb-221c0218b1bb"));
					player.sendMessage("player: " + player1.getName());
					break;
				case "memory_test":
					player.sendMessage(player.toString());
					player.sendMessage("test1");
					player.sendMessage(player + " ");
					break;
				case "worldedit_test":
					File file = new File(plugin.getDataFolder(), "schematics/Turret" + "1" + ".schem");
					Bukkit.getScheduler().runTask(plugin, () -> {
						try {
							plugin.getHookManager().getWorldEdit().pasteSchematic(file, player.getLocation());
						} catch (java.io.IOException exception) {
							plugin.getRosaLogger().warning("WorldEdit test failed: " + exception.getMessage());
						}
					});
					break;
				case "addpermission":
					if (user == null) {
						player.sendMessage("member is null");
						break;
					}
					plugin.getRoleManager().addPermission(user, VillagePermission.EFFECTS_TOGGLE);
					player.sendMessage("added perm");
					break;
				case "getPermissions":
					if (user == null) {
						player.sendMessage("member is null");
						break;
					}
					player.sendMessage(String.valueOf(plugin.getRoleManager().serialize(user)));
					break;
				case "villagemembers":
					player.sendMessage("provide a village");
					break;
				case "local":
					player.sendMessage("provide a node");
					break;
				case "dev":
					plugin.setDev(!plugin.isDev());
					player.sendMessage("Settings dev to " + (plugin.isDev() ? "Enabled" : "Disabled"));
					break;
				case "tutorial":
					plugin.getMessenger().animatedTitle(
							player,
							plugin.getGuiSettings().text("guis.tutorial.welcome.title",
									"&aThanks for using &f&lADVANCED&6&lVILLAGES"),
							plugin.getGuiSettings().text("guis.tutorial.welcome.subtitle",
									"&7Personalize your experience"),
							1
					);
					plugin.getRosaScheduler().runForEntityLater(player,
							() -> plugin.getGuiManager().openGUI(player, new TutorialGUI(plugin, false)),
							() -> {}, 4 * 20L);
					break;
			}
		}
		if (args.length == 2) {
			switch (args[1]) {
				case "random_name":
					player.sendMessage(ColorUtils.color("&7Random: &c" + generator.getRandomName()));
					break;
				case "villages_owner":
					player.sendMessage(ColorUtils.color("&7Villages:"));
					for (String s : plugin.getVillageManager().getVillageOwners()) {
						player.sendMessage(s);
					}
					break;
				case "villages_names":
					player.sendMessage(ColorUtils.color("&7Villages:"));
					for (String s : plugin.getVillageManager().getVillageNamesAsList()) {
						player.sendMessage(s);
					}
					break;
				case "villagemembers":
					Optional<Village> village = plugin.getVillageManager().findByName(args[1]);
					if (village.isEmpty()) {
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
				player.sendMessage(plugin.getRoleManager().toString(member2.getPermissions()));
			}
			if (args[0].equalsIgnoreCase("local")) {
				plugin.getVillageMessages().get(args[1]).sendPrefixed(player);
			}
		}
		if (args.length == 3) {
			if (args[1].equals("region")) {
				Optional<Village> village2 = plugin.getVillageManager().findByName(args[2]);
				if (village2.isEmpty()) {
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
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		if (args.length == 1) {
			return complete(args[0], List.of(
					"villages", "adv_title", "adv_actionbar", "test1", "worldedit_test",
					"addpermission", "villagemembers", "getPermissions", "memory_test",
					"local", "tutorial", "dev"
			));
		}
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("villages"))
				return complete(args[1], List.of("random_name", "villages_owner", "villages_names", "region"));
			if (args[0].equalsIgnoreCase("getPermissions"))
				return onlinePlayers();
			if (args[0].equalsIgnoreCase("villagemembers"))
				return complete(args[1], plugin.getVillageManager().getVillageNamesAsList());
		}
		if (args.length == 3) {
			if (args[1].equalsIgnoreCase("villages_owner"))
				return complete(args[2], plugin.getVillageManager().getVillageOwners());
			if (args[1].equalsIgnoreCase("villages_names") || args[1].equalsIgnoreCase("region"))
				return complete(args[2], plugin.getVillageManager().getVillageNamesAsList());
		}
		return EMPTY;
	}
}
