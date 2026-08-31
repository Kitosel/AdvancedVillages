package pl.kiosel.villages.config;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.dependencies.adventure.adventure.title.Title;
import pl.kiosel.rosacore.utils.TimeUtils;
import pl.kiosel.villages.AdvancedVillages;

import java.time.Duration;
import java.util.*;

public final class VillageMessages {

	private final AdvancedVillages plugin;

	public VillageMessages(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	public VillageMessage get(Lang key) {
		Objects.requireNonNull(key, "Message key cannot be null");
		return this.get(key.getPath());
	}

	public VillageMessage get(String path) {
		return new VillageMessage(this.plugin, this.plugin.getLocale().message(path));
	}

	public VillageMessage raw(String text) {
		return new VillageMessage(this.plugin, this.plugin.getLocale().literal(text));
	}

	public VillageMessage format(Lang key, Object... placeholders) {
		return this.format(key.getPath(), placeholders);
	}

	public VillageMessage format(String path, Object... placeholders) {
		if (placeholders.length % 2 != 0) {
			throw new IllegalArgumentException("Placeholders must be provided as name/value pairs");
		}

		VillageMessage message = this.get(path);
		for (int index = 0; index < placeholders.length; index += 2) {
			Object replacement = placeholders[index + 1];
			message.with(
					String.valueOf(placeholders[index]),
					replacement == null ? "" : String.valueOf(replacement)
			);
		}
		return message;
	}

	public String text(Lang key, Object... placeholders) {
		return this.format(key, placeholders).toText();
	}

	public String textOrDefault(Lang key, String defaultValue, Object... placeholders) {
		return this.textOrDefault(key.getPath(), defaultValue, placeholders);
	}

	public String textOrDefault(String path, String defaultValue, Object... placeholders) {
		return new VillageMessage(this.plugin,
				this.plugin.getLocale().message(path, defaultValue).withPairs(placeholders)).toText();
	}

	public String formatDuration(Duration duration) {
		return TimeUtils.formatTime(duration, (division, amount) -> {
			String path = "general.time." + division.name().toLowerCase(Locale.ROOT)
					+ "." + division.getFormType(amount).getKey();
			String unit = this.textOrDefault(path, division.getForm(amount));
			return amount + " " + unit;
		});
	}

	public void sendTitle(Player player, Lang title, Lang subtitle, Title.Times times) {
		sendTitle(player, get(title).toText(), get(subtitle).toText(), times);
	}

	public void sendTitle(Player player, String title, String subtitle, Title.Times times) {
		this.plugin.getMessenger().title(player, title, subtitle,
				toTicks(times.fadeIn()), toTicks(times.stay()), toTicks(times.fadeOut()));
	}

	public String prefixedText(Lang key, Object... placeholders) {
		return this.format(key, placeholders).getPrefixedMessage();
	}

	public void send(CommandSender sender, Lang key, Object... placeholders) {
		this.format(key, placeholders).sendMessage(sender);
	}

	public void sendPrefixed(CommandSender sender, Lang key, Object... placeholders) {
		this.format(key, placeholders).sendPrefixed(sender);
	}

	public boolean reload(String language, boolean force) {
		try {
			this.plugin.setLocale(language, "en_US");
			List<String> missing = this.getMissingKeys();
			if (!missing.isEmpty()) {
				this.plugin.getRosaLogger().warning("Language '" + language
						+ "' is missing message keys: " + String.join(", ", missing));
			}
			return true;
		} catch (RuntimeException exception) {
			this.plugin.getRosaLogger().warning("Could not load language '" + language + "': " + exception.getMessage());
			return false;
		}
	}

	public List<String> getMissingKeys() {
		List<String> missing = new ArrayList<>();
		for (Lang key : Lang.values()) {
			if (key.getPath().equals(this.get(key).toText())) {
				missing.add(key.getPath());
			}
		}
		return Collections.unmodifiableList(missing);
	}

	private static int toTicks(Duration duration) {
		return Math.toIntExact(Math.max(0L, duration.toMillis() / 50L));
	}
}
