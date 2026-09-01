package pl.kiosel.villages.storage;

import org.bukkit.Location;
import pl.kiosel.villages.data.user.User;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

record VillageData(
		UUID uuid,
		String name,
		User owner,
		Location location,
		Location home,
		Set<User> members,
		boolean pvp,
		int lives,
		int bank,
		int level,
		String purchasedEffects,
		String activeEffects,
		Instant protection,
		String tag,
		boolean tnt,
		boolean animationsEnabled
) {
	VillageData {
		members = members == null
				? Collections.emptySet()
				: Collections.unmodifiableSet(new HashSet<>(members));
	}
}
