package pl.kiosel.villages.data.village.turets.internal;

import org.bukkit.Material;
import org.bukkit.World;
import pl.kiosel.villages.data.village.turets.Turret;
import pl.kiosel.villages.data.village.turets.TurretStructure;

public class TurretSetPrismarine extends Turret {

	private static final TurretStructure STRUCTURE = TurretStructure.builder()
			.layer(Material.PRISMARINE, -1, 2)
			.block(Material.DARK_PRISMARINE, 0, -1, 0)
			.corners(Material.DARK_PRISMARINE, -1, 1)
			.corners(Material.DARK_PRISMARINE, -1, 2)

			.mirrorXZ(Material.PRISMARINE_WALL, 2, 0, 1)
			.mirrorXZ(Material.PRISMARINE_WALL, 1, 0, 2)
			.corners(Material.SEA_LANTERN, 0, 2)
			.corners(Material.DARK_PRISMARINE, 1, 2)
			.corners(Material.SMOOTH_QUARTZ_SLAB, 2, 2)

			.mirrorXZ(Material.QUARTZ_PILLAR, 2, 2, 1)
			.mirrorXZ(Material.QUARTZ_PILLAR, 1, 2, 2)
			.mirrorXZ(Material.PRISMARINE_BRICKS, 2, 3, 1)
			.mirrorXZ(Material.PRISMARINE_BRICKS, 1, 3, 2)
			.cardinals(Material.WHITE_STAINED_GLASS_PANE, 3, 2)

			.corners(Material.PRISMARINE, 4, 1)
			.mirrorXZ(Material.SMOOTH_QUARTZ_SLAB, 2, 4, 1)
			.mirrorXZ(Material.SMOOTH_QUARTZ_SLAB, 1, 4, 2)
			.corners(Material.SMOOTH_QUARTZ_SLAB, 5, 1)
			.block(Material.SEA_LANTERN, 0, 5, 0)
			.cardinals(Material.WHITE_STAINED_GLASS, 5, 1)
			.build();

	@Override
	public void setTurret(World world, int x, int y, int z) {
		paste(STRUCTURE, world, x, y, z);
	}
}
