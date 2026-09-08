package pl.kiosel.villages.data.user;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import pl.kiosel.rosacore.utils.ColorUtils;
import pl.kiosel.villages.data.AbstractMutableEntity;
import pl.kiosel.villages.data.village.Village;

import java.util.*;

public class User extends AbstractMutableEntity {

	private final UUID uuid;
	@Getter
	private final UserProfile profile;
	@Getter
	private final UserRank rank;
	private final UserVillageState villageState;
	private volatile String name;

	User(UUID uuid, String name, UserProfile profile, int startingPoints) {
		this.uuid = Objects.requireNonNull(uuid, "uuid");
		this.name = Objects.requireNonNull(name, "name");
		this.profile = Objects.requireNonNull(profile, "profile");
		this.rank = new UserRank(this, startingPoints);
		this.villageState = new UserVillageState(this::markChanged);
		this.markChanged();
	}

	public UUID getUUID() {
		return this.uuid;
	}

	@Override
	public String getName() {
		return this.name;
	}

	void setName(String name) {
		Objects.requireNonNull(name, "name");
		if (this.name.equals(name)) return;
		this.name = name;
		this.markChanged();
	}

	@Override
	public UnitType getType() {
		return UnitType.USER;
	}

	@Nullable
	public Village getPresentVillage() {
		return this.villageState.getPresentVillage();
	}

	public Optional<Village> getVillage() {
		return this.villageState.getVillage();
	}

	public boolean hasVillage() {
		return this.villageState.hasVillage();
	}

	public void setVillage(@Nullable Village village) {
		this.villageState.setVillage(village);
	}

	public void removeVillage() {
		this.villageState.clear();
	}

	public Set<VillagePermission> getPermissions() {
		return this.villageState.getPermissions();
	}

	public Optional<String> getRoleId() {
		return this.villageState.getRoleId();
	}

	public Optional<VillageSpecialization> getSpecialization() {
		return this.villageState.getSpecialization();
	}

	public long getSpecializationChangedAt() {
		return this.villageState.getSpecializationChangedAt();
	}

	public void setSpecialization(@Nullable VillageSpecialization specialization, long changedAt) {
		this.villageState.setSpecialization(specialization, changedAt);
	}

	public void assignRole(String roleId, Set<VillagePermission> permissions) {
		this.villageState.assignRole(roleId, permissions);
	}

	public boolean isOwner() {
		Village village = this.getPresentVillage();
		return village != null && village.isOwner(this);
	}

	public boolean hasVillagePermission(VillagePermission permission) {
		return this.villageState.hasPermission(permission, this.isOwner());
	}

	public void addVillagePermission(VillagePermission permission) {
		this.villageState.addPermission(permission);
	}

	public void removeVillagePermission(VillagePermission permission) {
		this.villageState.removePermission(permission);
	}

	public void setPermissions(Set<VillagePermission> permissions) {
		this.villageState.setPermissions(permissions);
	}

	public void setPermissions(VillagePermission... permissions) {
		Set<VillagePermission> updated = permissions == null
				? Set.of()
				: new HashSet<>(Arrays.asList(permissions));
		updated.remove(null);
		this.setPermissions(updated);
	}

	public boolean isOnline() {
		return this.profile.isOnline();
	}

	public boolean isVanished() {
		return this.profile.isVanished();
	}

	public boolean hasPermission(String permission) {
		return permission != null && this.profile.hasPermission(permission);
	}

	public int getPing() {
		return this.profile.getPing();
	}

	public void sendMessage(String message) {
		if (message != null) this.profile.sendMessage(ColorUtils.color(message));
	}

	@Override
	public String getIdentityKey() {
		return this.uuid.toString();
	}

	@Override
	public int hashCode() {
		return this.uuid.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (!(object instanceof User)) return false;
		User user = (User) object;
		return this.uuid.equals(user.uuid);
	}

	@Override
	public String toString() {
		return "User{uuid=" + this.uuid + ", name='" + this.name + "'}";
	}
}
