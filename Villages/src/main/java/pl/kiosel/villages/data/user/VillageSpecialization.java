package pl.kiosel.villages.data.user;

import lombok.Getter;

import java.util.Locale;
import java.util.Optional;

@Getter
public enum VillageSpecialization {

	WARRIOR("warrior", 8.0D),
	DEFENDER("defender", 10.0D),
	FARMER("farmer", 15.0D),
	FORESTER("forester", 20.0D),
	MINER("miner", 25.0D),
	MERCHANT("merchant", 5.0D),
	HEALER("healer", 15.0D);

	private final String id;
	private final double defaultBonus;

	VillageSpecialization(String id, double defaultBonus) {
		this.id = id;
		this.defaultBonus = defaultBonus;
	}

	public static Optional<VillageSpecialization> fromId(String id) {
		if (id == null || id.isBlank()) return Optional.empty();
		String normalized = id.trim().toLowerCase(Locale.ROOT);
		for (VillageSpecialization specialization : values()) {
			if (specialization.id.equals(normalized)) return Optional.of(specialization);
		}
		return Optional.empty();
	}
}
