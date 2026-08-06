package pl.kiosel.villages.addons.antylogout;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CombatManager {

	private final Map<UUID, Combat> combatMap = new ConcurrentHashMap<>();

	public void addCombat(UUID player, UUID opponent, long duration) {
		this.combatMap.put(player, new Combat(opponent, duration));
	}

	public void removeCombat(UUID player) {
		this.combatMap.remove(player);
	}

	public Optional<Combat> getCombat(UUID player) {
		return Optional.ofNullable(this.combatMap.get(player));
	}

	public Optional<UUID> findPlayerByOpponent(UUID opponent) {
		return this.combatMap.entrySet().stream()
				.filter(entry -> Objects.equals(entry.getValue().getOpponent(), opponent))
				.map(Map.Entry::getKey)
				.findFirst();
	}

	public Set<UUID> getCombatPlayers() {
		return Set.copyOf(this.combatMap.keySet());
	}

	public void clear() {
		this.combatMap.clear();
	}
}
