package pl.kiosel.villages.data.village.handler;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;
import pl.kiosel.core.chat.AdventureUtils;
import pl.kiosel.core.dependencies.net.kyori.adventure.title.Title;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.api.events.PlayerEnterVillageEvent;
import pl.kiosel.villages.api.events.PlayerExitVillageEvent;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.manager.VillageManager;

import java.time.Duration;

public class VillageListener implements Listener {

	private final AdvancedVillages plugin;
	private final Title.Times times;

	public VillageListener(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.times = Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(2), Duration.ofSeconds(1));
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
		String title = VillageManager.replaceWith(village, plugin.getLocale().getMessage(Lang.ENTER_VILLAGE_AREA_TITLE.getPath()).toString());
		String subtitle = VillageManager.replaceWith(village, plugin.getLocale().getMessage(Lang.ENTER_VILLAGE_AREA_SUBTITLE.getPath()).toString());
		Title titleComponent = AdventureUtils.createTitle(AdventureUtils.formatComponent(title), AdventureUtils.formatComponent(subtitle), times);
		AdventureUtils.sendTitle(titleComponent, event.getPlayer());
    }

    @EventHandler
    public void onExit(PlayerExitVillageEvent event) {
		String title = plugin.getLocale().getMessage(Lang.LEAVE_VILLAGE_AREA_TITLE.getPath()).toString();
		String subtitle = plugin.getLocale().getMessage(Lang.LEAVE_VILLAGE_AREA_SUBTITLE.getPath()).toString();
		Title titleComponent = AdventureUtils.createTitle(AdventureUtils.formatComponent(title), AdventureUtils.formatComponent(subtitle), times);
		AdventureUtils.sendTitle(titleComponent, event.getPlayer());
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