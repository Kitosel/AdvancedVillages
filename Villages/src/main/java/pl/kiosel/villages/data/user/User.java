package pl.kiosel.villages.data.user;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import panda.std.Option;
import pl.kiosel.core.utils.ColorUtils;
import pl.kiosel.villages.data.AbstractMutableEntity;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.Permission;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class User extends AbstractMutableEntity {

    private final UUID uuid;
    private String name;

    @Getter private final UserCache cache;
    @Getter private final UserRank rank;
	@Getter private Option<Village> village = Option.none();
	@Getter private Set<Permission> permissions = ConcurrentHashMap.newKeySet();

    @Getter
	private final UserProfile profile;

    User(UUID uuid, String name, UserProfile profile) {
        this.uuid = uuid;
        this.name = name;
        this.profile = profile;

        this.cache = new UserCache(this);
        this.rank = new UserRank(this, 1000);

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
		if (!village.isPresent())
			return null;
		return village.get();
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
        return this.village.isPresent();
    }

    public void setVillage(@Nullable Village village) {
        this.village = Option.of(village);
        this.markChanged();
    }

    public void removeVillage() {
        this.village = Option.none();
		this.permissions.clear();
        this.markChanged();
    }

    public boolean isOwner() {
        return this.village
                .map(village -> village.isOwner(this))
                .orElseGet(false);
    }

	public boolean hasVillagePermission(Permission permission) {
		if (isOwner() || permission.equals(Permission.OWNER)) return true;
		return permissions.contains(permission);
	}

	public void addVillagePermission(Permission permission) {
		permissions.add(permission);
		this.markChanged();
	}

	public void removeVillagePermission(Permission permission) {
		permissions.remove(permission);
		this.markChanged();
	}

	public void setPermissions(Set<Permission> permissions) {
		this.permissions.clear();
		if (permissions != null) {
			this.permissions.addAll(permissions);
		}
		this.markChanged();
	}

	public void setPermissions(Permission... permissions) {
		this.permissions.clear();
		if (permissions != null) {
			for (Permission permission : permissions) {
				if (permission != null) {
					this.permissions.add(permission);
				}
			}
		}
		this.markChanged();
	}

	@Override
    public int hashCode() {
        return this.uuid.hashCode();
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
