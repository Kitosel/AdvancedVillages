package pl.kiosel.villages.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.tablist.PlayerList;
import pl.kiosel.villages.data.user.*;

public class JoinListener implements Listener {

    private final AdvancedVillages plugin;
	private final UserManager userManager;

    public JoinListener(AdvancedVillages plugin) {
        this.plugin = plugin;
		this.userManager = plugin.getUserManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
		User user = this.userManager.findByPlayer(player)
				.peek(foundUser -> foundUser.getProfile().refresh())
				.orElseGet(() -> {
					UserProfile profile = new BukkitUserProfile(player.getUniqueId(), this.plugin.getMetaServer());
					return this.userManager.create(player.getUniqueId(), player.getName(), profile);
				});

		String playerName = player.getName();
		if (!user.getName().equals(playerName)) {
			this.userManager.updateUsername(user, playerName);
		}
		UserCache cache = user.getCache();
		plugin.getScoreboardManager().createBoard(player);

		if (this.plugin.getTablistConfig().isEnabled()) {
			PlayerList playerList = this.plugin.getIndividualPlayerList(user);
			playerList.send();
			cache.setPlayerList(playerList);
		}
    }
}
