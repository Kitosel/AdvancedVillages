package pl.kiosel.villages.gui.crafting;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.core.dependencies.net.kyori.adventure.text.Component;
import pl.kiosel.core.gui.Gui;
import pl.kiosel.core.gui.GuiUtils;
import pl.kiosel.core.lootables.RecipeBuilder;
import pl.kiosel.dependencies.com.cryptomorin.xseries.XMaterial;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.enums.Lang;

import java.util.List;
import java.util.Map;

public class GUICreateCrafting extends Gui {

	private final Player player;
	private final Gui parent;

	public GUICreateCrafting(Player player, Gui parent, RecipeBuilder builder) {
		super(5, parent);
		this.player = player;
		this.parent = parent;

		setTitle("Crafting - Destroyer");
		openGui(builder);
	}

	private void openGui(RecipeBuilder recipe) {
		ItemStack glass1 = GuiUtils.getBorderItem(XMaterial.WHITE_STAINED_GLASS_PANE);

		int[] gridSlots = { 10, 11, 12,
							19, 20, 21,
							28, 29, 30};

		List<String> shape = recipe.getShape();
		Map<Character, ItemStack> ingredients = recipe.getIngredientItems();

		int i = 0;
		for (String row : shape) {
			for (char c : row.toCharArray()) {
				if (i >= gridSlots.length) break;
				int slot = gridSlots[i++];
				if (c == ' ') continue;

				ItemStack item = ingredients.get(c);
				if (item != null) {
					setItem(slot, item);
				}
			}
		}
		setItem(23, GuiUtils.createButtonItem(XMaterial.ARROW, Component.text("§e→")));
		setItem(25, recipe.getResult());

		setDefaultItem(glass1);
		setButton(4, 8, GuiUtils.createButtonItem(XMaterial.TIPPED_ARROW,
				AdvancedVillages.getInstance().getMessages().text(Lang.BACK)),
				event -> this.guiManager.showGUI(player, parent)
		);
	}
}
