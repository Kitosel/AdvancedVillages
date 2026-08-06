package pl.kiosel.villages.addons.antylogout;

import java.util.*;
import java.util.stream.Collectors;

public class CombatCache {

	private final Map<UUID, Combat> combatMap = new HashMap<>();

	public void addCombat(UUID player, UUID opponent, long duration) {
		this.combatMap.put(player, new Combat(opponent, duration));
	}

	public void addCombat(UUID player, long duration) {
		this.combatMap.put(player, new Combat(null, duration));
	}

	public void removeCombat(UUID player) {
		this.combatMap.remove(player);
	}

	public Optional<Combat> getCombat(UUID player) {
		return Optional.ofNullable(this.combatMap.get(player));
	}

	public Optional<UUID> findPlayerByOpponent(UUID opponent) {
		return this.combatMap.entrySet().stream().filter(entry -> entry.getValue().getOpponent().equals(opponent)).map(Map.Entry::getKey).findFirst();
	}

	public Set<UUID> getCombatPlayers() {
		return this.combatMap.keySet().stream().filter(Objects::nonNull).collect(Collectors.toSet());
	}
}
