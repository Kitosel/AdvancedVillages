package pl.kiosel.villages.listeners.village;

import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;
import pl.kiosel.common.utils.AdventureUtils;
import pl.kiosel.common.utils.MessagesUtils;
import pl.kiosel.villages.Wioski;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.api.events.PlayerEnterVillageEvent;
import pl.kiosel.villages.api.events.PlayerExitVillageEvent;
import pl.kiosel.villages.village.Village;
import pl.kiosel.villages.village.VillageManager;

import java.time.Duration;

public class VillageListener implements Listener {

	private final Wioski plugin;
	private final Title.Times times;

	public VillageListener(Wioski plugin) {
		this.plugin = plugin;
		this.times = Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(3), Duration.ofSeconds(1));
	}

    @EventHandler
    public void onEnter(PlayerEnterVillageEvent event) {
        Player player = event.getPlayer();
        Village village = event.getVillage();
		if (village==null) return;

        if (!village.isMember(player) && village.getVillageSettings().isProtection()) {
            push(player, village.getLocation());
            return;
        }
		String title = VillageManager.replaceWith(village, plugin.getLang().getMessage(Lang.ENTER_VILLAGE_AREA_TITLE));
		String subtitle = VillageManager.replaceWith(village, plugin.getLang().getMessage(Lang.ENTER_VILLAGE_AREA_SUBTITLE));
		Title titleComponent = MessagesUtils.createTitle(AdventureUtils.formatComponent(title), AdventureUtils.formatComponent(subtitle), times);
		MessagesUtils.sendTitle(titleComponent, event.getPlayer());
    }

    @EventHandler
    public void onExit(PlayerExitVillageEvent event) {
		String title = plugin.getLang().getMessage(Lang.LEAVE_VILLAGE_AREA_TITLE);
		String subtitle = plugin.getLang().getMessage(Lang.LEAVE_VILLAGE_AREA_SUBTITLE);
		Title titleComponent = MessagesUtils.createTitle(AdventureUtils.formatComponent(title), AdventureUtils.formatComponent(subtitle), times);
		MessagesUtils.sendTitle(titleComponent, event.getPlayer());
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