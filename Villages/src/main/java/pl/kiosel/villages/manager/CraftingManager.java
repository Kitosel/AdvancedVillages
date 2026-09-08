package pl.kiosel.villages.manager;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import pl.kiosel.rosacore.compatibility.ZMaterial;
import pl.kiosel.rosacore.config.ConfigSaveResult;
import pl.kiosel.rosacore.config.RosaConfig;
import pl.kiosel.rosacore.material.CraftingRecipeData;
import pl.kiosel.rosacore.material.CraftingRecipeStore;
import pl.kiosel.rosacore.material.RecipeBuilder;
import pl.kiosel.villages.AdvancedVillages;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public final class CraftingManager {

	public enum RecipeType {
		VILLAGE("village", false),
		DESTROYER("destroyer", true),
		HEARTH("hearth", true);

		@Getter
		private final String key;
		private final boolean exactIngredients;

		RecipeType(String key, boolean exactIngredients) {
			this.key = key;
			this.exactIngredients = exactIngredients;
		}
	}

	private final AdvancedVillages plugin;
	private final CraftingRecipeStore recipeStore;
	private final Map<RecipeType, CraftingRecipeData> recipeData = new EnumMap<>(RecipeType.class);
	private final Map<RecipeType, RecipeBuilder> recipeBuilders = new EnumMap<>(RecipeType.class);
	private final Map<RecipeType, Recipe> registeredRecipes = new EnumMap<>(RecipeType.class);

	public CraftingManager(AdvancedVillages plugin) {
		this.plugin = Objects.requireNonNull(plugin, "plugin");
		this.recipeStore = new CraftingRecipeStore(new RosaConfig(plugin, "recipes.yml"));
	}

	public void createRecipe() {
		this.plugin.getDebug().debug("Creating recipes");
		for (RecipeType type : RecipeType.values()) {
			CraftingRecipeData data = this.loadRecipe(type);
			this.recipeData.put(type, data);
			this.recipeBuilders.put(type, data.toRecipeBuilder(this.plugin));
		}
	}

	public void registerRecipe() {
		this.plugin.getDebug().debug("Register recipes");
		this.registeredRecipes.clear();
		for (RecipeType type : RecipeType.values()) {
			RecipeBuilder builder = this.recipeBuilders.get(type);
			if (builder != null)
				this.registeredRecipes.put(type, builder.build());
		}
	}

	public void unRegisterRecipe() {
		this.plugin.getDebug().debug("Unregister recipes");
		for (RecipeBuilder builder : this.recipeBuilders.values())
			builder.unregister();
		this.registeredRecipes.clear();
	}

	public boolean replaceRecipe(RecipeType type, CraftingRecipeData editedData) {
		Objects.requireNonNull(type, "type");
		Objects.requireNonNull(editedData, "editedData");

		CraftingRecipeData replacementData = this.withFixedIdentity(type, editedData);
		RecipeBuilder previousBuilder = this.recipeBuilders.get(type);
		CraftingRecipeData previousData = this.recipeData.get(type);
		RecipeBuilder replacementBuilder = replacementData.toRecipeBuilder(this.plugin);

		if (previousBuilder != null)
			previousBuilder.unregister();
		this.registeredRecipes.remove(type);

		Recipe replacementRecipe;
		try {
			replacementRecipe = replacementBuilder.build();
		} catch (RuntimeException exception) {
			this.restoreRecipe(type, previousData, previousBuilder);
			this.plugin.getRosaLogger().log(Level.WARNING,
					"Could not register edited crafting recipe " + type.getKey(), exception);
			return false;
		}

		ConfigSaveResult saveResult = this.recipeStore.save(replacementData);
		if (!saveResult.isSuccess()) {
			replacementBuilder.unregister();
			this.restoreRecipe(type, previousData, previousBuilder);
			this.recipeStore.reload();
			this.plugin.getRosaLogger().warning("Could not save crafting recipe " + type.getKey()
					+ ": " + saveResult.getProblems());
			if (saveResult.getCause() != null)
				this.plugin.getRosaLogger().log(Level.WARNING, "Recipe save failure", saveResult.getCause());
			return false;
		}

		this.recipeData.put(type, replacementData);
		this.recipeBuilders.put(type, replacementBuilder);
		this.registeredRecipes.put(type, replacementRecipe);
		return true;
	}

	public CraftingRecipeData getRecipeData(RecipeType type) {
		CraftingRecipeData data = this.recipeData.get(Objects.requireNonNull(type, "type"));
		if (data == null)
			throw new IllegalStateException("Crafting recipes have not been created yet");
		return data;
	}

	public RecipeBuilder getVillage() {
		return this.recipeBuilders.get(RecipeType.VILLAGE);
	}

	public RecipeBuilder getDestroyer() {
		return this.recipeBuilders.get(RecipeType.DESTROYER);
	}

	public RecipeBuilder getHearth() {
		return this.recipeBuilders.get(RecipeType.HEARTH);
	}

	public boolean isCustomRecipe(Recipe recipe) {
		if (recipe == null || recipe.getResult() == null)
			return false;
		for (Recipe registeredRecipe : this.registeredRecipes.values()) {
			if (registeredRecipe != null && registeredRecipe.getResult().isSimilar(recipe.getResult()))
				return true;
		}
		return false;
	}

	private CraftingRecipeData loadRecipe(RecipeType type) {
		CraftingRecipeData fallback = this.createDefaultRecipe(type);
		try {
			return this.recipeStore.find(type.getKey())
					.map(data -> this.withFixedIdentity(type, data))
					.orElse(fallback);
		} catch (RuntimeException exception) {
			this.plugin.getRosaLogger().log(Level.WARNING,
					"Could not load crafting recipe " + type.getKey() + "; using its default", exception);
			return fallback;
		}
	}

	private CraftingRecipeData withFixedIdentity(RecipeType type, CraftingRecipeData data) {
		return new CraftingRecipeData(
				type.getKey(),
				data.getType(),
				data.hasExactIngredients(),
				data.getIngredients(),
				this.getResult(type)
		);
	}

	private CraftingRecipeData createDefaultRecipe(RecipeType type) {
		switch (type) {
			case VILLAGE:
				return this.recipe(type, new ItemStack[]{
						item(ZMaterial.GOLD_BLOCK), item(ZMaterial.DIAMOND_BLOCK), item(ZMaterial.GOLD_BLOCK),
						item(ZMaterial.DIAMOND_BLOCK), item(ZMaterial.HONEY_BLOCK), item(ZMaterial.DIAMOND_BLOCK),
						item(ZMaterial.AMETHYST_BLOCK), item(ZMaterial.GOLD_BLOCK), item(ZMaterial.AMETHYST_BLOCK)
				});
			case DESTROYER:
				return this.recipe(type, new ItemStack[]{
						item(ZMaterial.GOLD_INGOT), this.plugin.getApi().createDestroyerHearth(), item(ZMaterial.GOLD_INGOT),
						null, item(ZMaterial.STICK), null,
						null, item(ZMaterial.STICK), null
				});
			case HEARTH:
				return this.recipe(type, new ItemStack[]{
						this.plugin.getApi().createHearthPart(), item(ZMaterial.DIAMOND_BLOCK), this.plugin.getApi().createHearthPart(),
						item(ZMaterial.NETHERITE_INGOT), item(ZMaterial.TOTEM_OF_UNDYING), item(ZMaterial.NETHERITE_INGOT),
						this.plugin.getApi().createHearthPart(), item(ZMaterial.DIAMOND_BLOCK), this.plugin.getApi().createHearthPart()
				});
			default:
				throw new IllegalArgumentException("Unsupported crafting recipe type: " + type);
		}
	}

	private CraftingRecipeData recipe(RecipeType type, ItemStack[] ingredients) {
		return new CraftingRecipeData(
				type.getKey(),
				CraftingRecipeData.ShapeType.SHAPED,
				type.exactIngredients,
				ingredients,
				this.getResult(type)
		);
	}

	private ItemStack getResult(RecipeType type) {
		switch (type) {
			case VILLAGE:
				return this.plugin.getApi().createVillageBlock();
			case DESTROYER:
				return this.plugin.getApi().createDestroyer();
			case HEARTH:
				return this.plugin.getApi().createHearth();
			default:
				throw new IllegalArgumentException("Unsupported crafting recipe type: " + type);
		}
	}

	private void restoreRecipe(RecipeType type, CraftingRecipeData previousData,
							   RecipeBuilder previousBuilder) {
		if (previousData == null || previousBuilder == null)
			return;
		try {
			Recipe restoredRecipe = previousBuilder.build();
			this.recipeData.put(type, previousData);
			this.recipeBuilders.put(type, previousBuilder);
			this.registeredRecipes.put(type, restoredRecipe);
		} catch (RuntimeException exception) {
			this.plugin.getRosaLogger().log(Level.SEVERE,
					"Could not restore previous crafting recipe " + type.getKey(), exception);
		}
	}

	private static ItemStack item(ZMaterial material) {
		return material.requireItem();
	}
}
