package pl.kiosel.villages.data.user;

import org.jetbrains.annotations.Nullable;
import pl.kiosel.villages.data.village.Village;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

final class UserVillageState {

	private final Runnable changeListener;
	private final Set<VillagePermission> permissions = ConcurrentHashMap.newKeySet();
	@Nullable private volatile Village village;
	@Nullable private volatile String roleId;
	@Nullable private volatile VillageSpecialization specialization;
	private volatile long specializationChangedAt;

	UserVillageState(Runnable changeListener) {
		this.changeListener = Objects.requireNonNull(changeListener, "changeListener");
	}

	@Nullable
	Village getPresentVillage() {
		return this.village;
	}

	Optional<Village> getVillage() {
		return Optional.ofNullable(this.village);
	}

	boolean hasVillage() {
		return this.village != null;
	}

	void setVillage(@Nullable Village village) {
		if (this.village == village) return;
		this.village = village;
		this.changeListener.run();
	}

	void clear() {
		if (this.village == null && this.roleId == null && this.specialization == null
				&& this.permissions.isEmpty()) return;
		this.village = null;
		this.roleId = null;
		this.specialization = null;
		this.specializationChangedAt = 0L;
		this.permissions.clear();
		this.changeListener.run();
	}

	Optional<String> getRoleId() {
		return Optional.ofNullable(this.roleId);
	}

	Optional<VillageSpecialization> getSpecialization() {
		return Optional.ofNullable(this.specialization);
	}

	long getSpecializationChangedAt() {
		return this.specializationChangedAt;
	}

	Set<VillagePermission> getPermissions() {
		return Collections.unmodifiableSet(new HashSet<>(this.permissions));
	}

	void assignRole(@Nullable String roleId, @Nullable Set<VillagePermission> permissions) {
		String normalizedRoleId = normalizeRoleId(roleId);
		Set<VillagePermission> updated = copyPermissions(permissions);
		if (Objects.equals(this.roleId, normalizedRoleId) && this.permissions.equals(updated)) return;

		this.roleId = normalizedRoleId;
		this.permissions.clear();
		this.permissions.addAll(updated);
		this.changeListener.run();
	}

	void setSpecialization(@Nullable VillageSpecialization specialization, long changedAt) {
		long normalizedChangedAt = specialization == null ? 0L : Math.max(0L, changedAt);
		if (this.specialization == specialization && this.specializationChangedAt == normalizedChangedAt) return;
		this.specialization = specialization;
		this.specializationChangedAt = normalizedChangedAt;
		this.changeListener.run();
	}

	boolean hasPermission(VillagePermission permission, boolean owner) {
		if (permission == null) return false;
		if (owner) return true;
		return permission != VillagePermission.OWNER && this.permissions.contains(permission);
	}

	void addPermission(@Nullable VillagePermission permission) {
		if (permission != null && this.permissions.add(permission)) {
			this.changeListener.run();
		}
	}

	void removePermission(@Nullable VillagePermission permission) {
		if (permission != null && this.permissions.remove(permission)) {
			this.changeListener.run();
		}

	}

	void setPermissions(@Nullable Set<VillagePermission> permissions) {
		Set<VillagePermission> updated = copyPermissions(permissions);
		if (this.roleId == null && this.permissions.equals(updated)) return;

		this.roleId = null;
		this.permissions.clear();
		this.permissions.addAll(updated);
		this.changeListener.run();
	}

	private static String normalizeRoleId(@Nullable String roleId) {
		return roleId == null ? null : roleId.trim().toLowerCase(Locale.ROOT);
	}

	private static Set<VillagePermission> copyPermissions(@Nullable Set<VillagePermission> permissions) {
		return permissions == null ? Collections.emptySet() : new HashSet<>(permissions);
	}
}
