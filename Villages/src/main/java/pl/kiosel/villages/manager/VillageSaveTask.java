package pl.kiosel.villages.manager;

import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.VillageMember;

public class VillageSaveTask implements Runnable {

    private final AdvancedVillages plugin;

    public VillageSaveTask(AdvancedVillages plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for(Village village : plugin.getVillageDataManager().getVillages().values()) {
            plugin.getVillageManager().saveVillageAsync(village);
        }
		for(VillageMember member : plugin.getVillageDataManager().getMembers().values()) {
			if (member == null) return;
			plugin.getDatabaseUserManager().saveUsersAsync(member);
		}
//		plugin.getDebug().debug("Saved Villages and Users");
    }
}