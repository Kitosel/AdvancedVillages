package pl.kiosel.villages.addons.antylogout;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.kiosel.core.thread.MetaTask;

import java.time.Instant;
import java.util.UUID;

public class CombatTask implements MetaTask {

	private final CombatCache combatCache;
	private final CombatConfig combatConfig;

	public CombatTask(CombatCache combatCache, CombatConfig combatConfig) {
		this.combatCache = combatCache;
		this.combatConfig = combatConfig;
	}

	@Override
	public void execute() {
		Instant now = Instant.now();
		for (UUID uuid : this.combatCache.getCombatPlayers()) {
			Combat combat;
			Player player = Bukkit.getPlayer(uuid);
			if (player == null || !player.isOnline() || (combat = this.combatCache.getCombat(uuid).orElse(null)) == null) continue;
			long timeLeft = combat.getTimeLeft(now);
			boolean hasElapsed = combat.hasElapsed(now);
			if (hasElapsed) {
				this.combatCache.removeCombat(uuid);
				this.combatConfig.getCombatEndMessages().forEach(message -> message.send(player));
				continue;
			}
			this.combatConfig.getCombatMessage().send(player, "time", timeLeft);
		}
	}

	@Override
	public Type getType() {
		return Type.ASYNC;
	}
}