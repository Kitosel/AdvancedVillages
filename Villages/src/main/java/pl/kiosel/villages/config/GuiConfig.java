package pl.kiosel.villages.config;

import pl.kiosel.core.utils.TextUtils;
import pl.kiosel.villages.AdvancedVillages;

import java.util.ArrayList;
import java.util.List;

import static pl.kiosel.core.utils.ColorUtils.tl;

public class GuiConfig {

    private final AdvancedVillages plugin;

    public GuiConfig(AdvancedVillages plugin) {
        this.plugin = plugin;
    }

    public static String gui_village;
    public static String gui_bank;
    public static String gui_store;
    public static String gui_resident;
    public static String gui_remove;
    public static String gui_upgrade;
    public static String gui_settings;
    public static String gui_effects;
	public static String gui_storage;
	public static String gui_member_settings;
    public static String gui_remove_member;

    public static String yes;
    public static String no;

	public static String on;
	public static String off;

	public static String delete_village;

    public static String guis_village_settings;
    public static List<String> guis_village_settings_lore;

    public static String guis_village_store;
    public static List<String> guis_village_store_lore;

	public static String guis_village_members;
	public static List<String> guis_village_members_lore;

	public static String guis_village_members_owner;
	public static List<String> guis_village_members_owner_lore;
	public static String guis_village_members_member;
	public static List<String> guis_village_members_member_lore;

	public static String guis_village_storage;
	public static List<String> guis_village_storage_lore;

	public static String guis_village_effects;
    public static List<String> guis_village_effects_lore;

	public static String guis_village_effects_regen;
	public static List<String> guis_village_effects_regen_lore;
	public static String guis_village_effects_speed;
	public static List<String> guis_village_effects_speed_lore;
	public static String guis_village_effects_jump;
	public static List<String> guis_village_effects_jump_lore;
	public static String guis_village_effects_haste;
	public static List<String> guis_village_effects_haste_lore;
	public static String guis_village_effects_paper;
	public static List<String> guis_village_effects_paper_lore;

    public static String guis_village_upgrade;
    public static List<String> guis_village_upgrade_lore;

	public static String guis_village_upgrade_button;
	public static List<String> guis_village_upgrade_button_lore;

	public static String guis_village_upgrade_info_button;
	public static String guis_village_upgrade_info_button_upgraded;
	public static List<String> guis_village_upgrade_info_lore;

	public static String guis_village_bank;
	public static List<String> guis_village_bank_lore;

	public static String guis_village_setting_pvp;
	public static List<String> guis_village_setting_pvp_lore;
	public static String guis_village_setting_tnt;
	public static List<String> guis_village_setting_tnt_lore;
	public static String guis_village_setting_animations;
	public static List<String> guis_village_setting_animations_lore;
	public static String guis_village_setting_tag;
	public static List<String> guis_village_setting_tag_lore;
	public static String guis_village_setting_tag_set;
	public static String guis_village_setting_tag_notset;
	public static String guis_village_setting_teleport;
	public static List<String> guis_village_setting_teleport_lore;
	public static String guis_village_setting_delete;
	public static List<String> guis_village_setting_delete_lore;

	public static String no_tag;

	public static String guis_village_bank_balance;
	public static String guis_village_bank_add;
	public static String guis_village_bank_remove;

	public static String guis_next;
	public static String guis_previous;
    public static String guis_exit;
    public static String guis_back;
    public static String guis_blank;

