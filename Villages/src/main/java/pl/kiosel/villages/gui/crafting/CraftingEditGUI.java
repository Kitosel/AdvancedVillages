package pl.kiosel.villages.gui.crafting;

import org.bukkit.entity.Player;
import pl.kiosel.rosacore.gui.CraftingEditorGui;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.manager.CraftingManager;

public class CraftingEditGUI extends Gui {

	public CraftingEditGUI(AdvancedVillages plugin) {
		setRows(3);
		setTitle(plugin.getGuiSettings().text("guis.crafting.title", "Crafting"));
		setDefaultItem(Item.blank(Item.Blank.WHITE));

		setButton(2, 3, plugin.getApi().createVillageBlock(),
				event -> openRecipe(plugin, event.getPlayer(), CraftingManager.RecipeType.VILLAGE));
		setButton(2, 5, plugin.getApi().createDestroyer(),
				event -> openRecipe(plugin, event.getPlayer(), CraftingManager.RecipeType.DESTROYER));
		setButton(2, 7, plugin.getApi().createHearth(),
				event -> openRecipe(plugin, event.getPlayer(), CraftingManager.RecipeType.HEARTH));

		setButton(3, 9, Item.blank(Item.Blank.EXIT), (event) -> event.getPlayer().closeInventory());
	}

	private void openRecipe(AdvancedVillages plugin, Player player, CraftingManager.RecipeType recipeType) {
		String title = plugin.getGuiSettings()
				.text("guis.crafting.editor-title", "&8Edit recipe: %recipe%")
				.replace("%recipe%", recipeType.getKey());
		CraftingEditorGui editorGui = new CraftingEditorGui(recipeType.getKey(), this)
				.loadRecipe(plugin.getCraftingManager().getRecipeData(recipeType))
				.title(title)
				.closeOnSave(false);
		editorGui.clearActions(CraftingEditorGui.RESULT_SLOT);
		editorGui.setButton(CraftingEditorGui.CLEAR_SLOT,
				editorGui.getItem(CraftingEditorGui.CLEAR_SLOT), event -> {
					for (int index = 0; index < CraftingEditorGui.INGREDIENT_SLOTS.length; index++)
						editorGui.setIngredient(index, null);
				});
		editorGui.onError((editorPlayer, message) ->
				plugin.getVillageMessages().sendPrefixed(editorPlayer, Lang.CRAFTING_RECIPE_INVALID));
		editorGui.onSave((editorPlayer, data) -> {
			if (plugin.getCraftingManager().replaceRecipe(recipeType, data)) {
				plugin.getVillageMessages().sendPrefixed(editorPlayer, Lang.CRAFTING_RECIPE_SAVED,
						"recipe", recipeType.getKey());
				editorGui.close();
				return;
			}
			plugin.getVillageMessages().sendPrefixed(editorPlayer, Lang.CRAFTING_RECIPE_SAVE_FAILED,
					"recipe", recipeType.getKey());
		});

		plugin.getGuiManager().showGUI(player, editorGui);
	}
}
