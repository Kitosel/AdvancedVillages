package pl.kiosel.villages.manager;

import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.kiosel.core.locale.Locale;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.settings.Settings;
import pl.kiosel.villages.data.village.Village;

import java.util.*;

import static pl.kiosel.core.utils.ColorUtils.tl;

public class TeleportManager {

	@Getter	private final Map<Player, BukkitRunnable> teleportCooldowns = new HashMap<>();
	@Getter	private final Map<Player, BukkitRunnable> teleportTasks = new HashMap<>();
	@Getter	private final Set<Player> teleportingPlayers = new HashSet<>();
	@Getter	private final Set<Player> setTeleport = new HashSet<>();
	@Getter	private final HashMap<UUID, Long> cooldown = new HashMap<>();

	private final AdvancedVillages plugin;

	public TeleportManager(AdvancedVillages plugin) {
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
		Locale locale = plugin.getLocale();
		TextComponent teleport_message = new TextComponent(locale.getMessage(Lang.TELEPORT_SET.getPath()).toText());

		teleport_message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(locale.getMessage(Lang.TELEPORT_SET_HOVER.getPath()).toText()).create()));
		teleport_message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/village teleporting6 village7 set9"));

		player.sendMessage(tl("&8——————————————————————————"));
		player.spigot().sendMessage(teleport_message);
		player.sendMessage(tl("&8——————————————————————————"));
	}

	public void setTeleportToVillage(Location location, Village village) {
		village.setTeleport(location);
	}

	public boolean teleportPlayerToVillage(Player player) {
		Locale locale = plugin.getLocale();

		long cooldown_time = Settings.TELEPORT_BETWEEN_COOLDOWN.getLong();
		long cooldownTime = Settings.TELEPORT_COOLDOWN.getLong() * 1000L;

		if (cooldown.containsKey(player.getUniqueId())) {
			long timeElapsed = System.currentTimeMillis() - cooldown.get(player.getUniqueId());
			if (timeElapsed < cooldown_time * 1000L) {
				long secondsLeft = ((cooldown_time * 1000L) - timeElapsed) / 1000;
				player.sendMessage(locale.getMessage(Lang.TELEPORT_COOLDOWN.getPath()).processPlaceholder("time", secondsLeft).toString());
				return false;
			}
			cooldown.remove(player.getUniqueId());
		}

		Village village = VillageManager.getVillageByOfflineOwner(player.getName());
		if (village == null || village.getTeleport() == null) {
			locale.getMessage(Lang.VILLAGE_NO.getPath()).sendPrefixedMessage(player);
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
							locale.getMessage(Lang.TELEPORT_TITLE.getPath()).processPlaceholder("time", secondsLeft).toString(),
							locale.getMessage(Lang.TELEPORT_SUBTITLE.getPath()).processPlaceholder("time", secondsLeft).toString(),
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
					locale.getMessage(Lang.VILLAGE_NO.getPath()).sendPrefixedMessage(player);
					return;
				}
				player.teleport(v.getTeleport());
				locale.getMessage(Lang.TELEPORTED.getPath()).processPlaceholder("village", v.getVillageName()).sendPrefixedMessage(player);
			}
		};
		teleportTask.runTaskLater(plugin, cooldownTime / 50);
		teleportTasks.put(player, teleportTask);
		return true;
	}
}