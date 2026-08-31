package pl.kiosel.villages.data.village.role;

import lombok.Getter;
import pl.kiosel.villages.data.village.Permission;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

@Getter
public final class VillageRole {

	private final String id;
	private final String name;
	private final int priority;
	private final Set<Permission> permissions;

	public VillageRole(String id, String name, int priority, Set<Permission> permissions) {
		this.id = Objects.requireNonNull(id, "id");
		this.name = Objects.requireNonNull(name, "name");
		this.priority = priority;
		EnumSet<Permission> copied = permissions == null || permissions.isEmpty()
				? EnumSet.noneOf(Permission.class)
				: EnumSet.copyOf(permissions);
		copied.remove(Permission.UNSET);
		this.permissions = Collections.unmodifiableSet(copied);
	}

	public boolean hasPermission(Permission permission) {
		return permission == Permission.UNSET || this.permissions.contains(permission);
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof VillageRole role && this.id.equals(role.id);
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}
}
