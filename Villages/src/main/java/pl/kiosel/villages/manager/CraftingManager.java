package pl.kiosel.villages.manager;

import lombok.Getter;
import org.bukkit.inventory.Recipe;
import pl.kiosel.rosacore.compatibility.ZMaterial;
import pl.kiosel.rosacore.material.RecipeBuilder;
import pl.kiosel.villages.AdvancedVillages;

public class CraftingManager {

	private final AdvancedVillages plugin;

	@Getter private RecipeBuilder village;
	@Getter private RecipeBuilder destroyer;
	@Getter private RecipeBuilder hearth;

	private Recipe villageRecipe;
	private Recipe destroyerRecipe;
	private Recipe hearthRecipe;

	public CraftingManager(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	public void createRecipe() {
		plugin.getDebug().debug("Creating recipe");
		village = new RecipeBuilder(plugin, "village")
				.shape( "%^%",
						"^&^",
						"#%#")
				.setIngredient('^', ZMaterial.DIAMOND_BLOCK)
				.setIngredient('%', ZMaterial.GOLD_BLOCK)
				.setIngredient('#', ZMaterial.AMETHYST_BLOCK)
				.setIngredient('&', ZMaterial.HONEY_BLOCK)
				.setResult(plugin.getApi().createVillageBlock());
		destroyer = new RecipeBuilder(plugin, "destroyer")
				.shape( "%^%",
						" & ",
						" & ")
				.setIngredient('^', plugin.getApi().createDestroyerHearth())
				.setIngredient('%', ZMaterial.GOLD_INGOT)
				.setIngredient('&', ZMaterial.STICK)
				.setResult(plugin.getApi().createDestroyer());
		hearth = new RecipeBuilder(plugin, "hearth")
				.shape( "^&^",
						"#%#",
						"^&^")
				.setIngredient('^', plugin.getApi().createHearthPart())
				.setIngredient('%', ZMaterial.TOTEM_OF_UNDYING)
				.setIngredient('&', ZMaterial.DIAMOND_BLOCK)
				.setIngredient('#', ZMaterial.NETHERITE_INGOT)
				.setResult(plugin.getApi().createHearth());
	}

	public void registerRecipe() {
		plugin.getDebug().debug("Register recipe");
		if (village!=null)
			villageRecipe = village.build();
		if (destroyer!=null)
			destroyerRecipe = destroyer.build();
		if (hearth!=null)
			hearthRecipe = hearth.build();
	}

	public void unRegisterRecipe() {
		plugin.getDebug().debug("Unregister recipe");
		if (village!=null)
			village.unregister();
		if (destroyer!=null)
			destroyer.unregister();
		if (hearth!=null)
			hearth.unregister();
	}

	public boolean isCustomRecipe(Recipe recipe) {
		return recipe.getResult().equals(destroyerRecipe.getResult()) ||
				recipe.getResult().equals(hearthRecipe.getResult()) ||
				recipe.getResult().equals(villageRecipe.getResult());
	}
}