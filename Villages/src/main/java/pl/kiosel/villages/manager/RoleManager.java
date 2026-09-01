package pl.kiosel.villages.manager;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.config.RosaConfig;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.api.events.VillageMemberRoleChangeEvent;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Permission;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.role.VillageRole;

import java.util.*;
import java.util.stream.Collectors;

public class RoleManager {

	private static final String ROLE_PREFIX = "role:";
	private static final VillageRole OWNER_ROLE = new VillageRole(
			"owner", "&cOwner", Integer.MAX_VALUE, EnumSet.allOf(Permission.class));

	protected final AdvancedVillages plugin;
	private volatile Map<String, VillageRole> roles = Collections.emptyMap();
	private volatile List<VillageRole> orderedRoles = Collections.emptyList();
	private volatile String defaultRoleId = "member";

	public RoleManager(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.reload();
	}

	public synchronized void reload() {
		RosaConfig file = this.plugin.getVillageFile();
		Map<String, VillageRole> loaded = new LinkedHashMap<>();
		ConfigurationSection section = file.getConfigurationSection("roles");
		if (section != null) {
			for (String rawId : section.getKeys(false)) {
				String id = normalize(rawId, false);
				if (!id.matches("[a-z0-9_-]+") || id.equals("owner")) {
					this.plugin.getRosaLogger().warning("Ignoring invalid or reserved village role: " + rawId);
					continue;
				}
				String path = "roles." + rawId;
				Set<Permission> permissions = this.readPermissions(file.getStringList(path + ".permissions"), id);
				loaded.put(id, new VillageRole(id,
						file.getString(path + ".name", rawId),
						file.getInt(path + ".priority", 0), permissions));
			}
		}

		if (loaded.isEmpty()) {
			VillageRole member = fallbackMember();
			loaded.put(member.getId(), member);
			this.plugin.getRosaLogger().warning("No valid roles in village.yml; using the built-in member role");
		}

		String configuredDefault = normalize(file.getString("default-role", "member"), false);
		if (!loaded.containsKey(configuredDefault)) {
			configuredDefault = loaded.keySet().iterator().next();
			this.plugin.getRosaLogger().warning("Invalid default-role in village.yml; using " + configuredDefault);
		}

		List<VillageRole> ordered = new ArrayList<>(loaded.values());
		ordered.sort(Comparator.comparingInt(VillageRole::getPriority)
				.thenComparing(VillageRole::getId));
		this.roles = Collections.unmodifiableMap(loaded);
		this.orderedRoles = Collections.unmodifiableList(ordered);
		this.defaultRoleId = configuredDefault;

		if (this.plugin.getUserManager() != null) {
			for (User user : this.plugin.getUserManager().getUsers()) {
				user.getRoleId().flatMap(this::getRole)
						.ifPresent(role -> user.assignRole(role.getId(), role.getPermissions()));
			}
		}
	}

	public List<VillageRole> getRoles() {
		return this.orderedRoles;
	}

	public Optional<VillageRole> getRole(String id) {
		return Optional.ofNullable(id == null ? null : this.roles.get(normalize(id, false)));
	}

	public VillageRole getDefaultRole() {
		return this.roles.get(this.defaultRoleId);
	}

	public VillageRole getRole(User user) {
		if (user != null && user.isOwner()) return OWNER_ROLE;
		if (user != null) {
			Optional<VillageRole> assigned = user.getRoleId().flatMap(this::getRole);
			if (assigned.isPresent()) return assigned.get();
			if (!user.getPermissions().isEmpty()) {
				return new VillageRole("custom", "&eCustom", -1, user.getPermissions());
			}
		}
		return this.getDefaultRole();
	}

	public void assignDefaultRole(User user) {
		this.assignRole(user, this.getDefaultRole());
	}

	public void assignRole(User user, VillageRole role) {
		if (user == null || role == null || role == OWNER_ROLE || user.isOwner()) return;
		VillageRole configured = this.roles.get(role.getId());
		if (configured == null) throw new IllegalArgumentException("Unknown village role: " + role.getId());
		user.assignRole(configured.getId(), configured.getPermissions());
	}

