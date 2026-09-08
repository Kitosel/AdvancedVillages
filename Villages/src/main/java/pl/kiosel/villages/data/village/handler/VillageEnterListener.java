package pl.kiosel.villages.data.village.handler;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;
import pl.kiosel.rosacore.dependencies.adventure.adventure.title.Title;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.api.events.PlayerEnterVillageEvent;
import pl.kiosel.villages.api.events.PlayerExitVillageEvent;
import pl.kiosel.villages.api.events.VillageListener;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.manager.VillageUtils;

import java.time.Duration;

public class VillageEnterListener extends VillageListener {

	private final AdvancedVillages plugin;
	private final Title.Times times;

	public VillageEnterListener(AdvancedVillages plugin) {
		super(plugin);
		this.plugin = plugin;
		this.times = Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(2), Duration.ofSeconds(1));
	}

    @EventHandler
    public void onEnter(PlayerEnterVillageEvent event) {
        Player player = event.getPlayer();
        Village village = event.getVillage();
		if (village==null) return;

		User user = getUser(player);

        if (!village.isMember(user) && village.canBeAttacked()) {
            push(player, village.getLocation().get());
            return;
        }
		String title = VillageUtils.replaceWith(village, plugin.getVillageMessages().get(Lang.ENTER_VILLAGE_AREA_TITLE).toString());
		String subtitle = VillageUtils.replaceWith(village, plugin.getVillageMessages().get(Lang.ENTER_VILLAGE_AREA_SUBTITLE).toString());
		plugin.getVillageMessages().sendTitle(player, title, subtitle, times);
    }

    @EventHandler
    public void onExit(PlayerExitVillageEvent event) {
		String title = plugin.getVillageMessages().get(Lang.LEAVE_VILLAGE_AREA_TITLE).toString();
		String subtitle = plugin.getVillageMessages().get(Lang.LEAVE_VILLAGE_AREA_SUBTITLE).toString();
		plugin.getVillageMessages().sendTitle(event.getPlayer(), title, subtitle, times);
    }

	public void push(Player player, Location blockLocation) {
		Location playerLocation = player.getLocation();

		Vector playerVector = playerLocation.toVector();
		Vector blockVector = blockLocation.toVector();

		Vector vector = playerVector.subtract(blockVector);
		vector.normalize();
		vector.multiply(1);

		player.setVelocity(vector);
	}
}