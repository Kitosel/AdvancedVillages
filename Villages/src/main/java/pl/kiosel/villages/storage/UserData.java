package pl.kiosel.villages.storage;

import java.util.UUID;

record UserData(
		UUID uuid,
		String name,
		int points,
		int kills,
		int deaths,
		int assists,
		String role
) {
}
