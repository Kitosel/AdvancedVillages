package pl.kiosel.villages.data.village;

import org.bukkit.Location;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.level.Level;
import pl.kiosel.villages.manager.VillageNameGenerator;

import java.util.Set;
import java.util.UUID;

public class VillageBuilder {

	private final Village village;
	private final VillageNameGenerator villageNameGenerator;

	public VillageBuilder(UUID uuid, Location location) {
		this.village = new Village(uuid, location);
		this.villageNameGenerator = new VillageNameGenerator();
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
		this.village.setName(villageNameGenerator.getRandomName());
		return this;
	}

	public VillageBuilder setLevel(Level level) {
		this.village.setLevel(level);
		return this;
	}

	public VillageBuilder setLives(int life) {
		this.village.setLives(life);
		return this;
	}

	public VillageBuilder setEffectsDefault() {
		setEffects(false, false, false, false);
		setEffectsActive(false, false, false, false);
		return this;
	}

	public VillageBuilder setEffects(String effects) {
		String[] effects_data_array = effects.split(";");
		this.setEffects(Boolean.parseBoolean(effects_data_array[0]),
				Boolean.parseBoolean(effects_data_array[1]),
				Boolean.parseBoolean(effects_data_array[2]),
				Boolean.parseBoolean(effects_data_array[3]));
		return this;
	}

	public VillageBuilder setEffectsActive(String effects) {
		String[] effects_active_array = effects.split(";");
		this.setEffectsActive(Boolean.parseBoolean(effects_active_array[0]),
				Boolean.parseBoolean(effects_active_array[1]),
				Boolean.parseBoolean(effects_active_array[2]),
				Boolean.parseBoolean(effects_active_array[3]));
		return this;
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
		this.village.setTag("none");
		return this;
	}

	public VillageBuilder setBank(int bank) {
		this.village.setBank(bank);
		return this;
	}

	public Village build() {
		return this.village;
	}
}