package pl.kiosel.villages.gui.village;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.logs.VillageLogType;
import pl.kiosel.villages.config.GuiItemConfig;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.config.Settings;
import pl.kiosel.villages.data.village.Permission;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.gui.GUIS;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;

import java.util.List;
import java.util.function.BooleanSupplier;

public final class EffectsInventory extends VillageMenu {

	public EffectsInventory(AdvancedVillages plugin, VillageGUIManager menus, Village village,
	                        Player player, Gui parent) {
		super(plugin, menus, village, player, GUIS.EFFECTS, parent);
		addBackButton();

		addEffect("regeneration", 19, 28, Material.FEATHER, village::isRegeneration,
				village::isRegenerationActive,
				() -> village.setRegenerationActive(!village.isRegenerationActive()),
				() -> village.setRegeneration(true), Settings.EFFECTS_REGENERATION_COST.getDouble(),
				Settings.EFFECTS_REGENERATION_AMPLIFIER.getInt(), "&cRegeneration", "effect-regeneration");
		addEffect("speed", 21, 30, Material.RABBIT_FOOT, village::isSpeed,
				village::isSpeedActive, () -> village.setSpeedActive(!village.isSpeedActive()),
				() -> village.setSpeed(true), Settings.EFFECTS_SPEED_COST.getDouble(),
				Settings.EFFECTS_SPEED_AMPLIFIER.getInt(), "&bSpeed", "effect-speed");
		addEffect("jump", 23, 32, Material.SLIME_BALL, village::isJump,
				village::isJumpActive, () -> village.setJumpActive(!village.isJumpActive()),
				() -> village.setJump(true), Settings.EFFECTS_JUMP_BOOST_COST.getDouble(),
				Settings.EFFECTS_JUMP_BOOST_AMPLIFIER.getInt(), "&aJump boost", "effect-jump");
		addEffect("haste", 25, 34, Material.GOLDEN_PICKAXE, village::isHaste,
				village::isHasteActive, () -> village.setHasteActive(!village.isHasteActive()),
				() -> village.setHaste(true), Settings.EFFECTS_HASTE_COST.getDouble(),
				Settings.EFFECTS_HASTE_AMPLIFIER.getInt(), "&eHaste", "effect-haste");
	}

	private void addEffect(String id, int toggleSlot, int purchaseSlot, Material material,
	                       BooleanSupplier purchased, BooleanSupplier active, Runnable toggleAction,
	                       Runnable purchaseAction, double price, int amplifier,
	                       String fallbackName, String setting) {
		double adjustedPrice = plugin.getDevelopmentManager().applyEffectCost(village, price);
		List<String> fallbackEffectLore = List.of(
				"&7Gives %effect% &7effect",
				"&7Amplifier: &6%amplifier%"
		);
		GuiItemConfig effect = plugin.getGuiSettings().item(GUIS.EFFECTS,
				"guis.village.effects." + id, toggleSlot, material, fallbackName, fallbackEffectLore);
		List<String> effectLore = replaceEffects(effect.getLore(), Double.toString(adjustedPrice),
				Integer.toString(amplifier), effect.getName());
		if (effect.isEnabled()) {
			setButton(effect.getSlot(), effect.createItem(effect.getName(), effectLore,
					effect.isGlow() || active.getAsBoolean()), event -> toggle(
					purchased, active, toggleAction, effect.getName(), setting));
		}

		String paperName = plugin.getGuiSettings().text(
				"guis.village.effects.price_paper.name", "&eClick to buy");
		List<String> paperLore = plugin.getGuiSettings().list(
				"guis.village.effects.price_paper.lore",
				List.of("&7Gives %effect% &7effect", "&7Price: &6%price%"));
		GuiItemConfig purchase = plugin.getGuiSettings().item(GUIS.EFFECTS,
				"guis.village.effects.purchase-" + id, purchaseSlot, Material.PAPER,
				paperName, paperLore);
		List<String> purchaseLore = replaceEffects(purchase.getLore(), Double.toString(adjustedPrice),
				Integer.toString(amplifier), effect.getName());
		if (purchase.isEnabled()) {
			setButton(purchase.getSlot(), purchase.createItem(purchase.getName(), purchaseLore,
					purchase.isGlow() || purchased.getAsBoolean()), event -> buy(
					purchased, purchaseAction, adjustedPrice, setting));
		}
	}

	private void toggle(BooleanSupplier purchased, BooleanSupplier active, Runnable action,
	                    String effectName, String setting) {
		if (!hasPermission(Permission.EFFECTS_TOGGLE)) return;
		if (!purchased.getAsBoolean()) {
			getVillageMessages().get(Lang.EFFECT_NOT_BUY).with("effect", effectName).sendPrefixed(viewer);
			return;
		}
		action.run();
		plugin.getLogManager().record(village, VillageLogType.SETTING_CHANGED, viewer,
				"setting", setting, "value", active.getAsBoolean());
		playToggleSound();
		reopen(GUIS.EFFECTS);
	}

	private void buy(BooleanSupplier purchased, Runnable action, double price, String setting) {
		if (!hasPermission(Permission.EFFECTS_BUY)) return;
		if (purchased.getAsBoolean()) {
			playSound(Sound.BLOCK_NOTE_BLOCK_GUITAR, 10, 0);
			return;
		}
		if (!plugin.getEconomy().hasBalance(viewer, price)) {
			double missing = price - plugin.getEconomy().getBalance(viewer);
			getVillageMessages().get(Lang.NO_MONEY)
					.with("money", missing).sendPrefixed(viewer);
			return;
		}
		plugin.getEconomy().withdrawBalance(viewer, price);
		action.run();
		plugin.getLogManager().record(village, VillageLogType.SETTING_CHANGED, viewer,
				"setting", setting, "value", "purchased");
		playSound(Sound.BLOCK_NOTE_BLOCK_PLING, 10, 2);
		reopen(GUIS.EFFECTS);
	}
}
