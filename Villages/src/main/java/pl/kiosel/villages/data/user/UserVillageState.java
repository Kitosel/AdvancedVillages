package pl.kiosel.villages.data.user;

import org.jetbrains.annotations.Nullable;
import pl.kiosel.villages.data.village.Permission;
import pl.kiosel.villages.data.village.Village;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

final class UserVillageState {

	private final Runnable changeListener;
	private final Set<Permission> permissions = ConcurrentHashMap.newKeySet();
	@Nullable private volatile Village village;
	@Nullable private volatile String roleId;

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
		if (this.village == null && this.roleId == null && this.permissions.isEmpty()) return;
		this.village = null;
		this.roleId = null;
		this.permissions.clear();
		this.changeListener.run();
	}

	Optional<String> getRoleId() {
		return Optional.ofNullable(this.roleId);
	}

	Set<Permission> getPermissions() {
		return Collections.unmodifiableSet(new HashSet<>(this.permissions));
	}

	void assignRole(@Nullable String roleId, @Nullable Set<Permission> permissions) {
		String normalizedRoleId = normalizeRoleId(roleId);
		Set<Permission> updated = copyPermissions(permissions);
		if (Objects.equals(this.roleId, normalizedRoleId) && this.permissions.equals(updated)) return;

		this.roleId = normalizedRoleId;
		this.permissions.clear();
		this.permissions.addAll(updated);
		this.changeListener.run();
	}

	boolean hasPermission(Permission permission, boolean owner) {
		if (permission == null) return false;
		if (owner) return true;
		return permission != Permission.OWNER && this.permissions.contains(permission);
	}

	void addPermission(@Nullable Permission permission) {
		if (permission != null && this.permissions.add(permission)) {
			this.changeListener.run();
		}
	}

	void removePermission(@Nullable Permission permission) {
		if (permission != null && this.permissions.remove(permission)) {
			this.changeListener.run();
		}

	}

	void setPermissions(@Nullable Set<Permission> permissions) {
		Set<Permission> updated = copyPermissions(permissions);
		if (this.roleId == null && this.permissions.equals(updated)) return;

		this.roleId = null;
		this.permissions.clear();
		this.permissions.addAll(updated);
		this.changeListener.run();
	}

	private static String normalizeRoleId(@Nullable String roleId) {
		return roleId == null ? null : roleId.trim().toLowerCase(Locale.ROOT);
	}

	private static Set<Permission> copyPermissions(@Nullable Set<Permission> permissions) {
		return permissions == null ? Collections.emptySet() : new HashSet<>(permissions);
	}
}
