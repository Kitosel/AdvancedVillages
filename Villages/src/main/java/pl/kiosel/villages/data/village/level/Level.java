package pl.kiosel.villages.data.village.level;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.rosacore.compatibility.ZMaterial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Level {

	@Getter @Setter
	private int level;
	@Getter @Setter
	private int costExperience;
	@Getter @Setter
	private int costEconomy;
	@Getter @Setter
	private int size;

	private final Map<ZMaterial, Integer> materials;

	Level(int level, int costExperience, int costEconomy, int size, Map<ZMaterial, Integer> materials) {
		this.level = level;
		this.costExperience = costExperience;
		this.costEconomy = costEconomy;
		this.size = size;
		this.materials = materials;
	}

	public Map<ZMaterial, Integer> getMaterials() {
		return Collections.unmodifiableMap(this.materials);
	}

	public List<ItemStack> getItemMaterials() {
		List<ItemStack> itemStacks = new ArrayList<>();
		for (ZMaterial material : materials.keySet()) {
			if (material.getMaterial().isEmpty()) continue;
			ItemStack stack = new ItemStack(material.getMaterial().orElseThrow(), materials.get(material));
			itemStacks.add(stack);
		}
		return itemStacks;
	}
}
