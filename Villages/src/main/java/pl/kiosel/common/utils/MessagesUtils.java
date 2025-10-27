package pl.kiosel.common.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kiosel.villages.Wioski;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class MessagesUtils {

	//Bukkit defaults for time
	public static Title createTitle(Component title, Component subtitle) {
		return Title.title(title, subtitle, Title.Times.times(
				Duration.of(10 * 50L, ChronoUnit.MILLIS),
				Duration.of(70 * 50L, ChronoUnit.MILLIS),
				Duration.of(20 * 50L, ChronoUnit.MILLIS)
		));
	}
	// times in ticks
	public static Title createTitle(Component title, Component subtitle, int fadeIn, int stay, int fadeOut) {
		return Title.title(title, subtitle, Title.Times.times(
				Duration.of(fadeIn * 50L, ChronoUnit.MILLIS),
				Duration.of(stay * 50L, ChronoUnit.MILLIS),
				Duration.of(fadeOut * 50L, ChronoUnit.MILLIS)
		));
	}

	public static Title createTitle(Component title, Component subtitle, Title.Times times) {
		return Title.title(title, subtitle, times);
	}

	public static void sendTitle(Title title, Player player) {
		Wioski.getInstance().getBukkitAudiences().player(player).showTitle(title);
	}

	public static void sendActionBar(Component message, Player player) {
		Wioski.getInstance().getBukkitAudiences().player(player).sendActionBar(message);
	}

	public static void sendMessage(Component message, CommandSender... target) {
		for (CommandSender sender : target) {
			Wioski.getInstance().getBukkitAudiences().sender(sender).sendMessage(message);
		}
	}
}