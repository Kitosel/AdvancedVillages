package pl.kiosel.villages.data.outpost.turets;

import org.bukkit.Material;
import org.bukkit.World;
import pl.kiosel.villages.data.village.turets.Turret;
import pl.kiosel.villages.data.village.turets.TurretStructure;

public class TurretSetWhite extends Turret {

	// private final TurretStructure BODY5X5 = TurretStructure.builder()
	// 		.layer(Material.SPRUCE_PLANKS, -1, 2)
	// 		.corners(Material.SPRUCE_FENCE, 0, 2)
	// 		.corners(Material.WHITE_WOOL, 1, 2)
	// 		.mirrorXZ(Material.WHITE_WOOL, 2, 2, 1)
	// 		.mirrorXZ(Material.WHITE_WOOL, 1, 2, 2)
	// 		.mirrorXZ(Material.WHITE_WOOL, 1, 3, 1)
	// 		.mirrorXZ(Material.WHITE_WOOL, 2, 3, 0)
	// 		.mirrorXZ(Material.WHITE_WOOL, 0, 3, 2)
	// 		.mirrorXZ(Material.WHITE_WOOL, 1, 4, 0)
	// 		.mirrorXZ(Material.WHITE_WOOL, 0, 4, 1)
	// 		.block(Material.WHITE_WOOL, 0, 4, 0)
	// 		.build();

	private final TurretStructure BODY = TurretStructure.builder()
			.layer(Material.SPRUCE_PLANKS, -1, 3)
			.block(Material.LODESTONE, 0, 0, 0)
			.corners(Material.WHITE_WOOL, 0, 3)

			.cardinals(Material.WHITE_WOOL, 1, 3, 2)

			.mirrorXZ(Material.WHITE_WOOL, 2, 2, 2)

			.cardinals(Material.WHITE_WOOL, 2, 1, 3)
			.cardinals(Material.WHITE_WOOL, 3, 1, 2)
			.cardinals(Material.WHITE_WOOL, 3, 3)
			.cardinals(Material.WHITE_WOOL, 4, 2)

			.fill(Material.WHITE_WOOL, 1, 4, 1, -1, 4, -1)
			// .corners(Material.WHITE_WOOL, 4, 1)
			// .mirrorXZ(Material.WHITE_WOOL, 1, 5, 0)
			// .mirrorXZ(Material.WHITE_WOOL, 0, 5, 1)
			// .block(Material.WHITE_WOOL, 0, 5, 0)
			.build();

	@Override
	public void setTurret(World world, int x, int y, int z) {
		paste(BODY, world, x, y, z);
	}
}
