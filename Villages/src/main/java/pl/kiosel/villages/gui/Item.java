package pl.kiosel.villages.gui;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import pl.kiosel.core.dependencies.de.tr7zw.nbtapi.NBT;
import pl.kiosel.core.dependencies.de.tr7zw.nbtapi.iface.ReadWriteItemNBT;
import pl.kiosel.core.dependencies.de.tr7zw.nbtapi.iface.ReadableItemNBT;
import pl.kiosel.core.utils.ColorUtils;
import pl.kiosel.villages.config.GuiConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static pl.kiosel.core.utils.ColorUtils.tl;

public class Item {

	public static boolean hasTag(ItemStack item, String key) {
		if (item == null || item.getAmount() <= 0 || item.getType().isAir()) {
			return false;
		}
		return Boolean.TRUE.equals(NBT.get(item, (Function<ReadableItemNBT, Boolean>) nbt -> nbt.getBoolean(key)));
	}

    public static ItemStack create(Material material, int x, String name, List<String> lore, boolean enchant) {
        ItemStack item = new ItemStack(material, x);
        ItemMeta meta = item.getItemMeta();

        if(meta != null) {
			meta.setDisplayName(tl(name));
			meta.addItemFlags(ItemFlag.values());
            if (enchant) {
                meta.addEnchant(Enchantment.MENDING, 1, false);
            }
            if (lore != null) {
                meta.setLore(ColorUtils.listColor(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

	public static ItemStack create(Material mat, int x, String name) {
		return create(mat, x, name, null, false);
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

	public static ItemStack create(Material mat, int x, String name, List<String> lore) {
		return create(mat, x, name, lore, false);
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
		ItemStack item = new ItemStack(material, 1);
		NBT.modify(item, (Consumer<ReadWriteItemNBT>) nbt -> nbt.setBoolean("noPlace", true));
		ItemMeta meta = item.getItemMeta();

		if(meta != null) {
			meta.setDisplayName(tl(name));
			meta.addItemFlags(ItemFlag.values());
			meta.setUnbreakable(true);
			meta.addEnchant(Enchantment.MENDING, 1, false);
			if (lore != null) {
				meta.setLore(ColorUtils.listColor(lore));
			}
			item.setItemMeta(meta);
		}
		return item;
	}

	public static ItemStack createDestroyer(Material material, String name, List<String> lore) {
		ItemStack item = new ItemStack(material, 1);
		NBT.modify(item, (Consumer<ReadWriteItemNBT>) nbt -> nbt.setBoolean("noBreak", true));
		ItemMeta meta = item.getItemMeta();

		if(meta != null) {
			meta.setDisplayName(tl(name));
			meta.addItemFlags(ItemFlag.values());
			meta.setUnbreakable(true);
			meta.addEnchant(Enchantment.MENDING, 1, false);
			if (lore != null) {
				meta.setLore(ColorUtils.listColor(lore));
			}
			item.setItemMeta(meta);
		}
		return item;
	}

	public static ItemStack createHead(OfflinePlayer player, String name, List<String> lore) {
		ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
		NBT.modify(item, (Consumer<ReadWriteItemNBT>) nbt -> nbt.setString("player", player.getName()));
		NBT.modify(item, (Consumer<ReadWriteItemNBT>) nbt -> nbt.setUUID("uuid", player.getUniqueId()));
		SkullMeta meta = (SkullMeta) item.getItemMeta();

		if(meta != null) {
			meta.setDisplayName(tl(name));
			meta.addItemFlags(ItemFlag.values());
			meta.setOwningPlayer(player);
			if (lore != null) {
				meta.setLore(ColorUtils.listColor(lore));
			}
			item.setItemMeta(meta);
		}
		return item;
	}

    public static ItemStack blank(Blank blank) {
        return create(blank.getMaterial(), blank.getName());
    }

    public enum Blank {
        WHITE(Material.WHITE_STAINED_GLASS_PANE, GuiConfig.guis_blank),
        GRAY(Material.GRAY_STAINED_GLASS, GuiConfig.guis_blank),
        BLACK(Material.BLACK_STAINED_GLASS, GuiConfig.guis_blank),
		BACK(Material.ARROW, GuiConfig.guis_exit),
		NEXT_PAGE(Material.SPECTRAL_ARROW, GuiConfig.guis_next),
		PREVIUS_PAGE(Material.SPECTRAL_ARROW, GuiConfig.guis_previous),
        EXIT(Material.ARROW, GuiConfig.guis_exit);

        @Getter
        private final Material material;
        @Getter
        private final String name;

        Blank(Material material, String name) {
            this.material = material;
            this.name = name;
        }
    }
}
