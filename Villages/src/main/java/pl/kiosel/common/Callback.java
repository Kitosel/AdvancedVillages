package pl.kiosel.common;

import org.bukkit.scheduler.BukkitRunnable;
import pl.kiosel.villages.Wioski;

public abstract class Callback<T> {
    private final Wioski plugin;

    public Callback(Wioski plugin) {
        this.plugin = plugin;
    }

    public void onResult(T result) {}

    public void onError(Throwable throwable) {}

    public final void callSyncResult(final T result) {
        new BukkitRunnable() {
            @Override
            public void run() {
                onResult(result);
            }
        }.runTask(plugin);
    }

    public final void callSyncError(final Throwable throwable) {
        new BukkitRunnable() {
            @Override
            public void run() {
                onError(throwable);
            }
        }.runTask(plugin);
    }
}