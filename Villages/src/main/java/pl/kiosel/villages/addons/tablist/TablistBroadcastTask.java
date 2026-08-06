package pl.kiosel.villages.addons.tablist;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import panda.std.Option;
import pl.kiosel.core.thread.MetaTask;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.UserManager;

public class TablistBroadcastTask implements MetaTask {

    private final AdvancedVillages plugin;

    public TablistBroadcastTask(AdvancedVillages plugin) {
        this.plugin = plugin;
    }

	@Override
	public void execute() {
		UserManager userManager = this.plugin.getUserManager();

		if (!this.plugin.getTablistConfig().isEnabled()) {
			return;
		}

		for (Player player : Bukkit.getOnlinePlayers()) {
			Option<User> userOption = userManager.findByUuid(player.getUniqueId());
			if (userOption.isEmpty()) {
				continue;
			}

			User user = userOption.get();
			Option<PlayerList> playerListOption = user.getCache().getPlayerList();
			if (playerListOption.isEmpty()) {
				continue;
			}

			PlayerList playerList = playerListOption.get();
			playerList.updatePageCycle();
			playerList.send();
		}
	}

	@Override
	public Type getType() {
		return Type.SYNC;
	}
}
