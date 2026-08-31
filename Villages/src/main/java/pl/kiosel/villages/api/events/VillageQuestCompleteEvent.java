package pl.kiosel.villages.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import pl.kiosel.villages.addons.quests.QuestDefinition;
import pl.kiosel.villages.data.village.Village;

public final class VillageQuestCompleteEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();

	private final Village village;
	private final QuestDefinition quest;

	public VillageQuestCompleteEvent(Village village, QuestDefinition quest) {
		this.village = village;
		this.quest = quest;
	}

	public Village getVillage() {
		return this.village;
	}

	public QuestDefinition getQuest() {
		return this.quest;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
