package pl.kiosel.villages.gui;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.kiosel.core.dependencies.de.tr7zw.nbtapi.NBT;
import pl.kiosel.core.dependencies.de.tr7zw.nbtapi.iface.ReadableItemNBT;
import pl.kiosel.core.utils.ItemCreator;
import pl.kiosel.dependencies.com.cryptomorin.xseries.XItemFlag;
import pl.kiosel.dependencies.com.cryptomorin.xseries.XMaterial;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.enums.Lang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class Item {

	public static boolean hasTag(ItemStack item, String key) {
		if (item == null || item.getAmount() <= 0 || item.getType().isAir()) {
			return false;
		}
		return Boolean.TRUE.equals(NBT.get(item, (Function<ReadableItemNBT, Boolean>) nbt -> nbt.getBoolean(key)));
	}

    public static ItemStack create(Material material, int amount, String name, List<String> lore, boolean enchant) {
		ItemCreator itemCreator = ItemCreator.of(material).amount(amount)
				.name(name).glow(enchant)
				.hideAttributes().flags(XItemFlag.values());
		if (lore != null) {
			itemCreator.lore(lore);
		}
        return itemCreator.make();
    }

	public static ItemStack create(Material mat, int amount, String name) {
		return create(mat, amount, name, null, false);
	}

	public static ItemStack create(Material mat, String name, List<String> lore, boolean enchant) {
		return create(mat, 1, name, lore, enchant);
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

		ItemStack item = original.clone();
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return item;

		List<String> lore = meta.getLore();
		if (lore == null) lore = new ArrayList<>();

		lore.addAll(loreToAdd);
		meta.setLore(lore);
		item.setItemMeta(meta);

		return item;
	}

	public static ItemStack createNoPlaceNoCraft(Material material, String name, List<String> lore) {
		return ItemCreator.of(material).name(name).lore(lore)
				.nbtBoolean("noPlace", true)
				.hideAll().hideAttributes()
				.make();
	}

	public static ItemStack createDestroyer(Material material, String name, List<String> lore) {
		return ItemCreator.of(material).name(name).lore(lore)
				.nbtBoolean("noBreak", true)
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
        return create(blank.getMaterial().get(), blank.getName());
    }

    public enum Blank {
		WHITE(XMaterial.WHITE_STAINED_GLASS_PANE, Lang.BLANK),
		GRAY(XMaterial.GRAY_STAINED_GLASS, Lang.BLANK),
		BLACK(XMaterial.BLACK_STAINED_GLASS, Lang.BLANK),
		BACK(XMaterial.ARROW, Lang.BACK),
		NEXT_PAGE(XMaterial.SPECTRAL_ARROW, Lang.NEXT),
		PREVIUS_PAGE(XMaterial.SPECTRAL_ARROW, Lang.PREVIOUS),
		EXIT(XMaterial.ARROW, Lang.EXIT);

        @Getter
        private final XMaterial material;
		private final Lang name;

		Blank(XMaterial material, Lang name) {
			this.material = material;
			this.name = name;
		}

		public String getName() {
			AdvancedVillages plugin = AdvancedVillages.getInstance();
			return plugin == null ? this.name.name() : plugin.getMessages().text(this.name);
		}
    }
}
