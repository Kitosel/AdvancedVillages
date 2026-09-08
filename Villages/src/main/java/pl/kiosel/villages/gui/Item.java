package pl.kiosel.villages.gui;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.rosacore.compatibility.ZMaterial;
import pl.kiosel.rosacore.material.ItemCreator;
import pl.kiosel.villages.AdvancedVillages;

import java.util.Collections;
import java.util.List;

public class Item {

    public static ItemStack create(Material material, int amount, String name, List<String> lore, boolean glow) {
		ItemCreator itemCreator = ItemCreator.of(material).amount(amount).name(name).glow(glow)
				.hideAttributes().hideAll();
		if (lore != null) {
			itemCreator.lore(lore);
		}
        return itemCreator.makeMenuItem();
    }

	public static ItemStack create(Material mat, int amount, String name) {
		return create(mat, amount, name, null, false);
	}

	public static ItemStack create(Material mat, String name, List<String> lore, boolean glow) {
		return create(mat, 1, name, lore, glow);
	}

    public static ItemStack create(Material mat, String name, String lore) {
        return create(mat, name, Collections.singletonList(lore));
    }

    public static ItemStack create(Material mat, String name, List<String> lore) {
        return create(mat, 1, name, lore, false);
    }

	public static ItemStack create(Material mat, int amount, String name, List<String> lore) {
		return create(mat, amount, name, lore, false);
	}

    public static ItemStack create(Material mat, String name) {
        return create(mat, 1, name, null, false);
    }

	public static ItemStack addLoreToItemStack(ItemStack original, List<String> loreToAdd) {
		if (original == null || loreToAdd == null || loreToAdd.isEmpty()) return original;
		return ItemCreator.of(original).lore(loreToAdd).make();
	}

	public static ItemStack createNoPlaceNoCraft(Material material, String name, List<String> lore) {
		return ItemCreator.of(material).name(name).lore(lore)
				.amount(1)
				.nbtBoolean("noPlace", true)
				.hideAll().hideAttributes()
				.make();
	}

	public static ItemStack createDestroyer(Material material, String name, List<String> lore) {
		return ItemCreator.of(material).name(name).lore(lore)
				.nbtBoolean("villageDestroyer", true)
				.unbreakable().glow().hideAll().hideAttributes()
				.make();
	}

	public static ItemStack createHead(OfflinePlayer player, String name, List<String> lore) {
		return ItemCreator.playerHead().name(name).lore(lore)
				.nbtString("player", player.getName())
				.nbtUUID("uuid", player.getUniqueId())
				.skullOwner(player)
				.hideAll().hideAttributes()
				.make();
	}

    public static ItemStack blank(Blank blank) {
        AdvancedVillages plugin = AdvancedVillages.getInstance();
        Material fallbackMaterial = blank.getMaterial().getMaterial().orElse(Material.PAPER);
        if (plugin == null)
			return create(fallbackMaterial, blank.getFallbackName());

		String path = "guis.common." + blank.getConfigId();
		String configuredMaterial = plugin.getGuiConfig().getString(path + ".material", fallbackMaterial.name());
		Material material = Material.matchMaterial(configuredMaterial);
		if (material == null || material.isAir()) material = fallbackMaterial;
		return create(material, plugin.getGuiSettings().text(path + ".name", blank.getFallbackName()));
    }

    public enum Blank {
		WHITE(ZMaterial.WHITE_STAINED_GLASS_PANE, "blank-white", "&7&kBlank"),
		GRAY(ZMaterial.GRAY_STAINED_GLASS, "blank-gray", "&7&kBlank"),
		BLACK(ZMaterial.BLACK_STAINED_GLASS, "blank-black", "&7&kBlank"),
		BACK(ZMaterial.ARROW, "back", "&9Back"),
		NEXT_PAGE(ZMaterial.SPECTRAL_ARROW, "next-page", "&9Next"),
		PREVIUS_PAGE(ZMaterial.SPECTRAL_ARROW, "previous-page", "&cPrevious"),
		EXIT(ZMaterial.ARROW, "exit", "&cExit");

        @Getter private final ZMaterial material;
		@Getter private final String configId;
		@Getter private final String fallbackName;

		Blank(ZMaterial material, String configId, String fallbackName) {
			this.material = material;
			this.configId = configId;
			this.fallbackName = fallbackName;
		}
    }
}
