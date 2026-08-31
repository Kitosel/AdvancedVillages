package pl.kiosel.villages.data.village;

import lombok.Getter;
import org.bukkit.Material;
import pl.kiosel.rosacore.compatibility.ZMaterial;

public enum Upgrade {

    IRON(1, ZMaterial.IRON_INGOT),
    GOLD(2, ZMaterial.GOLD_INGOT),
    EMERALD(3, ZMaterial.EMERALD),
    DIAMOND(4, ZMaterial.DIAMOND),
	NETHERITE(5, ZMaterial.NETHERITE_INGOT),
	AMETHYST(6, ZMaterial.AMETHYST_SHARD),
	PRISMARINE(7, ZMaterial.PRISMARINE_CRYSTALS),
	ECHO(8, ZMaterial.END_CRYSTAL),
	STAR(9, ZMaterial.NETHER_STAR),
	DRAGON(10, ZMaterial.DRAGON_EGG),
	WORLDEDIT(73, ZMaterial.WOODEN_AXE),
	RESET(99, ZMaterial.BARRIER);

    @Getter private final int level;
    @Getter private final ZMaterial material;

    Upgrade(int level, ZMaterial material) {
        this.level = level;
        this.material = material;
    }

    public static Material getMaterialByLevel(int x) {
		return getByLevel(x).material.getMaterial().orElse(Material.STONE);
	}

	public static Upgrade getByLevel(int x) {
		for (Upgrade upgrade : values()) {
			if (upgrade.level == x)
				return upgrade;
		}
		return RESET;
	}
}
