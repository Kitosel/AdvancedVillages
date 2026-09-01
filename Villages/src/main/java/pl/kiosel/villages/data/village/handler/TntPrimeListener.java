package pl.kiosel.villages.data.village.handler;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.TNTPrimeEvent;
import pl.kiosel.rosacore.listener.RosaListener;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;

public final class TntPrimeListener extends RosaListener {

	private final AdvancedVillages plugin;

	public TntPrimeListener(AdvancedVillages plugin) {
		super(plugin);
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPrime(TNTPrimeEvent event) {
		Location location = event.getBlock().getLocation();
		if (location.getWorld() == null || plugin.getVillageUtils().isBlacklisted(location.getWorld())) return;

		Village village = plugin.getVillageUtils().getVillageAt(location);
		if (village == null || village.isTnt()) return;

		Entity entity = event.getPrimingEntity();
		if (!(entity instanceof Player)) return;

		Player player = (Player) entity;
		User user = plugin.getUserManager().findByUuid(player.getUniqueId()).orElse(null);
		if (user == null || !village.isMember(user)) return;

		event.setCancelled(true);
		player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 0.5f, 1.0f);
	}
}
