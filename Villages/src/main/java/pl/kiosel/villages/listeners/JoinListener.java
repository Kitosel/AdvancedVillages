package pl.kiosel.villages.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import pl.kiosel.rosacore.listener.RosaListener;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.UserManager;

public class JoinListener extends RosaListener {

    private final AdvancedVillages plugin;
	private final UserManager userManager;

    public JoinListener(AdvancedVillages plugin) {
		super(plugin);
        this.plugin = plugin;
		this.userManager = plugin.getUserManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		User user = this.userManager.getOrCreate(player);
		this.plugin.getScoreboardManager().createBoard(player);
		this.plugin.getTablistManager().handleJoin(player, user);
	}
}
