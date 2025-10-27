package pl.kiosel.villages.village;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class VillageNameGenerator {

	private final Random random = new Random();

	private final List<String> adjectives = Arrays.asList(
			"Shadow", "Mossy", "Burning", "Frozen", "Golden",
			"Cursed", "Electric", "Ancient", "Crystal", "Mystic",
			"Dark", "Silver", "Fiery", "Thunder", "Ghostly",
			"Royal", "Icy", "Emerald", "Luminous", "Silent",
			"Scarlet", "Arcane", "Enchanted", "Radiant", "Vengeful",
			"Celestial", "Iron", "Twilight", "Solar", "Venomous"
	);

	private final List<String> animal = Arrays.asList(
			"Wolf", "Dragon", "Phoenix", "Goblin", "Wizard",
			"Lion", "Beast", "Vortex", "Titan", "Spirit",
			"Lama", "Raven", "Golem", "Hydra", "Sorcerer",
			"Cat", "Knight", "Serpent", "Vanilla", "Rose",
			"Griffin", "Leviathan", "Wraith", "Demon", "Pegasus",
			"Reaper", "Basilisk", "Minotaur", "Specter", "Kraken"
	);

	private final List<String> place = Arrays.asList(
			"Blade", "Cave", "Tower", "Temple", "Forest",
			"Valley", "Fortress", "Shrine", "Keep", "Sanctum",
			"Crypt", "Hollow", "Peak", "Citadel", "Sanctuary",
			"Stronghold", "Chamber", "Ruins", "Spire", "Harbor",
			"Realm", "Tomb", "Domain", "Nest", "Abyss",
			"Portal", "Garden", "Horizon", "Vault", "Core"
	);

	public String getRandomName() {
		return getRandom(adjectives) + getRandom(animal) + getRandom(place);
	}

	private String getRandom(List<String> list) {
		return list.get(random.nextInt(list.size()));
	}
}
