package pl.kiosel.villages.models;

import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.kiosel.villages.Wioski;
import pl.kiosel.villages.config.Config;
import pl.kiosel.villages.config.Language;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.village.Village;
import pl.kiosel.villages.village.VillageManager;

import java.util.*;

import static pl.kiosel.common.utils.ColorUtils.tl;

public class TeleportManager {

	@Getter	private final Map<Player, BukkitRunnable> teleportCooldowns = new HashMap<>();
	@Getter	private final Map<Player, BukkitRunnable> teleportTasks = new HashMap<>();
	@Getter	private final Set<Player> teleportingPlayers = new HashSet<>();
	@Getter	private final Set<Player> setTeleport = new HashSet<>();
	@Getter	private final HashMap<UUID, Long> cooldown = new HashMap<>();

	private final Wioski plugin;

	public TeleportManager(Wioski plugin) {
		this.plugin = plugin;
	}

	public boolean isTeleportTask(Player player) {
		return setTeleport.contains(player);
	}

	public void setTeleportTask(Player player) {
		if (isTeleportTask(player)) return;
		setTeleport.add(player);
	}

	public void removeTeleportTask(Player player) {
		if (!isTeleportTask(player)) return;
		setTeleport.remove(player);
	}

	public void sendHoverSet(Player player) {
		if (!isTeleportTask(player)) return;
		Language lang = plugin.getLang();
		TextComponent teleport_message = new TextComponent(lang.getMessage(Lang.TELEPORT_SET));

		teleport_message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(lang.getMessage(Lang.TELEPORT_SET_HOVER)).create()));
		teleport_message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/village teleporting6 village7 set9"));

		player.sendMessage(tl("&8——————————————————————————"));
		player.spigot().sendMessage(teleport_message);
		player.sendMessage(tl("&8——————————————————————————"));
	}

	public void setTeleportToVillage(Location location, Village village) {
		village.setTeleport(location);
	}

	public boolean teleportPlayerToVillage(Player player) {
		Language lang = plugin.getLang();
		String teleportTitle = lang.getMessage(Lang.TELEPORT_TITLE);
		String teleportSubtitle = lang.getMessage(Lang.TELEPORT_SUBTITLE);
		String cooldownMessage = lang.getMessage(Lang.TELEPORT_COOLDOWN);

		long cooldown_time = Config.teleport_between_cooldown;
		long cooldownTime = Config.teleport_cooldown * 1000L;

		if (cooldown.containsKey(player.getUniqueId())) {
			long timeElapsed = System.currentTimeMillis() - cooldown.get(player.getUniqueId());
			if (timeElapsed < cooldown_time * 1000L) {
				long secondsLeft = ((cooldown_time * 1000L) - timeElapsed) / 1000;
				player.sendMessage(cooldownMessage.replace("%time%", String.valueOf(secondsLeft)));
				return false;
			}
			cooldown.remove(player.getUniqueId());
		}

		Village village = VillageManager.getVillageByOfflineOwner(player.getName());
		if (village == null || village.getTeleport() == null) {
			player.sendMessage(lang.getMessage(Lang.VILLAGE_NO));
			return false;
		}

		cooldown.put(player.getUniqueId(), System.currentTimeMillis());
		teleportingPlayers.add(player);

		int totalSeconds = (int) (cooldownTime / 1000);

		for (int i = totalSeconds; i > 0; i--) {
			int secondsLeft = i;
			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				if (teleportingPlayers.contains(player)) {
					player.sendTitle(
							teleportTitle.replace("%seconds%", String.valueOf(secondsLeft)),
							teleportSubtitle.replace("%seconds%", String.valueOf(secondsLeft)),
							10, 20, 10
					);
				}
			}, (totalSeconds - i) * 20L);
		}

		BukkitRunnable teleportTask = new BukkitRunnable() {
			@Override
			public void run() {
				teleportingPlayers.remove(player);

				Village v = VillageManager.getVillageByOfflineOwner(player.getName());
				if (v == null || v.getTeleport() == null) {
					player.sendMessage(lang.getMessage(Lang.VILLAGE_NO));
					return;
				}

				player.teleport(v.getTeleport());
				player.sendMessage(lang.getMessage(Lang.TELEPORTED).replace("%VILLAGE%", v.getVillageName()));
			}
		};
		teleportTask.runTaskLater(plugin, cooldownTime / 50);
		teleportTasks.put(player, teleportTask);
		return true;
	}
}