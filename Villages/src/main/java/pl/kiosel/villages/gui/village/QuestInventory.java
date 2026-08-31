package pl.kiosel.villages.gui.village;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.rosacore.compatibility.ZSound;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.quests.QuestDefinition;
import pl.kiosel.villages.addons.quests.QuestPeriod;
import pl.kiosel.villages.addons.quests.QuestReward;
import pl.kiosel.villages.addons.quests.VillageQuestView;
import pl.kiosel.villages.config.GuiItemConfig;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.village.Permission;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.gui.GUIS;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;

import java.util.ArrayList;
import java.util.List;

public final class QuestInventory extends VillageMenu {

	public QuestInventory(AdvancedVillages plugin, VillageGUIManager menus, Village village,
	                      Player player, Gui parent) {
		super(plugin, menus, village, player, GUIS.QUESTS, parent, true);
		addBackButton();

		List<VillageQuestView> views = plugin.getQuestManager().getViews(village);
		if (views.isEmpty()) {
			GuiItemConfig empty = configured("empty", 13, Material.BARRIER,
					"&cNo quests are currently configured.", List.of());
			if (empty.isEnabled()) setItem(empty.getSlot(), empty.createItem());
			return;
		}

		int slot = plugin.getGuiSettings().integer("guis.quests.start-slot", 9, 9,
				Math.max(9, menuConfig.getSize() - 1));
		for (VillageQuestView view : views) {
			QuestDefinition definition = view.getDefinition();
			GuiItemConfig visual = configured("tasks." + definition.getId(), slot,
					Material.PAPER, definition.getId(), List.of());
			if (!visual.isEnabled()) continue;
			int questSlot = slot++;
			ItemStack item = this.createQuestItem(view, visual);
			if (!view.isActive() && !view.isCompleted()) {
				setButton(questSlot, item, event -> {
					if (!hasPermission(Permission.QUEST_TOGGLE)) return;
					if (plugin.getQuestManager().activate(event.getPlayer(), village, view.getDefinition())) {
						messages.sendPrefixed(event.getPlayer(), Lang.QUESTS_ACTIVATED,
								"quest", this.questName(view.getDefinition()));
						playSound(ZSound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
						reopen(GUIS.QUESTS);
					}
				});
			} else {
				setItem(questSlot, item);
			}
		}
	}

	private ItemStack createQuestItem(VillageQuestView view, GuiItemConfig visual) {
		QuestDefinition definition = view.getDefinition();
		List<String> lore = new ArrayList<>();
		lore.add(definition.getPeriod() == QuestPeriod.DAILY
				? guiText("period.daily", "&eDaily quest")
				: guiText("period.weekly", "&6Weekly quest"));
		if (!visual.getLore().isEmpty()) {
			lore.add("");
			lore.addAll(visual.getLore());
		}
		lore.add("");
		if (view.isCompleted()) {
			lore.add(guiText("status.completed", "&aCompleted! &7(%progress%/%required%)",
					"progress", view.getProgress(),
					"required", definition.getRequiredAmount()));
			lore.add(this.progressBar(view.getProgress(), definition.getRequiredAmount()));
		} else if (view.isActive()) {
			lore.add(guiText("status.active", "&aActive - progress is being collected"));
			lore.add(guiText("status.progress", "&7Progress: &e%progress%&7/&e%required%",
					"progress", view.getProgress(),
					"required", definition.getRequiredAmount()));
			lore.add(this.progressBar(view.getProgress(), definition.getRequiredAmount()));
		} else {
			lore.add(guiText("status.inactive", "&cInactive - progress is not being collected"));
			lore.add(guiText("status.activate", "&eClick to activate this quest for the village."));
		}
		lore.add(guiText("reset", "&7Resets in: &f%time%",
				"time", messages.formatDuration(view.getUntilReset())));
		this.addRewards(lore, definition.getReward());

		return visual.createItem(visual.getName(), lore, view.isCompleted() || visual.isGlow());
	}

	private String questName(QuestDefinition definition) {
		return plugin.getGuiSettings().text(
				"guis.quests.tasks." + definition.getId() + ".name", definition.getId());
	}

	private void addRewards(List<String> lore, QuestReward reward) {
		lore.add("");
		lore.add(guiText("rewards.title", "&6Rewards:"));
		int bankReward = plugin.getDevelopmentManager().applyQuestBankReward(village, reward.getBank());
		if (bankReward > 0) {
			lore.add(guiText("rewards.bank", "&7 • Village bank: &6%amount%$",
					"amount", bankReward));
		}
		if (reward.getExperience() > 0) {
			lore.add(guiText("rewards.experience", "&7 • Every online member: &e%amount% XP",
					"amount", reward.getExperience()));
		}
		if (reward.getPoints() > 0) {
			lore.add(guiText("rewards.points", "&7 • Every member: &b%amount% points",
					"amount", reward.getPoints()));
		}
	}

	private String progressBar(int progress, int required) {
		int length = plugin.getGuiSettings().integer("guis.quests.progress-bar.length", 20, 1, 100);
		int completedBars = Math.min(length,
				(int) Math.floor(length * (progress / (double) required)));
		StringBuilder bar = new StringBuilder(guiText("progress-bar.prefix", "&8["));
		String completed = guiText("progress-bar.completed", "&a|");
		String remaining = guiText("progress-bar.remaining", "&7|");
		for (int index = 0; index < length; index++) {
			bar.append(index < completedBars ? completed : remaining);
		}
		return bar.append(guiText("progress-bar.suffix", "&8]")).toString();
	}

	private GuiItemConfig configured(String id, int slot, Material material, String name, List<String> lore) {
		return plugin.getGuiSettings().item(GUIS.QUESTS,
				"guis.quests." + id, slot, material, name, lore);
	}

	private String guiText(String path, String fallback, Object... placeholders) {
		String result = plugin.getGuiSettings().text("guis.quests." + path, fallback);
		for (int index = 0; index + 1 < placeholders.length; index += 2) {
			result = result.replace("%" + placeholders[index] + "%",
					String.valueOf(placeholders[index + 1]));
		}
		return result;
	}
}
