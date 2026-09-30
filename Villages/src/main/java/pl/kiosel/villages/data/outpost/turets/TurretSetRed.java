package pl.kiosel.villages.data.outpost.turets;

import org.bukkit.Material;
import org.bukkit.World;
import pl.kiosel.villages.data.village.turets.Turret;
import pl.kiosel.villages.data.village.turets.TurretStructure;

public final class TurretSetRed extends Turret {

	private final TurretStructure BODY = TurretStructure.builder()
			.block(Material.LODESTONE, 0, 0, 0)

			.layer(Material.DARK_OAK_PLANKS, -1, 3)
			.fourPillars(Material.DARK_OAK_FENCE, 3, 0, 0)
			.fourPillars(Material.RED_WOOL, 3, 1, 1)

			.mirrorXZ(Material.RED_WOOL, 3, 1, 2)
			.mirrorXZ(Material.RED_WOOL, 2, 1, 3)

			.outline(Material.RED_WOOL, 2, 3)
			.outline(Material.RED_WOOL, 3, 2)

			.cardinals(Material.RED_WOOL, 3, 3)
			.cardinals(Material.RED_WOOL, 4, 2)

			.layer(Material.RED_WOOL, 4, 1)
			.fourPillars(Material.AIR, 3, 2, 2)
			.build();

	@Override
	public void setTurret(World world, int x, int y, int z) {
		paste(BODY, world, x, y, z);
	}
}
