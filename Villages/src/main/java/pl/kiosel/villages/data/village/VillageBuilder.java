package pl.kiosel.villages.data.village;

import org.bukkit.Location;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.level.Level;
import pl.kiosel.villages.manager.VillageNameGenerator;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class VillageBuilder {

	private final Village village;
	private final VillageNameGenerator nameGenerator = new VillageNameGenerator();

	public VillageBuilder(UUID uuid, Location location) {
		this.village = new Village(uuid, location);
	}

	public VillageBuilder setLocation(Location location) {
		this.village.setLocation(location);
		return this;
	}

	public VillageBuilder setTeleport(Location location) {
		this.village.setHome(location);
		return this;
	}

	public VillageBuilder setOwner(User owner) {
		this.village.setOwner(owner);
		return this;
	}

	public VillageBuilder setMembers(Set<User> members) {
		this.village.setMembers(members);
		return this;
	}

	public VillageBuilder setName(String name) {
		this.village.setName(name);
		return this;
	}

	public VillageBuilder setRandomVillageName() {
		return this.setName(this.nameGenerator.getRandomName());
	}

	public VillageBuilder setLevel(Level level) {
		this.village.setLevel(level);
		return this;
	}

	public VillageBuilder setLives(int lives) {
		this.village.setLives(lives);
		return this;
	}

	public VillageBuilder setBank(int bank) {
		this.village.setBank(bank);
		return this;
	}

	public VillageBuilder setEffectsDefault() {
		return this.setEffects(false, false, false, false)
				.setEffectsActive(false, false, false, false);
	}

	public VillageBuilder setEffects(String serialized) {
		boolean[] values = parseEffects(serialized);
		return this.setEffects(values[0], values[2], values[1], values[3]);
	}

	public VillageBuilder setEffectsActive(String serialized) {
		boolean[] values = parseEffects(serialized);
		return this.setEffectsActive(values[0], values[2], values[1], values[3]);
	}

	public VillageBuilder setEffects(boolean regeneration, boolean jump, boolean speed, boolean haste) {
		this.village.setRegeneration(regeneration);
		this.village.setJump(jump);
		this.village.setSpeed(speed);
		this.village.setHaste(haste);
		return this;
	}

	public VillageBuilder setEffectsActive(boolean regeneration, boolean jump, boolean speed, boolean haste) {
		this.village.setRegenerationActive(regeneration);
		this.village.setJumpActive(jump);
		this.village.setSpeedActive(speed);
		this.village.setHasteActive(haste);
		return this;
	}

	public VillageBuilder setTag(String tag) {
		this.village.setTag(tag);
		return this;
	}

	public VillageBuilder noTag() {
		return this.setTag("none");
	}

	public Village build() {
		Objects.requireNonNull(this.village.getOwner(), "owner");
		Objects.requireNonNull(this.village.getLevel(), "level");
		if (this.village.getName() == null || this.village.getName().isBlank()) {
			throw new IllegalStateException("Village name cannot be blank");
		}
		if (this.village.getLocation().isEmpty()) {
			throw new IllegalStateException("Village location is required");
		}
		return this.village;
	}

	private static boolean[] parseEffects(String serialized) {
		boolean[] values = new boolean[4];
		if (serialized == null || serialized.isBlank()) return values;
		String[] parts = serialized.split(";");
		for (int index = 0; index < values.length && index < parts.length; index++) {
			values[index] = Boolean.parseBoolean(parts[index]);
		}
		return values;
	}
}
