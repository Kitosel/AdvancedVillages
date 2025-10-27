package pl.kiosel.common.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

public class LocationUtils {

	public static String convertLocactionToString(Location location) {
		return location.getX() + ";" + location.getY() + ";" + location.getZ() + ";" + Objects.requireNonNull(location.getWorld()).getName() + ";" + location.getYaw() + ";" + location.getPitch() + ";";
	}

	public static Location getLocationFromString(String loc) {
		String[] array = loc.split(";");
		double blockX = Double.parseDouble(array[0]);
		double blockY = Double.parseDouble(array[1]);
		double blockZ = Double.parseDouble(array[2]);
		World world = Bukkit.getWorld(array[3]);
		float yaw = Float.parseFloat(array[4]);
		float pitch = Float.parseFloat(array[5]);
		return new Location(world, blockX, blockY, blockZ, yaw, pitch);
	}
}