package pl.kiosel.villages.manager;

import lombok.Getter;
import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.enums.Permission;

import java.util.*;
import java.util.stream.Collectors;

public class PermissionManager {

	private final AdvancedVillages plugin;
	@Getter	private final Set<Permission> defaultPermission;

	public PermissionManager(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.defaultPermission = new HashSet<>();
		defaultPermission.add(Permission.BANK_ADD);
		defaultPermission.add(Permission.BANK_REMOVE);
		defaultPermission.add(Permission.STORE);
		defaultPermission.add(Permission.UPGRADE);
		defaultPermission.add(Permission.EFFECTS_BUY);
	}

	public void addPermission(User user, Permission permission) {
		if (!user.getPermissions().contains(permission))
			user.addVillagePermission(permission);
	}

	public void removePermission(User user, Permission permission) {
		user.removeVillagePermission(permission);
	}

	public boolean hasPermission(User user, Permission permission) {
		return user.hasVillagePermission(permission);
	}

	public boolean hasPermission(Player player, Permission permission) {
		return hasPermission(player.getUniqueId(), permission);
	}

	public boolean hasPermission(UUID uuid, Permission permission) {
		return hasPermission(plugin.getUserManager().findByUuid(uuid).get(), permission);
	}

	public String toString(Set<Permission> permissions) {
		if (permissions == null || permissions.isEmpty()) {
			return null;
		}
		return permissions.stream()
				.map(Enum::name)
				.collect(Collectors.joining(";"));
	}

	public Set<Permission> fromString(String raw) {
		Set<Permission> permissions = new HashSet<>();
		if (raw == null || raw.isEmpty()) return permissions;

		for (String s : raw.split(";")) {
			try {
				permissions.add(Permission.valueOf(s.trim()));
			} catch (IllegalArgumentException e) {
				plugin.getLogger().warning("Nieprawidłowy PERM: " + s);
			}
		}
		return permissions;
	}
}