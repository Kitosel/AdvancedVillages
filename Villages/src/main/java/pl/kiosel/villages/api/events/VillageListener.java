package pl.kiosel.villages.api.events;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import pl.kiosel.rosacore.listener.RosaListener;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.config.VillageMessage;
import pl.kiosel.villages.data.user.User;

public abstract class VillageListener extends RosaListener {

	private final AdvancedVillages plugin;

	public VillageListener(AdvancedVillages plugin) {
		super(plugin);
		this.plugin = plugin;
	}

	public User getUser(Player player) {
		return player == null ? null : this.plugin.getUserManager().findByPlayer(player).orElse(null);
	}

	public static Player getAttacker(Entity damager) {
		if (damager instanceof Player) return (Player) damager;
		if (damager instanceof Projectile) {
			Object shooter = ((Projectile) damager).getShooter();
			if (shooter instanceof Player) return (Player) shooter;
		}
		return null;
	}

	public VillageMessage getMessage(String node) {
		return plugin.getVillageMessages().get(node);
	}

	public void sendLocalized(CommandSender sender, Lang node) {
		plugin.getVillageMessages().sendPrefixed(sender, node);
	}
}
