package pl.kiosel.villages.addons.tablist;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import panda.std.stream.PandaStream;
import pl.kiosel.core.thread.MetaTask;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.UserCache;
import pl.kiosel.villages.data.user.UserManager;
import pl.kiosel.villages.settings.Settings;

public class TablistBroadcastTask implements MetaTask {

    private final AdvancedVillages plugin;

    public TablistBroadcastTask(AdvancedVillages plugin) {
        this.plugin = plugin;
    }

	@Override
	public void execute() {
		UserManager userManager = this.plugin.getUserManager();

		if (!Settings.ADDONS_TABLIST_ENABLE.getBoolean()) {
			return;
		}

		for (Player player : Bukkit.getOnlinePlayers()) {
			User user = userManager.findByUuid(player.getUniqueId()).get();
			IndividualPlayerList playerList = user.getCache().getPlayerList().get();
			playerList.updatePageCycle();
			playerList.send();
		}
	}

	@Override
	public Type getType() {
		return Type.ASYNC;
	}
}
