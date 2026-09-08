package pl.kiosel.villages.api.events;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.features.quest.QuestDefinition;

@Getter
public final class VillageQuestCompleteEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();

	private final Village village;
	private final QuestDefinition quest;

	public VillageQuestCompleteEvent(Village village, QuestDefinition quest) {
		this.village = village;
		this.quest = quest;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
