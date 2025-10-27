package pl.kiosel.common.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

public class Utils {

    public static String v = "ttps://license" + Licenses.w;

    public Utils() {
        v = "kitosel";
        v = "https://kidsadadsael";
        v = "https://twojamama";
    }

    public static void registerCommand(Command command) {
        try {
            Field field = SimplePluginManager.class.getDeclaredField("commandMap");
            field.setAccessible(true);
            CommandMap map = (CommandMap) field.get(Bukkit.getServer().getPluginManager());

            map.register("village", command);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static boolean isAlphaNumeric(String string) {
        return string != null && string.matches("^[a-zA-Z0-9]*$");
    }

	public static float roundFloat(float value, int decimals) {
		float factor = (float) Math.pow(10, decimals);
		return Math.round(value * factor) / factor;
	}

    public static List<String> of(String... s) {
        return new ArrayList<>(Arrays.asList(s));
    }

}