package pl.kiosel.villages.config;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.message.RosaMessage;
import pl.kiosel.villages.AdvancedVillages;

import java.util.Objects;

public final class VillageMessage {

	private final AdvancedVillages plugin;
	private RosaMessage message;

	VillageMessage(AdvancedVillages plugin, RosaMessage message) {
		this.plugin = Objects.requireNonNull(plugin, "plugin");
		this.message = Objects.requireNonNull(message, "message");
	}

	public VillageMessage with(String name, Object value) {
		this.message = this.message.with(name, value == null ? "" : value);
		return this;
	}

	public String toText() {
		return this.message.legacy();
	}

	public String getPrefixedMessage() {
		return this.message.prefixedLegacy();
	}

	public void sendMessage(CommandSender sender) {
		this.message.send(sender);
	}

	public void sendPrefixed(CommandSender sender) {
		this.message.sendPrefixed(sender);
	}

	public void sendActionBar(Player player) {
		this.plugin.getMessenger().actionBar(player, toText());
	}

	public void sendTitle(Player player) {
		this.plugin.getMessenger().title(player, toText(), "");
	}

	@Override
	public String toString() {
		return toText();
	}
}