	public boolean changeRole(Village village, Player actor, User member, VillageRole role) {
		if (village == null || actor == null || member == null || role == null
				|| !village.isMember(member) || village.isOwner(member)) return false;
		VillageRole previous = this.getRole(member);
		if (previous.getId().equals(role.getId())) return false;
		VillageMemberRoleChangeEvent event = new VillageMemberRoleChangeEvent(
				village, actor, member, previous, role);
		this.plugin.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) return false;
		this.assignRole(member, role);
		return true;
	}

	public VillageRole nextRole(User user, int direction) {
		List<VillageRole> available = this.orderedRoles;
		if (available.isEmpty()) return this.getDefaultRole();
		String currentId = user == null ? null : user.getRoleId().orElse(null);
		int current = -1;
		for (int index = 0; index < available.size(); index++) {
			if (available.get(index).getId().equals(currentId)) {
				current = index;
				break;
			}
		}
		int step = direction < 0 ? -1 : 1;
		int next = current < 0
				? (step < 0 ? available.size() - 1 : 0)
				: Math.floorMod(current + step, available.size());
		return available.get(next);
	}

	public boolean hasCommandPermission(Player player, Permission permission) {
		return permission == Permission.UNSET || this.hasPermission(player.getUniqueId(), permission);
	}

	public boolean hasPermission(User user, Permission permission) {
		if (user == null || permission == null) return false;
		if (permission == Permission.UNSET) return true;
		if (!user.hasVillage()) return false;
		if (user.isOwner()) return true;
		if (permission == Permission.OWNER) return false;
		return this.getRole(user).hasPermission(permission);
	}

	public boolean hasPermission(Player player, Permission permission) {
		return player != null && this.hasPermission(player.getUniqueId(), permission);
	}

	public boolean hasPermission(UUID uuid, Permission permission) {
		return this.plugin.getUserManager().findByUuid(uuid)
				.map(user -> this.hasPermission(user, permission))
				.orElse(false);
	}

	public void addPermission(User user, Permission permission) {
		if (user == null || permission == null) return;
		Set<Permission> permissions = EnumSet.noneOf(Permission.class);
		permissions.addAll(this.getRole(user).getPermissions());
		permissions.add(permission);
		user.setPermissions(permissions);
	}

	public void removePermission(User user, Permission permission) {
		if (user == null || permission == null) return;
		Set<Permission> permissions = EnumSet.noneOf(Permission.class);
		permissions.addAll(this.getRole(user).getPermissions());
		permissions.remove(permission);
		user.setPermissions(permissions);
	}

	public String serialize(User user) {
		if (user == null) return null;
		return user.getRoleId()
				.map(id -> ROLE_PREFIX + id)
				.orElseGet(() -> this.toString(user.getPermissions()));
	}

	public void deserialize(User user, String raw) {
		if (user == null) return;
		if (raw != null && raw.regionMatches(true, 0, ROLE_PREFIX, 0, ROLE_PREFIX.length())) {
			String id = normalize(raw.substring(ROLE_PREFIX.length()), false);
			VillageRole role = this.roles.get(id);
			if (role != null) {
				user.assignRole(role.getId(), role.getPermissions());
				return;
			}
			this.plugin.getRosaLogger().warning("Unknown saved village role '" + id + "' for " + user.getName());
			this.assignDefaultRole(user);
			return;
		}

		Set<Permission> legacy = this.fromString(raw);
		for (VillageRole role : this.orderedRoles) {
			if (role.getPermissions().equals(legacy)) {
				user.assignRole(role.getId(), role.getPermissions());
				return;
			}
		}
		user.setPermissions(legacy);
	}

	public String toString(Set<Permission> permissions) {
		if (permissions == null || permissions.isEmpty()) return null;
		return permissions.stream().map(Enum::name).sorted().collect(Collectors.joining(";"));
	}

	public Set<Permission> fromString(String raw) {
		Set<Permission> permissions = EnumSet.noneOf(Permission.class);
		if (raw == null || raw.isBlank()) return permissions;
		for (String value : raw.split(";")) {
			try {
				permissions.add(Permission.valueOf(normalize(value, true)));
			} catch (IllegalArgumentException exception) {
				this.plugin.getRosaLogger().warning("Invalid village permission: " + value);
			}
		}
		return permissions;
	}

	private Set<Permission> readPermissions(List<String> values, String roleId) {
		Set<Permission> permissions = EnumSet.noneOf(Permission.class);
		for (String value : values) {
			try {
				Permission permission = Permission.valueOf(normalize(value, true).replace('-', '_'));
				if (permission != Permission.OWNER && permission != Permission.UNSET) permissions.add(permission);
			} catch (IllegalArgumentException exception) {
				this.plugin.getRosaLogger().warning("Invalid permission '" + value + "' in role " + roleId);
			}
		}
		return permissions;
	}

	private static VillageRole fallbackMember() {
		return new VillageRole("member", "&7Member", 0, EnumSet.of(
				Permission.BANK_ADD, Permission.BANK_REMOVE, Permission.STORE,
				Permission.UPGRADE, Permission.EFFECTS_BUY, Permission.QUEST_TOGGLE));
	}

	private static String normalize(String id, boolean uppercase) {
		if (id == null) return "";
		String normalized = id.trim();
		if (uppercase) {
			return normalized.toUpperCase(Locale.ROOT);
		}
		return normalized.toLowerCase(Locale.ROOT);
	}
}
