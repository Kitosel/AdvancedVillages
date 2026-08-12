package pl.kiosel.villages.enums;

import lombok.Getter;
import org.bukkit.Material;
import pl.kiosel.dependencies.com.cryptomorin.xseries.XMaterial;

public enum Upgrade {

    IRON(1, XMaterial.IRON_INGOT),
    GOLD(2, XMaterial.GOLD_INGOT),
    EMERALD(3, XMaterial.EMERALD),
    DIAMOND(4, XMaterial.DIAMOND),
	NETHERITE(5, XMaterial.NETHERITE_INGOT),
	AMETHYST(6, XMaterial.AMETHYST_SHARD),
	PRISMARINE(7, XMaterial.PRISMARINE_CRYSTALS),
	ECHO(8, XMaterial.END_CRYSTAL),
	STAR(9, XMaterial.NETHER_STAR),
	DRAGON(10, XMaterial.DRAGON_EGG),
	WORLDEDIT(73, XMaterial.WOODEN_AXE),
	RESET(99, XMaterial.BARRIER);

    @Getter private final int level;
    @Getter private final XMaterial material;

    Upgrade(int level, XMaterial material) {
        this.level = level;
        this.material = material;
    }

    public static Material getMaterialByLevel(int x) {
		return getByLevel(x).material.get();
	}

	public static Upgrade getByLevel(int x) {
		for (Upgrade upgrade : values()) {
			if (upgrade.level == x) {
				return upgrade;
			}
		}
		return RESET;
	}
}
