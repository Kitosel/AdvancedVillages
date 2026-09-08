package pl.kiosel.villages.gui.village;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.rosacore.utils.NumberUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.GuiItemConfig;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.VillageSpecialization;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.gui.GUIS;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;
import pl.kiosel.villages.manager.SpecializationManager;

import java.time.Duration;
import java.util.List;

public final class SpecializationGUI extends VillageMenu {

	private final User user;
	private final SpecializationManager manager;

	public SpecializationGUI(AdvancedVillages plugin, VillageGUIManager menus, Village village,
							 Player player, Gui parent) {
		super(plugin, menus, village, player, GUIS.SPECIALIZATIONS, parent);
		this.user = plugin.getUserManager().findByPlayer(player).orElse(null);
		this.manager = plugin.getSpecializationManager();
		addBackButton();
		if (this.user == null) return;

		for (VillageSpecialization specialization : this.manager.getAvailable()) {
			addSpecialization(specialization);
		}
	}

	private void addSpecialization(VillageSpecialization specialization) {
		String path = "guis.specializations." + specialization.getId();
		GuiItemConfig item = plugin.getGuiSettings().item(
				GUIS.SPECIALIZATIONS, path, defaultSlot(specialization), defaultMaterial(specialization),
				defaultName(specialization), defaultLore(specialization));
		if (!item.isEnabled()) return;

		boolean selected = this.user.getSpecialization().filter(specialization::equals).isPresent();
		Duration remaining = this.manager.getRemainingCooldown(this.user);
		String status = selected
				? plugin.getGuiSettings().text("guis.specializations.status.selected", "&aSelected")
				: remaining.isZero()
					? plugin.getGuiSettings().text("guis.specializations.status.available", "&eClick to select")
					: plugin.getGuiSettings().text("guis.specializations.status.cooldown", "&cAvailable in %time%")
						.replace("%time%", plugin.getVillageMessages().formatDuration(remaining));
		String bonus = NumberUtils.formatNumber(this.manager.getBonus(specialization), 1);
		List<String> lore = item.getLore().stream()
				.map(line -> line.replace("%bonus%", bonus).replace("%status%", status))
				.toList();
		setButton(item.getSlot(), item.createItem(item.getName(), lore, item.isGlow() || selected),
				event -> select(specialization));
	}

	private void select(VillageSpecialization specialization) {
		if (this.user.getSpecialization().filter(specialization::equals).isPresent()) return;
		Duration remaining = this.manager.getRemainingCooldown(this.user);
		if (!remaining.isZero()) {
			plugin.getVillageMessages().get(Lang.SPECIALIZATION_COOLDOWN)
					.with("time", plugin.getVillageMessages().formatDuration(remaining))
					.sendPrefixed(viewer);
			return;
		}
		if (!this.manager.select(this.user, specialization)) return;
		plugin.getVillageMessages().get(Lang.SPECIALIZATION_SELECTED)
				.with("specialization", this.manager.getDisplayName(specialization))
				.sendPrefixed(viewer);
		playToggleSound();
		reopen(GUIS.SPECIALIZATIONS);
	}

	private static int defaultSlot(VillageSpecialization specialization) {
		return 10 + specialization.ordinal();
	}

	private static Material defaultMaterial(VillageSpecialization specialization) {
		switch (specialization) {
			case WARRIOR: return Material.IRON_SWORD;
			case DEFENDER: return Material.SHIELD;
			case FARMER: return Material.WHEAT;
			case FORESTER: return Material.OAK_SAPLING;
			case MINER: return Material.IRON_PICKAXE;
			case MERCHANT: return Material.EMERALD;
			case HEALER: return Material.GOLDEN_APPLE;
			default: return Material.PAPER;
		}
	}

	private static String defaultName(VillageSpecialization specialization) {
		switch (specialization) {
			case WARRIOR: return "&cWarrior";
			case DEFENDER: return "&9Defender";
			case FARMER: return "&eFarmer";
			case FORESTER: return "&2Forester";
			case MINER: return "&7Miner";
			case MERCHANT: return "&aMerchant";
			case HEALER: return "&dHealer";
			default: return specialization.getId();
		}
	}

	private static List<String> defaultLore(VillageSpecialization specialization) {
		switch (specialization) {
			case WARRIOR: return List.of("&7Deal &a%bonus%% &7more damage.", "", "%status%");
			case DEFENDER: return List.of("&7Receive &a%bonus%% &7less damage", "&7inside your village.", "", "%status%");
			case FARMER: return List.of("&a%bonus%% &7chance for double crops.", "", "%status%");
			case FORESTER: return List.of("&a%bonus%% &7chance to prevent axe", "&7durability loss.", "", "%status%");
			case MINER: return List.of("&7Gain &a%bonus%% &7more experience", "&7from ores.", "", "%status%");
			case MERCHANT: return List.of("&a%bonus%% &7discount in the village store.", "", "%status%");
			case HEALER: return List.of("&7Receive &a%bonus%% &7more healing.", "", "%status%");
			default: return List.of("%status%");
		}
	}
}
