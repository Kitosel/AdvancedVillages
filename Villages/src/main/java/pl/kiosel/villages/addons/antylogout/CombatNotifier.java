package pl.kiosel.villages.addons.antylogout;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.kiosel.core.chat.AdventureUtils;
import pl.kiosel.core.dependencies.net.kyori.adventure.text.Component;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.VillageMessages;
import pl.kiosel.villages.enums.Lang;

final class CombatNotifier {

	private final AdvancedVillages plugin;

	CombatNotifier(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	void combatStarted(Player player, String victim) {
		this.messages().send(player, Lang.ANTILOGOUT_STARTED, "victim", victim);
	}

	void combatAttacked(Player player, String attacker) {
		this.messages().send(player, Lang.ANTILOGOUT_ATTACKED, "attacker", attacker);
	}

	void combatTick(Player player, long remainingSeconds) {
		this.sendActionBar(player, Lang.ANTILOGOUT_ACTIONBAR_MESSAGE, "time", remainingSeconds);
	}

	void combatEnded(Player player) {
		this.messages().send(player, Lang.ANTILOGOUT_END_COMBAT);
		this.sendActionBar(player, Lang.ANTILOGOUT_END_COMBAT);
	}

	void opponentRemoved(Player player, String opponent) {
		this.messages().send(player, Lang.ANTILOGOUT_REMOVE_COMBAT, "opponent", opponent);
	}

	void commandBlocked(Player player, String command) {
		this.messages().sendPrefixed(player, Lang.ANTILOGOUT_BLOCK_COMMAND, "command", command);
	}

	void regionBlocked(Player player, String region) {
		this.messages().sendPrefixed(player, Lang.ANTILOGOUT_BLOCKED_REGION, "region", region);
	}

	void broadcastCombatQuit(Player player) {
		for (Player recipient : Bukkit.getOnlinePlayers()) {
			this.messages().send(recipient, Lang.ANTILOGOUT_QUIT_BROADCAST, "player", player.getName());
		}
	}

	void clearActionBar(Player player) {
		AdventureUtils.sendActionBar(Component.empty(), player);
	}

	private void sendActionBar(Player player, Lang key, Object... placeholders) {
		String content = this.messages().text(key, placeholders);
		AdventureUtils.sendActionBar(AdventureUtils.formatComponent(content), player);
	}

	private VillageMessages messages() {
		return this.plugin.getMessages();
	}
}
