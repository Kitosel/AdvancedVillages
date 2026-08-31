package pl.kiosel.villages.data.user;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import pl.kiosel.rosacore.utils.ColorUtils;
import pl.kiosel.villages.data.AbstractMutableEntity;
import pl.kiosel.villages.data.village.Permission;
import pl.kiosel.villages.data.village.Village;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class User extends AbstractMutableEntity {

    private final UUID uuid;
    private String name;

    @Getter private final UserRank rank;
	@Nullable private volatile Village village;
	@Nullable private volatile String roleId;
	private final Set<Permission> permissions = ConcurrentHashMap.newKeySet();

    @Getter
	private final UserProfile profile;

    User(UUID uuid, String name, UserProfile profile, int startingPoints) {
        this.uuid = uuid;
        this.name = name;
        this.profile = profile;

        this.rank = new UserRank(this, startingPoints);

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
        this.name = name;
		this.markChanged();
    }

    @Override
    public UnitType getType() {
        return UnitType.USER;
    }

	public Village getPresentVillage() {
		return this.village;
	}

	public Optional<Village> getVillage() {
		return Optional.ofNullable(this.village);
	}

	public Set<Permission> getPermissions() {
		return Collections.unmodifiableSet(new HashSet<>(this.permissions));
	}

	public Optional<String> getRoleId() {
		return Optional.ofNullable(this.roleId);
	}

	public void assignRole(String roleId, Set<Permission> permissions) {
		String normalized = roleId == null ? null : roleId.trim().toLowerCase(Locale.ROOT);
		Set<Permission> updated = permissions == null ? Collections.emptySet() : new HashSet<>(permissions);
		if (!java.util.Objects.equals(this.roleId, normalized) || !this.permissions.equals(updated)) {
			this.roleId = normalized;
			this.permissions.clear();
			this.permissions.addAll(updated);
			this.markChanged();
		}
	}

	public boolean isOnline() {
        return this.profile.isOnline();
    }

    public boolean isVanished() {
        return this.profile.isVanished();
    }

    public boolean hasPermission(String permission) {
        return this.profile.hasPermission(permission);
    }

    public int getPing() {
        return this.profile.getPing();
    }

    public void sendMessage(String message) {
        this.profile.sendMessage(ColorUtils.color(message));
    }

	public boolean hasVillage() {
        return this.village != null;
    }

    public void setVillage(@Nullable Village village) {
		if (this.village != village) {
			this.village = village;
			this.markChanged();
		}
    }

    public void removeVillage() {
		if (this.village != null || this.roleId != null || !this.permissions.isEmpty()) {
			this.village = null;
			this.roleId = null;
			this.permissions.clear();
			this.markChanged();
		}
    }

    public boolean isOwner() {
		Village currentVillage = this.village;
        return currentVillage != null && currentVillage.isOwner(this);
    }

	public boolean hasVillagePermission(Permission permission) {
		if (isOwner()) return true;
		if (permission.equals(Permission.OWNER)) return false;
		return permissions.contains(permission);
	}

	public void addVillagePermission(Permission permission) {
		if (permission != null && permissions.add(permission)) {
			this.markChanged();
		}
	}

	public void removeVillagePermission(Permission permission) {
		if (permission != null && permissions.remove(permission)) {
			this.markChanged();
		}
	}

	public void setPermissions(Set<Permission> permissions) {
		Set<Permission> updated = ConcurrentHashMap.newKeySet();
		if (permissions != null) {
			updated.addAll(permissions);
		}
		if (this.roleId != null || !this.permissions.equals(updated)) {
			this.roleId = null;
			this.permissions.clear();
			this.permissions.addAll(updated);
			this.markChanged();
		}
	}

	public void setPermissions(Permission... permissions) {
		Set<Permission> updated = ConcurrentHashMap.newKeySet();
		if (permissions != null) {
			for (Permission permission : permissions) {
				if (permission != null) {
					updated.add(permission);
				}
			}
		}
		this.setPermissions(updated);
	}

	@Override
    public int hashCode() {
        return this.uuid.hashCode();
    }

	@Override
	public String getIdentityKey() {
		return this.uuid.toString();
	}

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }

        User user = (User) obj;
        return this.uuid.equals(user.uuid);
    }

    @Override
    public String toString() {
        return "User{uuid=" + this.uuid + ", name='" + this.name + "'}";
    }

}
