package pl.kiosel.villages.gui.crafting;

import org.bukkit.entity.Player;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.rosacore.gui.RecipeGui;
import pl.kiosel.rosacore.material.RecipeBuilder;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.gui.GUIS;
import pl.kiosel.villages.gui.Item;

public class CraftingGUI extends Gui {

	public CraftingGUI(AdvancedVillages plugin) {
		setRows(3);
		setTitle(plugin.getGuiSettings().menu(GUIS.CRAFTING).getTitle());

		setDefaultItem(Item.blank(Item.Blank.WHITE));

		setButton(2, 3, plugin.getApi().createVillageBlock(),
				event -> openRecipe(plugin, event.getPlayer(), plugin.getCraftingManager().getVillage()));
		setButton(2, 5, plugin.getApi().createDestroyer(),
				event -> openRecipe(plugin, event.getPlayer(), plugin.getCraftingManager().getDestroyer()));
		setButton(2, 7, plugin.getApi().createHearth(),
				event -> openRecipe(plugin, event.getPlayer(), plugin.getCraftingManager().getHearth()));

		if (plugin.isDev())
			setButton(2, 9, plugin.getApi().createOutpostBlock(),
				event -> openRecipe(plugin, event.getPlayer(), plugin.getCraftingManager().getOutpost()));

		setButton(3, 9, Item.blank(Item.Blank.EXIT), (event) -> event.getPlayer().closeInventory());
	}

	private void openRecipe(AdvancedVillages plugin, Player player, RecipeBuilder recipe) {
		RecipeGui gui = new RecipeGui(recipe, this)
				.title(plugin.getGuiSettings().menu(GUIS.CRAFTING_RECIPE).getTitle())
				.backgroundItem(Item.blank(Item.Blank.WHITE))
				.arrowItem(Item.blank(Item.Blank.RESULT))
				.backItem(Item.blank(Item.Blank.BACK))
				.setItems();
		plugin.getGuiManager().showGUI(player, gui);
	}
}
