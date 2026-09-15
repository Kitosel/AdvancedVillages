package pl.kiosel.villages.manager;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import pl.kiosel.rosacore.config.RosaConfig;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.api.events.VillageMemberRoleChangeEvent;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.VillagePermission;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.VillageRole;
import pl.kiosel.villages.storage.VillageRoleStorage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class RoleManager {

	private static final String ROLE_PREFIX = "role:";
	private static final int MAX_ROLES = 7;
	private static final VillageRole OWNER_ROLE = new VillageRole(
			"owner", "&cOwner", Integer.MAX_VALUE, Collections.singleton(VillagePermission.OWNER));

	protected final AdvancedVillages plugin;
	private Map<String, VillageRole> roles = Collections.emptyMap();
	private List<VillageRole> orderedRoles = Collections.emptyList();
	private String defaultRoleId = "member";
	private final RosaConfig config;
	private final VillageRoleStorage storage;
	private final Map<UUID, Map<String, Set<VillagePermission>>> permissionOverrides = new ConcurrentHashMap<>();

	public RoleManager(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.config = plugin.getVillageFile();
		this.storage = new VillageRoleStorage(plugin);
		this.reload();
	}

	public synchronized void reload() {
		Map<String, VillageRole> loaded = new LinkedHashMap<>();
		ConfigurationSection section = config.getConfigurationSection("roles");
		if (section != null) {
			for (String rawId : section.getKeys(false)) {
				if (loaded.size() >= MAX_ROLES) {
					this.plugin.getRosaLogger().warning("Ignoring village role '" + rawId
							+ "'; a maximum of " + MAX_ROLES + " roles is supported");
					continue;
				}
				String id = normalize(rawId, false);
				if (!id.matches("[a-z0-9_-]+") || id.equals("owner")) {
					this.plugin.getRosaLogger().warning("Ignoring invalid or reserved village role: " + rawId);
					continue;
				}
				String path = "roles." + rawId;
				Set<VillagePermission> permissions = this.readPermissions(config.getStringList(path + ".permissions"), id);
				loaded.put(id, new VillageRole(id,
						config.getString(path + ".name", rawId),
						config.getInt(path + ".priority", 0), permissions));
			}
		}

		if (loaded.isEmpty()) {
			VillageRole member = fallbackMember();
			loaded.put(member.getId(), member);
			this.plugin.getRosaLogger().warning("No valid roles in village.yml; using the built-in member role");
		}

		String configuredDefault = normalize(config.getString("default-role", "member"), false);
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

	public void load() {
		this.permissionOverrides.clear();
		for (VillageRoleStorage.StoredRole stored : this.storage.load()) {
			String roleId = normalize(stored.getRoleId(), false);
			if (roleId.isEmpty()) {
				this.plugin.getRosaLogger().warning("Ignoring blank saved role for village " + stored.getVillageId());
				continue;
			}
			Set<VillagePermission> permissions = this.fromString(stored.getPermissions());
			permissions.remove(VillagePermission.OWNER);
			permissions.remove(VillagePermission.UNSET);
			this.permissionOverrides.computeIfAbsent(stored.getVillageId(), ignored -> new ConcurrentHashMap<>())
					.put(roleId, immutablePermissions(permissions));
		}
	}

	public boolean canChangePermission() {
		return config.getBoolean("owner-can-change-permissions", true);
	}

	public Set<VillagePermission> getPermissions(Village village, VillageRole role) {
		if (role == null) return Collections.emptySet();
		if (village == null || role == OWNER_ROLE) return role.getPermissions();
		Map<String, Set<VillagePermission>> villagePermissions = this.permissionOverrides.get(village.getUUID());
		if (villagePermissions == null) return role.getPermissions();
		return villagePermissions.getOrDefault(role.getId(), role.getPermissions());
	}

	public Set<VillagePermission> getPermissions(Village village, User user) {
		return this.getPermissions(village, this.getRole(user));
	}

	public Optional<Boolean> togglePermission(Village village, Player actor, VillageRole role, VillagePermission permission) {
		if (!this.canEditPermissions(village, actor) || role == null || permission == null
				|| permission == VillagePermission.OWNER || permission == VillagePermission.UNSET) {
			return Optional.empty();
		}

		VillageRole configured = this.roles.get(role.getId());
		if (configured == null) return Optional.empty();

		Set<VillagePermission> updated = EnumSet.noneOf(VillagePermission.class);
		updated.addAll(this.getPermissions(village, configured));
		boolean enabled;
		if (updated.remove(permission)) {
			enabled = false;
		} else {
			updated.add(permission);
			enabled = true;
		}

		try {
			this.storage.save(village.getUUID(), configured.getId(), this.toString(updated));
			this.permissionOverrides.computeIfAbsent(village.getUUID(), ignored -> new ConcurrentHashMap<>())
					.put(configured.getId(), immutablePermissions(updated));
			return Optional.of(enabled);
		} catch (RuntimeException exception) {
			this.plugin.getRosaLogger().log(Level.SEVERE,
					"Could not save permissions for role " + configured.getId()
							+ " in village " + village.getUUID(), exception);
			return Optional.empty();
		}
	}

	public boolean resetPermissions(Village village, Player actor, VillageRole role) {
		if (!this.canEditPermissions(village, actor) || role == null) {
			return false;
		}

		VillageRole configured = this.roles.get(role.getId());
		if (configured == null) return false;

		try {
			UUID villageId = village.getUUID();
			this.storage.delete(villageId, configured.getId());
			Map<String, Set<VillagePermission>> overrides = this.permissionOverrides.get(villageId);
			if (overrides != null) {
				overrides.remove(configured.getId());
				if (overrides.isEmpty()) {
					this.permissionOverrides.remove(villageId, overrides);
				}
			}
			return true;
		} catch (RuntimeException exception) {
			this.plugin.getRosaLogger().log(Level.SEVERE,
					"Could not reset permissions for role " + configured.getId()
							+ " in village " + village.getUUID(), exception);
			return false;
		}
	}

	public boolean canEditPermissions(Village village, Player actor) {
		if (!this.canChangePermission() || village == null || actor == null) return false;
		return this.plugin.getUserManager().findByUuid(actor.getUniqueId())
				.map(village::isOwner)
				.orElse(false);
	}

	public void delete(Village village) {
		if (village == null) return;
		this.permissionOverrides.remove(village.getUUID());
		this.storage.delete(village.getUUID());
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

	public boolean hasCommandPermission(Player player, VillagePermission permission) {
		return permission == VillagePermission.UNSET || this.hasPermission(player.getUniqueId(), permission);
	}

	public boolean hasPermission(User user, VillagePermission permission) {
		if (user == null || permission == null) return false;
		if (permission == VillagePermission.UNSET) return true;
		Village village = user.getPresentVillage();
		if (village == null) return false;
		if (user.isOwner()) return true;
		if (permission == VillagePermission.OWNER) return false;
		return this.getPermissions(village, user).contains(permission);
	}

	public boolean hasPermission(Player player, VillagePermission permission) {
		return player != null && this.hasPermission(player.getUniqueId(), permission);
	}

	public boolean hasPermission(UUID uuid, VillagePermission permission) {
		return this.plugin.getUserManager().findByUuid(uuid)
				.map(user -> this.hasPermission(user, permission))
				.orElse(false);
	}

	public void addPermission(User user, VillagePermission permission) {
		if (user == null || permission == null) return;
		Set<VillagePermission> permissions = EnumSet.noneOf(VillagePermission.class);
		permissions.addAll(this.getRole(user).getPermissions());
		permissions.add(permission);
		user.setPermissions(permissions);
	}

	public void removePermission(User user, VillagePermission permission) {
		if (user == null || permission == null) return;
		Set<VillagePermission> permissions = EnumSet.noneOf(VillagePermission.class);
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

		Set<VillagePermission> legacy = this.fromString(raw);
		for (VillageRole role : this.orderedRoles) {
			if (role.getPermissions().equals(legacy)) {
				user.assignRole(role.getId(), role.getPermissions());
				return;
			}
		}
		user.setPermissions(legacy);
	}

	public String toString(Set<VillagePermission> permissions) {
		if (permissions == null || permissions.isEmpty()) return null;
		return permissions.stream().map(Enum::name).sorted().collect(Collectors.joining(";"));
	}

	public Set<VillagePermission> fromString(String raw) {
		Set<VillagePermission> permissions = EnumSet.noneOf(VillagePermission.class);
		if (raw == null || raw.isBlank()) return permissions;
		for (String value : raw.split(";")) {
			try {
				permissions.add(VillagePermission.valueOf(normalize(value, true)));
			} catch (IllegalArgumentException exception) {
				this.plugin.getRosaLogger().warning("Invalid village permission: " + value);
			}
		}
		return permissions;
	}

	private Set<VillagePermission> readPermissions(List<String> values, String roleId) {
		Set<VillagePermission> permissions = EnumSet.noneOf(VillagePermission.class);
		for (String value : values) {
			try {
				VillagePermission permission = VillagePermission.valueOf(normalize(value, true).replace('-', '_'));
				if (permission != VillagePermission.OWNER && permission != VillagePermission.UNSET) permissions.add(permission);
			} catch (IllegalArgumentException exception) {
				this.plugin.getRosaLogger().warning("Invalid permission '" + value + "' in role " + roleId);
			}
		}
		return permissions;
	}

	private static VillageRole fallbackMember() {
		return new VillageRole("member", "&7Member", 0, EnumSet.of(
				VillagePermission.BANK_ADD, VillagePermission.BANK_REMOVE, VillagePermission.STORE,
				VillagePermission.UPGRADE, VillagePermission.EFFECTS_BUY, VillagePermission.QUEST_TOGGLE));
	}

	private static Set<VillagePermission> immutablePermissions(Set<VillagePermission> permissions) {
		if (permissions == null || permissions.isEmpty()) return Collections.emptySet();
		return Collections.unmodifiableSet(EnumSet.copyOf(permissions));
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
