package pl.kiosel.villages.gui.crafting;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.rosacore.gui.RecipeGui;
import pl.kiosel.rosacore.material.RecipeBuilder;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.gui.Item;

public class GUICrafting extends Gui {

	public GUICrafting(AdvancedVillages plugin, Player player) {
		setRows(3);
		setTitle(plugin.getGuiSettings().text("guis.crafting.title", "Crafting"));
		setDefaultItem(Item.blank(Item.Blank.WHITE));

		setButton(2, 3, plugin.getApi().createVillageBlock(),
				event -> openRecipe(plugin, event.getPlayer(), plugin.getCraftingManager().getVillage()));
		setButton(2, 5, plugin.getApi().createDestroyer(),
				event -> openRecipe(plugin, event.getPlayer(), plugin.getCraftingManager().getDestroyer()));
		setButton(2, 7, plugin.getApi().createHearth(),
				event -> openRecipe(plugin, event.getPlayer(), plugin.getCraftingManager().getHearth()));

		setButton(3, 9, Item.blank(Item.Blank.EXIT), (event) -> player.closeInventory());
	}

	private void openRecipe(AdvancedVillages plugin, Player player, RecipeBuilder recipe) {
		RecipeGui gui = new RecipeGui(recipe, this)
				.title(plugin.getGuiSettings().text("guis.crafting.recipe-title", "Crafting recipe"))
				.backgroundItem(Item.create(Material.WHITE_STAINED_GLASS_PANE, " "))
				.arrowItem(Item.create(Material.ARROW,
						plugin.getGuiSettings().text("guis.crafting.result-arrow", "&e→")))
				.backItem(Item.create(Material.TIPPED_ARROW,
						plugin.getGuiSettings().text("guis.common.back.name", "&9Back")))
				.setItems();
		plugin.getGuiManager().showGUI(player, gui);
	}
}
