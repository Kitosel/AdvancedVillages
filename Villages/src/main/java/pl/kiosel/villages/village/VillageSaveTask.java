package pl.kiosel.villages.village;

import pl.kiosel.villages.Wioski;

public class VillageSaveTask implements Runnable {

    private final Wioski plugin;

    public VillageSaveTask(Wioski plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for(Village village : plugin.getPlayerDataManager().getVillages().values()) {
            plugin.getVillageManager().saveVillageAsync(village);
        }
		for(VillageMember member : plugin.getPlayerDataManager().getMembers().values()) {
			if (member == null) return;
			plugin.getUserManager().saveUsersAsync(member);
		}
//		plugin.getDebug().debug("Saved Villages and Users");
    }
}