package pl.kiosel.villages.gui.village;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import pl.kiosel.villages.config.GuiConfig;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.gui.VillageMenu;
import pl.kiosel.villages.settings.Settings;
import pl.kiosel.villages.data.village.Village;

import java.util.List;

public class EffectsInventory extends VillageMenu {

	@Override
	public Inventory getInventory(Village village, Player player) {
		Inventory inventory = Bukkit.createInventory(player, GUIS.EFFECTS.getSize(), GUIS.EFFECTS.getName());
		for(int x = 0; x < 9; ++x) { inventory.setItem(x, blank(Item.Blank.WHITE)); }
		inventory.setItem(4, blank(Item.Blank.BACK));

		if (village == null) return inventory;

		List<String> regenList = replaceEffects(GuiConfig.guis_village_effects_regen_lore,
				String.valueOf(Settings.EFFECTS_REGENERATION_COST.getInt()),
				String.valueOf(Settings.EFFECTS_REGENERATION_AMPLIFIER.getInt()), GuiConfig.guis_village_effects_regen);
		List<String> speedList = replaceEffects(GuiConfig.guis_village_effects_speed_lore,
				String.valueOf(Settings.EFFECTS_SPEED_COST.getInt()),
				String.valueOf(Settings.EFFECTS_SPEED_AMPLIFIER.getInt()), GuiConfig.guis_village_effects_speed);
		List<String> jumpList = replaceEffects(GuiConfig.guis_village_effects_jump_lore,
				String.valueOf(Settings.EFFECTS_JUMP_BOOST_COST.getInt()),
				String.valueOf(Settings.EFFECTS_JUMP_BOOST_AMPLIFIER.getInt()), GuiConfig.guis_village_effects_jump);
		List<String> hasteList = replaceEffects(GuiConfig.guis_village_effects_haste_lore,
				String.valueOf(Settings.EFFECTS_HASTE_COST.getInt()),
				String.valueOf(Settings.EFFECTS_HASTE_AMPLIFIER.getInt()), GuiConfig.guis_village_effects_haste);

		inventory.setItem(19, create(Material.FEATHER, GuiConfig.guis_village_effects_regen, regenList, village.isRegenerationActive()));
		inventory.setItem(21, create(Material.RABBIT_FOOT, GuiConfig.guis_village_effects_speed, speedList, village.isSpeedActive()));
		inventory.setItem(23, create(Material.SLIME_BALL, GuiConfig.guis_village_effects_jump, jumpList, village.isJumpActive()));
		inventory.setItem(25, create(Material.GOLDEN_PICKAXE, GuiConfig.guis_village_effects_haste, hasteList, village.isHasteActive()));

		inventory.setItem(28, create(Material.PAPER, GuiConfig.guis_village_effects_paper, regenList, village.isRegeneration()));
		inventory.setItem(30, create(Material.PAPER, GuiConfig.guis_village_effects_paper, speedList, village.isSpeed()));
		inventory.setItem(32, create(Material.PAPER, GuiConfig.guis_village_effects_paper, jumpList, village.isJump()));
		inventory.setItem(34, create(Material.PAPER, GuiConfig.guis_village_effects_paper, hasteList, village.isHaste()));
		return inventory;
	}
}
