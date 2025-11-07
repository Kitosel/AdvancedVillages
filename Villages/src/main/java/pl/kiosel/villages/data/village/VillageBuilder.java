package pl.kiosel.villages.data.village;

import org.bukkit.Location;
import pl.kiosel.villages.data.village.level.Level;
import pl.kiosel.villages.manager.VillageNameGenerator;

import java.util.List;
import java.util.UUID;

public class VillageBuilder {

	private final Village village;
	private final VillageNameGenerator villageNameGenerator;

	public VillageBuilder(Location location) {
		this.village = new Village(location);
		this.villageNameGenerator = new VillageNameGenerator();
	}

	public VillageBuilder setOwner(String owner) {
		this.village.setOwner(owner);
		return this;
	}

	public VillageBuilder setOwnerUUID(UUID uuid) {
		this.village.setOwnerUUID(uuid);
		return this;
	}

	public VillageBuilder setMembers(List<UUID> members) {
		this.village.setMembers(members);
		return this;
	}

	public VillageBuilder setVillageName(String name) {
		this.village.setVillageName(name);
		return this;
	}

	public VillageBuilder setRandomVillageName() {
		this.village.setVillageName(villageNameGenerator.getRandomName());
		return this;
	}

	public VillageBuilder setLevel(Level level) {
		this.village.setLevel(level);
		return this;
	}

	public VillageBuilder setLife(int life) {
		this.village.setLife(life);
		return this;
	}

	public VillageBuilder setEffectsDefault() {
		setEffects(false, false, false, false);
		setEffectsActive(false, false, false, false);
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

	public VillageBuilder setTeleport(Location location) {
		this.village.setTeleport(location);
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