    public void setConfig() {
		plugin.getDebug().debug("Setting guis.yml");
        plugin.getGuiConfig().reloadConfig();

        gui_village = getString("gui-village", "Village");
        gui_bank = getString("gui-bank", "Village - &6&lBank");
        gui_store = getString("gui-store", "Village - &aStore");
        gui_resident = getString("gui-resident", "Village - &eMembers");
        gui_remove = getString("gui-remove", "Village - &c&lRemove");
        gui_upgrade = getString("gui-upgrade", "Village - &bUpgrade");
        gui_settings = getString("gui-settings", "Village - &cSettings");
        gui_effects = getString("gui-effects", "Village - &dEffects");
		gui_storage = getString("gui-storage", "Village - &2Storage");
		gui_member_settings = getString("gui-member-settings", "Village - &7Member Settings");
        gui_remove_member = getString("gui-remove_member", "&cRemove member");

		delete_village = getString("delete-village", "&7Do you want to delete village?");

        guis_village_settings = getString("guis.village.settings.name", "&lSettings");
        guis_village_settings_lore = getList("guis.village.settings.lore", TextUtils.of("&7Manage Village"));
        guis_village_store = getString("guis.village.store.name", "&3Store");
        guis_village_store_lore = getList("guis.village.store.lore", TextUtils.of("&7Village store"));
        guis_village_members = getString("guis.village.members.name", "&cMembers");
        guis_village_members_lore = getList("guis.village.members.lore", TextUtils.of("&7Manage members"));

		guis_village_storage = getString("guis.village.storage.name", "3Store");
		guis_village_storage_lore = getList("guis.village.storage.lore", TextUtils.of("&7Village store"));

        guis_village_effects = getString("guis.village.effects.name", "&dEffects");
        guis_village_effects_lore = getList("guis.village.effects.lore", TextUtils.of("&7Add effects for", "&7Members of village"));

		guis_village_effects_regen = getString("guis.village.effects.regeneration.name", "&cRegeneration");
		guis_village_effects_regen_lore = getList("guis.village.effects.regeneration.lore", TextUtils.of("&7Gives &cRegeneration &7effect", "&7Amplifier: &6%amplifier%"));
		guis_village_effects_speed = getString("guis.village.effects.speed.name", "&bSpeed");
		guis_village_effects_speed_lore = getList("guis.village.effects.speed.lore", TextUtils.of("&7Gives &bSpeed &7effect", "&7Amplifier: &6%amplifier%"));
		guis_village_effects_jump = getString("guis.village.effects.jump.name", "&aJump boost");
		guis_village_effects_jump_lore = getList("guis.village.effects.jump.lore", TextUtils.of("&7Gives &aJump boost &7effect", "&7Amplifier: &6%amplifier%"));
		guis_village_effects_haste = getString("guis.village.effects.haste.name", "&eHaste");
		guis_village_effects_haste_lore = getList("guis.village.effects.haste.lore", TextUtils.of("&7Gives &eHaste &7effect", "&7Amplifier: &6%amplifier%"));

		guis_village_effects_paper = getString("guis.village.effects.price_paper.name", "&eClick to buy");
		guis_village_effects_paper_lore = getList("guis.village.effects.price_paper.lore", TextUtils.of("&7Gives &e%effect% &7effect", "&7Price: &6%price%"));

		guis_village_members_owner = getString("guis.village.members.owner.name", "&6%PLAYER% &cOWNER");
		guis_village_members_owner_lore = getList("guis.village.members.owner.lore", TextUtils.of("&7Role: &cOwner", " ", "&7Last online: %last_online%", " "));
		guis_village_members_member = getString("guis.village.members.resident.name", "&&7Role: &cMember");
		guis_village_members_member_lore = getList("guis.village.members.resident.lore", TextUtils.of("&7Role: &cMember", " ", "&7Last online: %last_online%", " "));

		guis_village_upgrade = getString("guis.village.upgrade.name", "&bUpgrade");
        guis_village_upgrade_lore = getList("guis.village.upgrade.lore", TextUtils.of("&7Upgrade village"));
		guis_village_upgrade_button = getString("guis.village.upgrade.upgrade-button.name", "&aUpgrade village");
		guis_village_upgrade_button_lore = getList("guis.village.upgrade.upgrade-button.lore", TextUtils.of("&7Actual level: &e%level%", "&7Cost: &a%cost%", "&cClick to &6Upgrade"));
		guis_village_upgrade_info_button = getString("guis.village.upgrade.info.name", "&cTo upgrade:");
		guis_village_upgrade_info_button_upgraded = getString("guis.village.upgrade.info.name-upgraded", "&bUpgraded");
		guis_village_upgrade_info_lore = getList("guis.village.upgrade.info.lore", TextUtils.of("&7Level &c%level% &f-> &e%next_level%"));

		guis_village_bank = getString("guis.village.bank.name", "&6Bank");
        guis_village_bank_lore = getList("guis.village.bank.lore", TextUtils.of("&7Village bank"));
		guis_village_bank_balance = getString("guis.village.bank.account_balance.name", "&6Bank&7: %village_balance%");
		guis_village_bank_add = getString("guis.village.bank.add.name", "&aAdd to bank");
		guis_village_bank_remove = getString("guis.village.bank.remove.name", "&cRemove from bank");

		guis_village_setting_pvp = getString("guis.village.settings.pvp.name", "&f&lPVP");
		guis_village_setting_pvp_lore = getList("guis.village.settings.pvp.lore", TextUtils.of("&8——————————", " ", "&7Pvp: %village_pvp%", " ", "&8——————————"));
		guis_village_setting_tnt = getString("guis.village.settings.tnt.name", "&c&lTNT");
		guis_village_setting_tnt_lore = getList("guis.village.settings.tnt.lore", TextUtils.of("&8——————————", " ", "&7TNT: %village_tnt%", " ", "&8——————————"));
		guis_village_setting_animations = getString("guis.village.settings.animations.name", "&d&lAnimations");
		guis_village_setting_animations_lore = getList("guis.village.settings.animations.lore", TextUtils.of("&8——————————", " ", "&7Animations: %village_animations%", " ", "&8——————————"));
		guis_village_setting_tag = getString("guis.village.settings.tag.name", "&c&lTag");
		guis_village_setting_tag_lore = getList("guis.village.settings.tag.lore", TextUtils.of("&8——————————", " ", "&e&lTag: &c%village_tag%", " ", "%istagset%", "&8——————————"));
		guis_village_setting_tag_set = getString("guis.village.settings.tag_set", "&7Click to set");
		guis_village_setting_tag_notset = getString("guis.village.settings.tag_not_set", "&7Set");
		guis_village_setting_teleport = getString("guis.village.settings.teleport.name", "&b&lTeleport");
		guis_village_setting_teleport_lore = getList("guis.village.settings.teleport.lore", TextUtils.of("&8——————————", " ", "%village_teleport%", " ", "&7Click to set", "&8——————————"));
		guis_village_setting_delete = getString("guis.village.settings.delete.name", "&c&l&nDelete village");
		guis_village_setting_delete_lore = getList("guis.village.settings.delete.lore", TextUtils.of("&8——————————", " ", "&7Click to delete", " ", "&8——————————"));

		no_tag = getString("no-tag", "&cNONE");

		guis_next = getString("next", "&aNext page");
		guis_previous = getString("previous", "&cPrevious page");
        guis_exit = getString("exit", "&7Exit");
        guis_back = getString("back", "&cBack");
        guis_blank = getString("blank", "&7&kBlank");

		yes = getString("yes", "&aYes");
		no = getString("no", "&cNo");

		on = getString("yes", "&aON");
		off = getString("no", "&cOFF");
    }

    private String getString(String path, String def) {
        if(path == null) return tl(def);
		String message = plugin.getGuiConfig().getConfig().getString(path);
        if(message == null || message.isBlank()) return tl(def);

        return tl(message);
    }

    private List<String> getList(String path, List<String> def) {
        if(path == null) return def;
        List<String> lore = new ArrayList<>();

        for(String s : plugin.getGuiConfig().getConfig().getStringList(path)) {
            lore.add(tl(s));
        }
        return lore;
    }
}
