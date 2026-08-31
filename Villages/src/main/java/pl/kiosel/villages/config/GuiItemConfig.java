package pl.kiosel.villages.config;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.villages.gui.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public final class GuiItemConfig {

	private final String id;
	private final int slot;
	private final Material material;
	private final int amount;
	private final String name;
	private final List<String> lore;
	private final boolean glow;
	private final boolean enabled;

	GuiItemConfig(String id, int slot, Material material, int amount, String name,
	              List<String> lore, boolean glow, boolean enabled) {
		this.id = id;
		this.slot = slot;
		this.material = material;
		this.amount = amount;
		this.name = name;
		this.lore = Collections.unmodifiableList(new ArrayList<>(lore));
		this.glow = glow;
		this.enabled = enabled;
	}

	public ItemStack createItem() {
		return Item.create(this.material, this.amount, this.name, this.lore, this.glow);
	}

	public ItemStack createItem(String name, List<String> lore) {
		return Item.create(this.material, this.amount, name, lore, this.glow);
	}

	public ItemStack createItem(String name, List<String> lore, boolean glow) {
		return Item.create(this.material, this.amount, name, lore, glow);
	}
}
