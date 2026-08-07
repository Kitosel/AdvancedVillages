package pl.kiosel.villages.addons.antylogout;

import org.bukkit.entity.Player;
import pl.kiosel.core.chat.AdventureUtils;
import pl.kiosel.core.dependencies.net.kyori.adventure.text.Component;
import pl.kiosel.core.dependencies.net.kyori.adventure.title.Title;
import pl.kiosel.core.utils.ColorUtils;
import pl.kiosel.villages.AdvancedVillages;

import java.util.Collections;
import java.util.Map;

public class CombatMessage {

	private final MessageType type;
	private final String content;

	public CombatMessage(String content) {
		this.type = MessageType.CHAT;
		this.content = content;
	}

	public CombatMessage(MessageType type, String content) {
		this.type = type;
		this.content = content;
	}

	public CombatMessage(Map<String, Object> map) {
		this(MessageType.valueOf((String)map.get("type")), (String)map.get("content"));
	}

	private String[] processMessageContent(Player player, String content, Map<String, Object> replacements) {
		if (replacements != null && !replacements.isEmpty() && AdvancedVillages.getInstance().isPlaceholder()) {
			content = AdvancedVillages.getInstance().getPlaceholder().replacePlaceholder(player, content, replacements);
		}
		return content.split("\n");
	}

	public void send(Player player, Map<String, Object> replacements) {
		if (this.type == null || this.content == null) {
			new CombatMessage("&4l&lANTYLOGOUT &8&l| &cAn error occurred while sending the message. Please check the console for details.").send(player);
			return;
		}
		String[] messageContent = this.processMessageContent(player, ColorUtils.color(this.content), replacements);
		switch (this.type) {
			case CHAT: {
				player.sendMessage(messageContent);
				break;
			}
			case ACTION_BAR: {
				AdventureUtils.sendActionBar(AdventureUtils.formatComponent(messageContent[0]), player);
				break;
			}
			case TITLE: {
				Component title = AdventureUtils.formatComponent(messageContent.length > 0 ? messageContent[0].trim() : "");
				Component subtitle = AdventureUtils.formatComponent(messageContent.length > 1 ? messageContent[1].trim() : "");
				Title titleComponent = AdventureUtils.createTitle(title, subtitle,10, 70, 20);
				AdventureUtils.sendTitle(titleComponent, player);
				break;
			}
			case NONE: {
				break;
			}
			default: {
				throw new UnsupportedOperationException("Unsupported message type: " + this.type);
			}
		}
	}

	public void send(Player player, String placeholderKey, Object placeholderValue) {
		this.send(player, Collections.singletonMap(placeholderKey, placeholderValue));
	}

	public void send(Player player) {
		this.send(player, null);
	}

	public enum MessageType {
		CHAT,
		ACTION_BAR,
		TITLE,
		NONE

	}
}
