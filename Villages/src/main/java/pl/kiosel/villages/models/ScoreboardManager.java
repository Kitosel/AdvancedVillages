package pl.kiosel.villages.models;

import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.kiosel.villages.Wioski;
import pl.kiosel.villages.config.Config;
import pl.kiosel.villages.config.GuiConfig;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.village.UpgradeManager;
import pl.kiosel.villages.village.Village;
import pl.kiosel.villages.village.VillageManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static pl.kiosel.common.utils.ColorUtils.tl;

public class ScoreboardManager {

	private final Wioski plugin;
	private final Map<UUID, FastBoard> boards = new ConcurrentHashMap<>();
	private final List<String> titleFrames = new ArrayList<>();
	private int animationIndex = 0;

	public ScoreboardManager(Wioski plugin) {
		this.plugin = plugin;
		setupTitleAnimation();
		startAnimationTask();
		startUpdateTask();
	}

	private void setupTitleAnimation() {
		String base = ChatColor.translateAlternateColorCodes('&', Config.scoreboard_title);
		titleFrames.add("&b&l" + base);
		titleFrames.add("&3&l" + base);
		titleFrames.add("&9&l" + base);
		titleFrames.add("&1&l" + base);
		titleFrames.add("&3&l" + base);
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

		for (String line : Config.scoreboard_score) {
			line = replaceWith(player, village, line);
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
		}.runTaskTimer(plugin, 0L, 10L);
	}

	private void startUpdateTask() {
		new BukkitRunnable() {
			@Override
			public void run() {
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
				.replace("%player_money%", plugin.getApi().getMoney(player)+"")
				.replace("%village_owner%", village == null ? plugin.getLang().getMessage(Lang.SCOREBOARD_NONE) : village.getOwner())
				.replace("%village_name%", village == null ? plugin.getLang().getMessage(Lang.SCOREBOARD_NONE) : village.getVillageName())
				.replace("%village_tag%", village == null ? plugin.getLang().getMessage(Lang.SCOREBOARD_NONE) : village.isTag() ? village.getTag() : notag)
				.replace("%village_level%", village == null ? "-" : village.getLevel()+"")
				.replace("%village_cost%", village == null ? "-" : UpgradeManager.getCostForLevel(village.getLevel())+"")
				.replace("%village_teleport%", village == null ? "-" : village.tpToString())
				.replace("%village_bank%", village == null ? "-" : village.getBank()+"")
				.replace("%village_life%", village == null ? "-" : village.getLife()+"")
				.replace("%village_size%", village == null ? "-" : village.getSize()+"")
				.replace("%istagset%", village == null ? "-" : village.isTag() ? tagnotset : tagset));
	}
}