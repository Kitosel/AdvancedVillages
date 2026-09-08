package pl.kiosel.villages.data.village.handler;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.TNTPrimeEvent;
import pl.kiosel.rosacore.compatibility.ZSound;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.api.events.VillageListener;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;

public final class TntPrimeListener extends VillageListener {

	private final AdvancedVillages plugin;

	public TntPrimeListener(AdvancedVillages plugin) {
		super(plugin);
		this.plugin = plugin;
	}

	@Override
	public boolean isAvailable() {
		try {
			Class.forName("org.bukkit.event.block.TNTPrimeEvent", false, getClass().getClassLoader());
			return true;
		} catch (ClassNotFoundException | LinkageError ignored) {
			return false;
		}
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
		User user = getUser(player);
		if (user == null || !village.isMember(user)) return;

		event.setCancelled(true);
		ZSound.ENTITY_VILLAGER_NO.play(player, 0.5f, 1f);
	}
}
