package pl.kiosel.villages.commands;

import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kiosel.core.chat.AdventureUtils;
import pl.kiosel.core.commands.SimpleCommand;
import pl.kiosel.core.database.Callback;
import pl.kiosel.core.dependencies.net.kyori.adventure.title.Title;
import pl.kiosel.core.hooks.WorldEditHook;
import pl.kiosel.core.utils.TabUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.enums.Permission;
import pl.kiosel.villages.data.village.VillageMember;
import pl.kiosel.villages.manager.VillageNameGenerator;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CommandTest extends SimpleCommand {

	private final AdvancedVillages plugin;
	private final VillageNameGenerator generator;

	public CommandTest(AdvancedVillages plugin) {
		super("test", List.of("testing"), "wioski.testingcommand");
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

		VillageMember member = plugin.getVillageDataManager().getVillageMember(player.getUniqueId());
		if (args.length == 1) {
			switch (args[0]) {
				case "anvil":
					new AnvilGUI.Builder()
							.onClose(stateSnapshot -> {
								stateSnapshot.getPlayer().sendMessage("You closed the inventory.");
							})
							.onClick((slot, stateSnapshot) -> { // Either use sync or async variant, not both
								if (slot != AnvilGUI.Slot.OUTPUT) {
									return Collections.emptyList();
								}

								if (stateSnapshot.getText().equalsIgnoreCase("you")) {
									stateSnapshot.getPlayer().sendMessage("You have magical powers!");
									return List.of(AnvilGUI.ResponseAction.close());
								} else {
									return List.of(AnvilGUI.ResponseAction.replaceInputText("Try again"));
								}
							})
							.preventClose()                                         //prevents the inventory from being closed
							.text("What is the meaning of life?")                   //sets the text the GUI should start with
							.title("Enter your answer.")                            //set the title of the GUI (only works in 1.14+)
							.plugin(plugin)                                         //set the plugin instance
							.open(player);                                          //opens the GUI for the player provided
					break;
				case "villages":
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
					OfflinePlayer player1 = Bukkit.getOfflinePlayer(UUID.fromString("cb8b7c68-1787-3a6e-aebb-221c0218b1bb"));
					player.sendMessage("player: " + player1.getName());
					break;
				case "worldedit_test":
					File file = new File(plugin.getDataFolder(), "schematics/Turret" + "1" + ".schem");
					Bukkit.getScheduler().runTask(plugin, () -> WorldEditHook.pasteSchematic(file, player.getLocation()));
					break;
				case "addpermission":
					if (member == null) {
						player.sendMessage("member is null");
						break;
					}
					plugin.getPermissionManager().addPermission(member, Permission.EFFECTS_TOGGLE);
					player.sendMessage("added perm");
					break;
				case "villagemember":
					plugin.getDatabaseUserManager().getUser(player.getUniqueId(), new Callback<>(plugin) {
						@Override
						public void onResult(VillageMember result) {
							if (result == null) {
								player.sendMessage("You dont have village");
							} else {
								player.sendMessage("You are in village: " + result.getVillage().getVillageName());
							}
						}
					});
					break;
				case "getPermissions":
					if (member == null) {
						player.sendMessage("member is null");
						break;
					}
					player.sendMessage(plugin.getPermissionManager().toString(member.getPermissions()));
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
					for (String s : plugin.getVillageDataManager().getVillageOwners()) {
						player.sendMessage(s);
					}
					break;
				case "villages_names":
					player.sendMessage(tl("&7Villages:"));
					for (String s : plugin.getVillageDataManager().getVillageNamesAsList()) {
						player.sendMessage(s);
					}
					break;
			}
			if (args[0].equalsIgnoreCase("getPermissions")) {
				Player another = Bukkit.getPlayer(args[1]);
				if (another == null) {
					player.sendMessage("another is null");
					return false;
				}
				VillageMember member2 = plugin.getVillageDataManager().getVillageMember(another.getUniqueId());
				if (member2 == null) {
					player.sendMessage("member is null");
					return false;
				}
				player.sendMessage(plugin.getPermissionManager().toString(member2.getPermissions()));
			}
		}
		return false;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (args.length == 1) {
			return TabUtils.returnWith(args[0], List.of(
					"anvil", "villages", "adv_title", "adv_actionbar", "test1", "worldedit_test", "addpermission", "villagemember", "getPermissions"
			));
		}
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("villages"))
				return TabUtils.returnWith(args[1], List.of("random_name", "villages_owner", "villages_names"));
			if (args[0].equalsIgnoreCase("getPermissions"))
				return TabUtils.onlinePlayers();
		}
		if (args.length == 3) {
			if (args[1].equalsIgnoreCase("villages_owner"))
				return TabUtils.returnWith(args[2], plugin.getVillageDataManager().getVillageOwners());
			if (args[1].equalsIgnoreCase("villages_names"))
				return TabUtils.returnWith(args[2], plugin.getVillageDataManager().getVillageNamesAsList());
		}
		return TabUtils.returnEmpty();
	}
}