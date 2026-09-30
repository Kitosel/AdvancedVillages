package pl.kiosel.villages.data.outpost.turets;

import org.bukkit.Material;
import org.bukkit.World;
import pl.kiosel.villages.data.village.turets.Turret;
import pl.kiosel.villages.data.village.turets.TurretStructure;

public final class TurretSetGreen extends Turret {

	private final TurretStructure BODY = TurretStructure.builder()
			.block(Material.LODESTONE, 0, 0, 0)

			.layer(Material.DARK_OAK_PLANKS, -1, 3)
			.fourPillars(Material.LIME_WOOL, 3, 0, 0)

			.mirrorXZ(Material.LIME_WOOL, 3, 0, 2)
			.mirrorXZ(Material.LIME_WOOL, 2, 0, 3)

			.outline(Material.LIME_WOOL, 1, 3)
			.outline(Material.LIME_WOOL, 2, 2)

			.cardinals(Material.LIME_WOOL, 2, 3)
			.cardinals(Material.LIME_WOOL, 3, 2)

			.layer(Material.LIME_WOOL, 3, 1)
			.fourPillars(Material.AIR, 3, 1, 1)
			.build();

	@Override
	public void setTurret(World world, int x, int y, int z) {
		paste(BODY, world, x, y, z);
	}
}
