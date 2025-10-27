package pl.kiosel.villages.village;

import lombok.Getter;
import org.bukkit.entity.Player;
import pl.kiosel.villages.Wioski;
import pl.kiosel.villages.enums.Permission;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PermissionManager {

	private final Wioski plugin;
	@Getter	private final List<Permission> defaultPermission;

	public PermissionManager(Wioski plugin) {
		this.plugin = plugin;
		this.defaultPermission = new ArrayList<>();
		defaultPermission.add(Permission.BANK_ADD);
		defaultPermission.add(Permission.BANK_REMOVE);
		defaultPermission.add(Permission.STORE);
		defaultPermission.add(Permission.UPGRADE);
		defaultPermission.add(Permission.EFFECTS_BUY);
	}

	public void addPermission(VillageMember villageMember, Permission permission) {
		if (!villageMember.getPermissions().contains(permission))
			villageMember.getPermissions().add(permission);
	}

	public void removePermission(VillageMember villageMember, Permission permission) {
		villageMember.getPermissions().remove(permission);
	}

	public boolean hasPermission(Player player, Permission permission) {
		return hasPermission(player.getUniqueId(), permission);
	}

	public boolean hasPermission(UUID uuid, Permission permission) {
		return hasPermission(plugin.getPlayerDataManager().getVillageMember(uuid), permission);
	}

	public boolean hasPermission(VillageMember member, Permission permission) {
		if (member == null) return false;
		if (member.getVillage().isOwner(member.getUuid())) return true;
		return member.getPermissions().contains(permission);
	}

	public String toString(List<Permission> permissions) {
		if (permissions == null || permissions.isEmpty()) {
			return "";
		}
		return permissions.stream()
				.map(Enum::name)
				.collect(Collectors.joining(";"));
	}

	public List<Permission> fromString(String raw) {
		List<Permission> permissions = new ArrayList<>();
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