package pl.kiosel.villages.config;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kiosel.core.chat.AdventureUtils;
import pl.kiosel.core.dependencies.net.kyori.adventure.title.Title;
import pl.kiosel.core.locale.Message;
import pl.kiosel.core.utils.TimeUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.enums.Lang;

import java.time.Duration;
import java.util.*;

public final class VillageMessages {

	private final AdvancedVillages plugin;

	public VillageMessages(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	public Message get(Lang key) {
		Objects.requireNonNull(key, "Message key cannot be null");
		return this.get(key.getPath());
	}

	public Message get(String path) {
		Message message = this.plugin.getLocale().getMessage(path);
		String text = message.toText();
		if (!text.contains("\\n")) {
			return message;
		}
		return this.plugin.getLocale().newMessage(text.replace("\\n", "\n"));
	}

	public Message format(Lang key, Object... placeholders) {
		return this.format(key.getPath(), placeholders);
	}

	public Message format(String path, Object... placeholders) {
		if (placeholders.length % 2 != 0) {
			throw new IllegalArgumentException("Placeholders must be provided as name/value pairs");
		}

		Message message = this.get(path);
		for (int index = 0; index < placeholders.length; index += 2) {
			Object replacement = placeholders[index + 1];
			message.processPlaceholder(
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
		String text = this.format(path, placeholders).toText();
		if (!path.equals(text)) {
			return text;
		}
		return this.plugin.getLocale().newMessage(defaultValue).toText();
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
		sendTitle(player, get(title).toString(), get(subtitle).toString(), times);
	}

	public void sendTitle(Player player, String title, String subtitle, Title.Times times) {
		Title titleComponent = AdventureUtils.createTitle(
				AdventureUtils.formatComponent(title),
				AdventureUtils.formatComponent(subtitle), times);
		AdventureUtils.sendTitle(titleComponent, player);
	}

	public String prefixedText(Lang key, Object... placeholders) {
		return AdventureUtils.toLegacy(this.format(key, placeholders).getPrefixedMessage());
	}

	public void send(CommandSender sender, Lang key, Object... placeholders) {
		this.format(key, placeholders).sendMessage(sender);
	}

	public void sendPrefixed(CommandSender sender, Lang key, Object... placeholders) {
		this.format(key, placeholders).sendPrefixedMessage(sender);
	}

	public boolean reload(String language, boolean force) {
		if (this.plugin.setLocale(language, force)) {
			List<String> missing = this.getMissingKeys();
			if (!missing.isEmpty()) {
				this.plugin.getLogger().warning("Language '" + language
						+ "' is missing message keys: " + String.join(", ", missing));
			}
			return true;
		}

		this.plugin.getLogger().warning("Could not load language '" + language
				+ "'; keeping '" + this.plugin.getLocale().getName() + "'");
		return false;
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
}
