package pl.kiosel.villages.gui.village;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.features.diplomacy.AllianceRequest;
import pl.kiosel.villages.data.village.features.diplomacy.VillageWar;
import pl.kiosel.villages.data.village.features.diplomacy.WarState;
import pl.kiosel.villages.config.GuiItemConfig;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.gui.GUIS;
import pl.kiosel.villages.gui.VillageGUIManager;
import pl.kiosel.villages.gui.VillageMenu;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DiplomacyGUI extends VillageMenu {

	public DiplomacyGUI(AdvancedVillages plugin, VillageGUIManager menus, Village village,
						Player player, Gui parent) {
		super(plugin, menus, village, player, GUIS.DIPLOMACY, parent, true);
		addBackButton();

		List<ItemStack> entries = new ArrayList<>();
		this.addAlliances(entries);
		this.addRequests(entries);
		this.addWars(entries);
		if (entries.isEmpty()) {
			GuiItemConfig empty = configured("empty", Material.BARRIER,
					"&7No diplomatic relations", List.of());
			if (empty.isEnabled()) setItem(empty.getSlot(), empty.createItem());
			return;
		}

		int slot = plugin.getGuiSettings().integer("guis.diplomacy.start-slot", 9, 9,
				Math.max(9, menuConfig.getSize() - 1));
		for (ItemStack entry : entries) {
			setItem(slot++, entry);
		}
	}

	private void addAlliances(List<ItemStack> entries) {
		GuiItemConfig item = configured("ally", Material.EMERALD,
				"&aAlliance: &f%village%", List.of("&7This village is your ally."));
		if (!item.isEnabled()) return;
		for (Village ally : plugin.getDiplomacyManager().getAllies(village)) {
			entries.add(item.createItem(
					replace(item.getName(), "village", ally.getName()),
					replace(item.getLore(), "village", ally.getName())));
		}
	}

	private void addRequests(List<ItemStack> entries) {
		Instant now = Instant.now();
		for (AllianceRequest request : plugin.getDiplomacyManager().getRequests(village)) {
			boolean incoming = request.getTargetVillageId().equals(village.getUUID());
			UUID otherId = incoming ? request.getSenderVillageId() : request.getTargetVillageId();
			Village other = plugin.getVillageManager().findByUuid(otherId).orElse(null);
			if (other == null) continue;
			String id = incoming ? "request-incoming" : "request-outgoing";
			String name = incoming ? "&eAlliance request from %village%"
					: "&6Alliance request to %village%";
			GuiItemConfig item = configured(id, incoming ? Material.PAPER : Material.MAP,
					name, List.of("&7Expires in: &f%time%"));
			if (!item.isEnabled()) continue;
			Duration remaining = Duration.between(now, request.getExpiresAt());
			if (remaining.isNegative()) remaining = Duration.ZERO;
			entries.add(item.createItem(
					replace(replace(item.getName(), "village", other.getName()),
							"time", messages.formatDuration(remaining)),
					replace(replace(item.getLore(), "village", other.getName()),
							"time", messages.formatDuration(remaining))));
		}
	}

	private void addWars(List<ItemStack> entries) {
		Instant now = Instant.now();
		for (VillageWar war : plugin.getDiplomacyManager().getWars(village)) {
			Village enemy = plugin.getDiplomacyManager().getOtherVillage(war, village);
			if (enemy == null) continue;
			VillageWar.Snapshot snapshot = war.snapshot();
			WarState state = war.getState(now);
			boolean attacker = snapshot.getAttackerVillageId().equals(village.getUUID());
			Duration remaining = remaining(snapshot, state, now);
			Material material = state == WarState.PREPARING ? Material.CLOCK
					: state == WarState.ACTIVE ? Material.IRON_SWORD : Material.SHIELD;
			GuiItemConfig item = configured("war", material,
					"&cWar: &f%village%", List.of(
							"&7Status: %state%",
							"&7Remaining: &f%time%",
							"&7Score: &e%our_score%&7:&e%enemy_score%"));
			if (!item.isEnabled()) continue;
			String name = warText(item.getName(), enemy, state, remaining,
					attacker ? snapshot.getAttackerScore() : snapshot.getDefenderScore(),
					attacker ? snapshot.getDefenderScore() : snapshot.getAttackerScore());
			List<String> lore = new ArrayList<>(item.getLore().size());
			for (String line : item.getLore()) {
				lore.add(warText(line, enemy, state, remaining,
						attacker ? snapshot.getAttackerScore() : snapshot.getDefenderScore(),
						attacker ? snapshot.getDefenderScore() : snapshot.getAttackerScore()));
			}
			entries.add(item.createItem(name, lore));
		}
	}

	private String warText(String text, Village enemy, WarState state, Duration remaining,
	                       int ourScore, int enemyScore) {
		return text.replace("%village%", enemy.getName())
				.replace("%state%", stateName(state))
				.replace("%time%", messages.formatDuration(remaining))
				.replace("%our_score%", Integer.toString(ourScore))
				.replace("%enemy_score%", Integer.toString(enemyScore));
	}

	private String stateName(WarState state) {
		switch (state) {
			case PREPARING: return plugin.getGuiSettings().text(
					"guis.diplomacy.states.preparing", "&ePreparation");
			case ACTIVE: return plugin.getGuiSettings().text(
					"guis.diplomacy.states.active", "&cActive");
			default: return plugin.getGuiSettings().text(
					"guis.diplomacy.states.finished", "&7Cooldown");
		}
	}

	private GuiItemConfig configured(String id, Material material, String name, List<String> lore) {
		return plugin.getGuiSettings().item(GUIS.DIPLOMACY,
				"guis.diplomacy." + id, 13, material, name, lore);
	}

	private Duration remaining(VillageWar.Snapshot war, WarState state, Instant now) {
		Instant until = state == WarState.PREPARING ? war.getStartsAt()
				: state == WarState.ACTIVE ? war.getScheduledEndsAt() : war.getCooldownUntil();
		if (until == null) return Duration.ZERO;
		Duration remaining = Duration.between(now, until);
		return remaining.isNegative() ? Duration.ZERO : remaining;
	}

	private String replace(String text, String key, String value) {
		return text.replace("%" + key + "%", value == null ? "" : value);
	}

	private List<String> replace(List<String> lines, String key, String value) {
		return lines.stream().map(line -> replace(line, key, value)).toList();
	}
}
