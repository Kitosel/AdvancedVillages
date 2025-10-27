package pl.kiosel.villages.gui.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import pl.kiosel.villages.config.Config;
import pl.kiosel.villages.config.GuiConfig;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.common.Item;
import pl.kiosel.villages.gui.VillageMenu;
import pl.kiosel.villages.village.Village;

public class EffectsInventory extends VillageMenu {

	@Override
	public Inventory getInventory(Village village, Player player) {
		Inventory inventory = Bukkit.createInventory(player, GUIS.EFFECTS.getSize(), GUIS.EFFECTS.getName());
		for(int x = 0; x < 9; ++x) { inventory.setItem(x, blank(Item.Blank.WHITE)); }
		inventory.setItem(4, blank(Item.Blank.BACK));

		if (village == null) return inventory;

		inventory.setItem(19, create(Material.FEATHER, GuiConfig.guis_village_effects_regen,
				replaceEffects(GuiConfig.guis_village_effects_regen_lore,
						Config.effect_regeneration_cost+"", Config.effect_regeneration_amplifier+"", GuiConfig.guis_village_effects_regen), village.isRegenerationActive()));
		inventory.setItem(21, create(Material.RABBIT_FOOT, GuiConfig.guis_village_effects_speed,
				replaceEffects(GuiConfig.guis_village_effects_speed_lore,
						Config.effect_speed_cost+"", Config.effect_speed_amplifier+"", GuiConfig.guis_village_effects_speed), village.isSpeedActive()));
		inventory.setItem(23, create(Material.SLIME_BALL, GuiConfig.guis_village_effects_jump,
				replaceEffects(GuiConfig.guis_village_effects_jump_lore,
						Config.effect_jump_cost+"", Config.effect_jump_amplifier+"", GuiConfig.guis_village_effects_jump), village.isJumpActive()));
		inventory.setItem(25, create(Material.GOLDEN_PICKAXE, GuiConfig.guis_village_effects_haste,
				replaceEffects(GuiConfig.guis_village_effects_haste_lore,
						Config.effect_haste_cost+"", Config.effect_haste_amplifier+"", GuiConfig.guis_village_effects_haste), village.isHasteActive()));

		inventory.setItem(28, create(Material.PAPER, GuiConfig.guis_village_effects_paper,
				replaceEffects(GuiConfig.guis_village_effects_regen_lore,
						Config.effect_regeneration_cost+"", Config.effect_regeneration_amplifier+"", GuiConfig.guis_village_effects_regen), village.isRegeneration()));
		inventory.setItem(30, create(Material.PAPER, GuiConfig.guis_village_effects_paper,
				replaceEffects(GuiConfig.guis_village_effects_speed_lore,
						Config.effect_speed_cost+"", Config.effect_speed_amplifier+"", GuiConfig.guis_village_effects_speed), village.isSpeed()));
		inventory.setItem(32, create(Material.PAPER, GuiConfig.guis_village_effects_paper,
				replaceEffects(GuiConfig.guis_village_effects_jump_lore,
						Config.effect_jump_cost+"", Config.effect_jump_amplifier+"", GuiConfig.guis_village_effects_jump), village.isJump()));
		inventory.setItem(34, create(Material.PAPER, GuiConfig.guis_village_effects_paper,
				replaceEffects(GuiConfig.guis_village_effects_haste_lore,
						Config.effect_haste_cost+"", Config.effect_haste_amplifier+"", GuiConfig.guis_village_effects_haste), village.isHaste()));
		return inventory;
	}
}
