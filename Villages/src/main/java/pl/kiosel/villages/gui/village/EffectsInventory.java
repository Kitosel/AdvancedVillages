package pl.kiosel.villages.gui.village;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import pl.kiosel.core.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.GuiConfig;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.enums.Permission;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;
import pl.kiosel.villages.settings.Settings;

import java.util.List;
import java.util.function.BooleanSupplier;

public final class EffectsInventory extends VillageMenu {

	public EffectsInventory(AdvancedVillages plugin, VillageGUIManager menus, Village village,
	                        Player player, Gui parent) {
		super(plugin, menus, village, player, GUIS.EFFECTS, parent);
		addBackButton(4);

		List<String> regeneration = replaceEffects(GuiConfig.guis_village_effects_regen_lore,
				Integer.toString(Settings.EFFECTS_REGENERATION_COST.getInt()),
				Integer.toString(Settings.EFFECTS_REGENERATION_AMPLIFIER.getInt()),
				GuiConfig.guis_village_effects_regen);
		List<String> speed = replaceEffects(GuiConfig.guis_village_effects_speed_lore,
				Integer.toString(Settings.EFFECTS_SPEED_COST.getInt()),
				Integer.toString(Settings.EFFECTS_SPEED_AMPLIFIER.getInt()),
				GuiConfig.guis_village_effects_speed);
		List<String> jump = replaceEffects(GuiConfig.guis_village_effects_jump_lore,
				Integer.toString(Settings.EFFECTS_JUMP_BOOST_COST.getInt()),
				Integer.toString(Settings.EFFECTS_JUMP_BOOST_AMPLIFIER.getInt()),
				GuiConfig.guis_village_effects_jump);
		List<String> haste = replaceEffects(GuiConfig.guis_village_effects_haste_lore,
				Integer.toString(Settings.EFFECTS_HASTE_COST.getInt()),
				Integer.toString(Settings.EFFECTS_HASTE_AMPLIFIER.getInt()),
				GuiConfig.guis_village_effects_haste);

		setButton(19, Item.create(Material.FEATHER, GuiConfig.guis_village_effects_regen,
				regeneration, village.isRegenerationActive()), event -> toggle(
				village::isRegeneration, () -> village.setRegenerationActive(!village.isRegenerationActive()),
				GuiConfig.guis_village_effects_regen));
		setButton(21, Item.create(Material.RABBIT_FOOT, GuiConfig.guis_village_effects_speed,
				speed, village.isSpeedActive()), event -> toggle(
				village::isSpeed, () -> village.setSpeedActive(!village.isSpeedActive()),
				GuiConfig.guis_village_effects_speed));
		setButton(23, Item.create(Material.SLIME_BALL, GuiConfig.guis_village_effects_jump,
				jump, village.isJumpActive()), event -> toggle(
				village::isJump, () -> village.setJumpActive(!village.isJumpActive()),
				GuiConfig.guis_village_effects_jump));
		setButton(25, Item.create(Material.GOLDEN_PICKAXE, GuiConfig.guis_village_effects_haste,
				haste, village.isHasteActive()), event -> toggle(
				village::isHaste, () -> village.setHasteActive(!village.isHasteActive()),
				GuiConfig.guis_village_effects_haste));

		setButton(28, Item.create(Material.PAPER, GuiConfig.guis_village_effects_paper,
				regeneration, village.isRegeneration()), event -> buy(
				village::isRegeneration, () -> village.setRegeneration(true),
				Settings.EFFECTS_REGENERATION_COST.getDouble()));
		setButton(30, Item.create(Material.PAPER, GuiConfig.guis_village_effects_paper,
				speed, village.isSpeed()), event -> buy(
				village::isSpeed, () -> village.setSpeed(true), Settings.EFFECTS_SPEED_COST.getDouble()));
		setButton(32, Item.create(Material.PAPER, GuiConfig.guis_village_effects_paper,
				jump, village.isJump()), event -> buy(
				village::isJump, () -> village.setJump(true), Settings.EFFECTS_JUMP_BOOST_COST.getDouble()));
		setButton(34, Item.create(Material.PAPER, GuiConfig.guis_village_effects_paper,
				haste, village.isHaste()), event -> buy(
				village::isHaste, () -> village.setHaste(true), Settings.EFFECTS_HASTE_COST.getDouble()));
	}

	private void toggle(BooleanSupplier purchased, Runnable action, String effectName) {
		if (!hasPermission(Permission.EFFECTS_TOGGLE)) return;
		if (!purchased.getAsBoolean()) {
			plugin.getLocale().getMessage(Lang.EFFECT_NOT_BUY.getPath())
					.processPlaceholder("effect", effectName).sendPrefixedMessage(viewer);
			return;
		}
		action.run();
		playToggleSound();
		reopen(GUIS.EFFECTS);
	}

	private void buy(BooleanSupplier purchased, Runnable action, double price) {
		if (!hasPermission(Permission.EFFECTS_BUY)) return;
		if (purchased.getAsBoolean()) {
			viewer.playSound(viewer.getLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, 10, 0);
			return;
		}
		if (!plugin.getEconomy().hasBalance(viewer, price)) {
			double missing = price - plugin.getEconomy().getBalance(viewer);
			plugin.getLocale().getMessage(Lang.NO_MONEY.getPath())
					.processPlaceholder("money", missing).sendPrefixedMessage(viewer);
			return;
		}
		plugin.getEconomy().withdrawBalance(viewer, price);
		action.run();
		viewer.playSound(viewer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10, 2);
		reopen(GUIS.EFFECTS);
	}
}
