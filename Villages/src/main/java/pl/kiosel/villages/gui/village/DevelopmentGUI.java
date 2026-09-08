package pl.kiosel.villages.gui.village;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.rosacore.compatibility.ZSound;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.features.development.DevelopmentBonus;
import pl.kiosel.villages.data.village.features.development.DevelopmentNode;
import pl.kiosel.villages.data.village.features.development.DevelopmentPurchaseResult;
import pl.kiosel.villages.config.GuiItemConfig;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.user.VillagePermission;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.gui.GUIS;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class DevelopmentGUI extends VillageMenu {
	private static final int[] DEFAULT_NODE_SLOTS = {11, 13, 15, 20, 22, 24, 29, 31, 33};

	public DevelopmentGUI(AdvancedVillages plugin, VillageGUIManager menus, Village village,
						  Player player, Gui parent) {
		super(plugin, menus, village, player, GUIS.DEVELOPMENT, parent);
		addBackButton();

		if (plugin.getDevelopmentManager().getNodes().isEmpty()) {
			GuiItemConfig empty = configured("empty", 22, Material.BARRIER,
					"&7No development nodes are configured.", List.of());
			if (empty.isEnabled()) setItem(empty.getSlot(), empty.createItem());
			return;
		}

		int index = 0;
		for (DevelopmentNode node : plugin.getDevelopmentManager().getNodes()) {
			GuiItemConfig item = configured("nodes." + node.getId(), defaultSlot(index++),
					Material.PAPER, node.getId(), List.of());
			if (!item.isEnabled()) continue;
			boolean unlocked = plugin.getDevelopmentManager().isUnlocked(village, node.getId());
			setButton(item.getSlot(), this.createNodeItem(node, item, unlocked),
					event -> this.purchase(node));
		}
	}

	private ItemStack createNodeItem(DevelopmentNode node, GuiItemConfig item, boolean unlocked) {
		Object[] nodePlaceholders = {"id", node.getId(), "cost", node.getCost(),
				"level", node.getRequiredVillageLevel()};
		String name = replace(item.getName(), nodePlaceholders);
		List<String> lore = item.getLore().stream()
				.map(line -> replace(line, nodePlaceholders))
				.collect(Collectors.toCollection(ArrayList::new));
		if (!lore.isEmpty()) lore.add("");

		boolean levelMet = village.getLevel().getLevel() >= node.getRequiredVillageLevel();
		boolean requirementsMet = plugin.getDevelopmentManager().requirementsMet(village, node);
		String status = unlocked
				? text("status.unlocked", "&aUnlocked")
				: levelMet && requirementsMet
				? text("status.available", "&eClick to unlock")
				: text("status.locked", "&cLocked");
		lore.add(status);
		lore.add(text("cost", "&7Cost: &6%cost%$", "cost", node.getCost()));
		lore.add(text("level", "&7Required village level: &f%level%",
				"level", node.getRequiredVillageLevel()));

		if (!node.getRequirements().isEmpty()) {
			String requirements = node.getRequirements().stream()
					.map(plugin.getDevelopmentManager()::getNode)
					.filter(java.util.Objects::nonNull)
					.map(plugin.getDevelopmentManager()::getNodeName)
					.collect(Collectors.joining(", "));
			lore.add(text("requires", "&7Requires: &f%nodes%", "nodes", requirements));
		}

		if (!node.getBonuses().isEmpty()) {
			lore.add("");
			lore.add(text("bonuses.title", "&dBonuses:"));
			for (Map.Entry<DevelopmentBonus, Integer> bonus : node.getBonuses().entrySet()) {
				lore.add(this.bonusLine(bonus.getKey(), bonus.getValue()));
			}
		}
		return item.createItem(name, lore, unlocked || item.isGlow());
	}

	private String bonusLine(DevelopmentBonus bonus, int value) {
		String key;
		String fallback;
		switch (bonus) {
			case MAX_MEMBERS:
				key = "max-members";
				fallback = "&8 • &a+%value% &7member slots";
				break;
			case QUEST_BANK_PERCENT:
				key = "quest-bank-percent";
				fallback = "&8 • &a+%value%% &7quest bank reward";
				break;
			case WAR_SCORE_PERCENT:
				key = "war-score-percent";
				fallback = "&8 • &a+%value%% &7war score";
				break;
			case ATTACK_PROTECTION_PERCENT:
				key = "attack-protection-percent";
				fallback = "&8 • &a+%value%% &7attack protection time";
				break;
			case TELEPORT_DELAY_REDUCTION_PERCENT:
				key = "teleport-delay-reduction-percent";
				fallback = "&8 • &a-%value%% &7teleport delay";
				break;
			default:
				key = "effect-cost-discount-percent";
				fallback = "&8 • &a-%value%% &7effect cost";
		}
		return text("bonuses." + key, fallback, "value", value);
	}

	private GuiItemConfig configured(String id, int slot, Material material,
	                                 String name, List<String> lore) {
		return plugin.getGuiSettings().item(GUIS.DEVELOPMENT,
				"guis.development." + id, slot, material, name, lore);
	}

	private int defaultSlot(int index) {
		return index < DEFAULT_NODE_SLOTS.length ? DEFAULT_NODE_SLOTS[index] : Math.min(35, 9 + index);
	}

	private String text(String path, String fallback, Object... placeholders) {
		return replace(plugin.getGuiSettings().text("guis.development." + path, fallback), placeholders);
	}

	private String replace(String text, Object... placeholders) {
		String result = text == null ? "" : text;
		for (int index = 0; index + 1 < placeholders.length; index += 2) {
			result = result.replace("%" + placeholders[index] + "%", String.valueOf(placeholders[index + 1]));
		}
		return result;
	}

	private void purchase(DevelopmentNode node) {
		if (!hasPermission(VillagePermission.DEVELOPMENT_BUY)) return;
		DevelopmentPurchaseResult result = plugin.getDevelopmentManager().unlock(viewer, village, node.getId());
		switch (result) {
			case SUCCESS:
				playSound(ZSound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
				reopen(GUIS.DEVELOPMENT);
				return;
			case ALREADY_UNLOCKED:
				messages.sendPrefixed(viewer, Lang.DEVELOPMENT_ALREADY_UNLOCKED);
				return;
			case LEVEL_REQUIRED:
				messages.sendPrefixed(viewer, Lang.DEVELOPMENT_LEVEL_REQUIRED,
						"level", node.getRequiredVillageLevel());
				return;
			case PREREQUISITE_REQUIRED:
				messages.sendPrefixed(viewer, Lang.DEVELOPMENT_PREREQUISITE_REQUIRED);
				return;
			case NOT_ENOUGH_BANK:
				messages.sendPrefixed(viewer, Lang.DEVELOPMENT_NOT_ENOUGH_BANK,
						"cost", node.getCost(), "bank", village.getBank());
				return;
			default:
				messages.sendPrefixed(viewer, Lang.ADDON_DISABLED, "addon", "development");
		}
	}
}
