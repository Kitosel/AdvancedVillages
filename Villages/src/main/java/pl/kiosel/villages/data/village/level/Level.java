package pl.kiosel.villages.data.village.level;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.dependencies.com.cryptomorin.xseries.XMaterial;

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

	private final Map<XMaterial, Integer> materials;

	Level(int level, int costExperience, int costEconomy, int size, Map<XMaterial, Integer> materials) {
		this.level = level;
		this.costExperience = costExperience;
		this.costEconomy = costEconomy;
		this.size = size;
		this.materials = materials;
	}

	public Map<XMaterial, Integer> getMaterials() {
		return Collections.unmodifiableMap(this.materials);
	}

	public List<ItemStack> getItemMaterials() {
		List<ItemStack> itemStacks = new ArrayList<>();
		for (XMaterial material : materials.keySet()) {
			if (material.get() == null) continue;
			ItemStack stack = new ItemStack(material.get(), materials.get(material));
			itemStacks.add(stack);
		}
		return itemStacks;
	}
}
