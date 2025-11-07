package pl.kiosel.villages.addons.scoreboard;

import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.kiosel.core.hooks.EconomyManager;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.GuiConfig;
import pl.kiosel.villages.settings.Settings;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.manager.VillageManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static pl.kiosel.core.utils.ColorUtils.tl;

public class ScoreboardManager {

	private final AdvancedVillages plugin;
	private final Map<UUID, FastBoard> boards = new ConcurrentHashMap<>();
	private final List<String> titleFrames = new ArrayList<>();
	private int animationIndex = 0;

	public ScoreboardManager(AdvancedVillages plugin) {
		this.plugin = plugin;
		setupTitleAnimation();
		startAnimationTask();
		startUpdateTask();
	}

	private void setupTitleAnimation() {
		if (plugin.getScoreboardHandler().scoreboardAnimationEnabled()) {
			for (String string : plugin.getScoreboardHandler().getTitles()) {
				this.titleFrames.add(tl(string));
			}
		} else {
			titleFrames.add(plugin.getScoreboardHandler().scoreboardTitle());
		}
	}

	public void createBoard(Player player) {
		FastBoard board = new FastBoard(player);
		boards.put(player.getUniqueId(), board);
		updateBoard(board);
	}

	public void removeBoard(Player player) {
		FastBoard board = boards.remove(player.getUniqueId());
		if (board != null) board.delete();
	}

	private void updateBoard(FastBoard board) {
		Player player = board.getPlayer();
		Village village = VillageManager.getVillageByOfflineOwner(player.getName());

		List<String> lines = new ArrayList<>();

		for (String line : plugin.getScoreboardHandler().getScore()) {
			line = replaceWith(player, village, line);
			if (plugin.isPlaceholder())
				line = plugin.getPlaceholder().replacePlaceholder(player, line);
			lines.add(line);
		}

		board.updateTitle(ChatColor.translateAlternateColorCodes('&', titleFrames.get(animationIndex)));
		board.updateLines(lines);
	}

	private void startAnimationTask() {
		new BukkitRunnable() {
			@Override
			public void run() {
				animationIndex = (animationIndex + 1) % titleFrames.size();
			}
		}.runTaskTimer(plugin, plugin.getScoreboardHandler().getAnimationSpeed(), plugin.getScoreboardHandler().getAnimationSpeed());
	}

	private void startUpdateTask() {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (Settings.ADDONS_SCOREBOARD_ENABLE.getBoolean())
					for (FastBoard board : boards.values()) {
						updateBoard(board);
					}
			}
		}.runTaskTimer(plugin, 20L, 40L);
	}

	private String replaceWith(Player player, Village village, String string) {
		String notag = GuiConfig.no_tag;
		String tagset = GuiConfig.guis_village_setting_tag_set;
		String tagnotset = GuiConfig.guis_village_setting_tag_notset;
		return tl(string
				.replace("%player_name%", player.getName())
				.replace("%player_money%", EconomyManager.getBalance(player)+"")
				.replace("%village_owner%", village == null ? plugin.getScoreboardHandler().scoreboardNoVillage() : village.getOwner())
				.replace("%village_name%", village == null ? plugin.getScoreboardHandler().scoreboardNoVillage() : village.getVillageName())
				.replace("%village_tag%", village == null ? plugin.getScoreboardHandler().scoreboardNoVillage() : village.isTag() ? village.getTag() : notag)
				.replace("%village_level%", village == null ? "-" : village.getLevel().getLevel()+"")
				.replace("%village_cost%", village == null ? "-" : village.getLevel().getCostEconomy()+"")
				.replace("%village_teleport%", village == null ? "-" : village.tpToString())
				.replace("%village_bank%", village == null ? "-" : village.getBank()+"")
				.replace("%village_life%", village == null ? "-" : village.getLife()+"")
				.replace("%village_size%", village == null ? "-" : village.getLevel().getSize()+"")
				.replace("%istagset%", village == null ? "-" : village.isTag() ? tagnotset : tagset));
	}
}