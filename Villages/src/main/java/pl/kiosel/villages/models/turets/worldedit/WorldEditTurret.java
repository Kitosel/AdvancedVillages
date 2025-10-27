package pl.kiosel.villages.models.turets.worldedit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import pl.kiosel.common.utils.WorldEditUtils;
import pl.kiosel.villages.Wioski;

import java.io.File;

public class WorldEditTurret {

	private final Wioski plugin;

	public WorldEditTurret(Wioski plugin) {
		this.plugin = plugin;
	}

	public void pasteVillage(Location loc, int level) {
		File file = new File(plugin.getDataFolder(), "schematics/Turret" + level + ".schem");
		Bukkit.getScheduler().runTask(plugin, () -> WorldEditUtils.pasteSchematic(file, loc));
	}
}