package pl.kiosel.common;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;

public class Packet {

    public String packageVersion = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];

    public static String getServerVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    public static int getRevision() { return Integer.parseInt(getServerVersion().substring(getServerVersion().length() - 1)); }
    public static int getMajorVersion() { return Integer.parseInt(getServerVersion().split("_")[1]); }

    public Class<?> getNMSClass(String package17, String name) {
        Class<?> c = null;
        try {
            if(getMajorVersion() > 16) {
                c = Class.forName("net.minecraft." + package17 + "." + name);
            } else {
                c = Class.forName("net.minecraft.server." + packageVersion + "." + name);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return c;
    }

    public Class<?> getCraftBukkitClass(String name) {
        Class<?> c = null;
        try {
            c = Class.forName("org.bukkit.craftbukkit." + packageVersion + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return c;
    }

    public Object asNMSCopy(ItemStack itemStack) {
        try {
            return getCraftBukkitClass("inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class).invoke(null, itemStack);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public ItemStack asBukkitCopy(Object item) {
        try {
            return (ItemStack) getCraftBukkitClass("inventory.CraftItemStack").getMethod("asBukkitCopy", item.getClass()).invoke(null, item);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}