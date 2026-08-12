package pl.kiosel.villages.gui.village;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.core.gui.Gui;
import pl.kiosel.dependencies.com.cryptomorin.xseries.XSound;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.quests.QuestDefinition;
import pl.kiosel.villages.addons.quests.QuestPeriod;
import pl.kiosel.villages.addons.quests.QuestReward;
import pl.kiosel.villages.addons.quests.VillageQuestView;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.enums.Permission;
import pl.kiosel.villages.gui.Item;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class QuestInventory extends VillageMenu {

	private static final int PROGRESS_BAR_LENGTH = 20;

	public QuestInventory(AdvancedVillages plugin, VillageGUIManager menus, Village village,
	                      Player player, Gui parent) {
		super(plugin, menus, village, player, GUIS.QUESTS, parent, true);
		addBackButton();

		List<VillageQuestView> views = plugin.getQuestManager().getViews(village);
		if (views.isEmpty()) {
			setItem(13, Item.create(Material.BARRIER, messages.text(Lang.QUESTS_GUI_NONE)));
			return;
		}

		int slot = 9;
		for (VillageQuestView view : views) {
			int questSlot = slot++;
			ItemStack item = this.createQuestItem(view);
			if (!view.isActive() && !view.isCompleted()) {
				setButton(questSlot, item, event -> {
					if (!hasPermission(Permission.QUEST_TOGGLE)) return;
					if (plugin.getQuestManager().activate(event.player, village, view.getDefinition())) {
						messages.sendPrefixed(event.player, Lang.QUESTS_ACTIVATED,
								"quest", this.questName(view.getDefinition()));
						playSound(XSound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
						reopen(GUIS.QUESTS);
					}
				});
			} else {
				setItem(questSlot, item);
			}
		}
	}

	private ItemStack createQuestItem(VillageQuestView view) {
		QuestDefinition definition = view.getDefinition();
		String name = messages.textOrDefault(
				definition.getTranslationPath() + ".name",
				definition.getId()
		);
		String description = messages.textOrDefault(
				definition.getTranslationPath() + ".description",
				""
		);

		List<String> lore = new ArrayList<>();
		lore.add(messages.text(definition.getPeriod() == QuestPeriod.DAILY
				? Lang.QUESTS_GUI_DAILY
				: Lang.QUESTS_GUI_WEEKLY));
		if (!description.isEmpty()) {
			lore.add("");
			Collections.addAll(lore, description.split("\n"));
		}
		lore.add("");
		if (view.isCompleted()) {
			lore.add(messages.text(Lang.QUESTS_GUI_COMPLETED,
					"progress", view.getProgress(),
					"required", definition.getRequiredAmount()));
			lore.add(this.progressBar(view.getProgress(), definition.getRequiredAmount()));
		} else if (view.isActive()) {
			lore.add(messages.text(Lang.QUESTS_GUI_ACTIVE));
			lore.add(messages.text(Lang.QUESTS_GUI_PROGRESS,
					"progress", view.getProgress(),
					"required", definition.getRequiredAmount()));
			lore.add(this.progressBar(view.getProgress(), definition.getRequiredAmount()));
		} else {
			lore.add(messages.text(Lang.QUESTS_GUI_INACTIVE));
			lore.add(messages.text(Lang.QUESTS_GUI_ACTIVATE));
		}
		lore.add(messages.text(Lang.QUESTS_GUI_RESET,
				"time", messages.formatDuration(view.getUntilReset())));
		this.addRewards(lore, definition.getReward());

		return Item.create(definition.getIcon(), name, lore, view.isCompleted());
	}

	private String questName(QuestDefinition definition) {
		return messages.textOrDefault(
				definition.getTranslationPath() + ".name",
				definition.getId()
		);
	}

	private void addRewards(List<String> lore, QuestReward reward) {
		lore.add("");
		lore.add(messages.text(Lang.QUESTS_GUI_REWARDS));
		if (reward.getBank() > 0) {
			lore.add(messages.text(Lang.QUESTS_GUI_REWARD_BANK, "amount", reward.getBank()));
		}
		if (reward.getExperience() > 0) {
			lore.add(messages.text(Lang.QUESTS_GUI_REWARD_EXPERIENCE,
					"amount", reward.getExperience()));
		}
		if (reward.getPoints() > 0) {
			lore.add(messages.text(Lang.QUESTS_GUI_REWARD_POINTS, "amount", reward.getPoints()));
		}
	}

	private String progressBar(int progress, int required) {
		int completedBars = Math.min(PROGRESS_BAR_LENGTH,
				(int) Math.floor(PROGRESS_BAR_LENGTH * (progress / (double) required)));
		StringBuilder bar = new StringBuilder("&8[");
		for (int index = 0; index < PROGRESS_BAR_LENGTH; index++) {
			bar.append(index < completedBars ? "&a|" : "&7|");
		}
		return bar.append("&8]").toString();
	}
}
