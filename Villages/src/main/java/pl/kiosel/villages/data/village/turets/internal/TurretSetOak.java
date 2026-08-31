package pl.kiosel.villages.data.village.turets.internal;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.turets.Turret;
import pl.kiosel.villages.data.village.turets.TurretStructure;

public class TurretSetOak extends Turret {

	private static final TurretStructure FENCES = TurretStructure.builder()
			.cardinals(Material.SPRUCE_FENCE, 3, 2)
			.mirrorXZ(Material.SPRUCE_FENCE, 2, 4, 1)
			.mirrorXZ(Material.SPRUCE_FENCE, 1, 4, 2)
			.build();

	private static final TurretStructure BODY = TurretStructure.builder()
			.layer(Material.SPRUCE_PLANKS, -1, 1)
			.outline(Material.SPRUCE_LOG, -1, 2)
			.corners(Material.SPRUCE_PLANKS, -1, 2)
			.fourPillars(Material.SPRUCE_LOG, 2, 0, 4)
			.mirrorXZ(Material.SPRUCE_WOOD, 2, 3, 1)
			.mirrorXZ(Material.SPRUCE_WOOD, 1, 3, 2)
			.corners(Material.SPRUCE_SLAB, 5, 2)
			.build();

	@Override
	public void setTurret(World world, int x, int y, int z) {
		paste(FENCES, world, x, y, z);
		Bukkit.getScheduler().runTaskLater(AdvancedVillages.getInstance(),
				() -> paste(BODY, world, x, y, z), 5L);
	}
}
