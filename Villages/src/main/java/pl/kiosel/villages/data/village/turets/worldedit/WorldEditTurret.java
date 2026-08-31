package pl.kiosel.villages.data.village.turets.worldedit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import pl.kiosel.villages.AdvancedVillages;

import java.io.File;
import java.io.IOException;

public class WorldEditTurret {

	private final AdvancedVillages plugin;

	public WorldEditTurret(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	public void pasteVillage(Location loc, int level) {
		File file = new File(plugin.getDataFolder(), "schematics/Turret" + level + ".schem");
		Bukkit.getScheduler().runTask(plugin, () -> {
			try {
				plugin.getHookManager().getWorldEdit().pasteSchematic(file, loc);
			} catch (IOException exception) {
				plugin.getRosaLogger().warning("Could not paste village schematic: " + exception.getMessage());
			}
		});
	}
}
