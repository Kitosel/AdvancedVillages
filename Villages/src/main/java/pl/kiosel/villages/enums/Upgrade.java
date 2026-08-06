package pl.kiosel.villages.enums;

import lombok.Getter;
import org.bukkit.Material;

public enum Upgrade {

    IRON(1, Material.IRON_INGOT),
    GOLD(2, Material.GOLD_INGOT),
    EMERALD(3, Material.EMERALD),
    DIAMOND(4, Material.DIAMOND),
	NETHERITE(5, Material.NETHERITE_INGOT),
	AMETHYST(6, Material.AMETHYST_SHARD),
	PRISMARINE(7, Material.PRISMARINE_CRYSTALS),
	ECHO(8, Material.ECHO_SHARD),
	STAR(9, Material.NETHER_STAR),
	DRAGON(10, Material.DRAGON_EGG),
	WORLDEDIT(73, Material.WOODEN_AXE),
	RESET(99, Material.BARRIER);

    @Getter private final int level;
    @Getter private final Material material;

    Upgrade(int level, Material material) {
        this.level = level;
        this.material = material;
    }

    public static Material getMaterialByLevel(int x) {
		return getByLevel(x).material;
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
