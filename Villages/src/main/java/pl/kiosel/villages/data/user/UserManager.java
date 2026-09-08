package pl.kiosel.villages.data.user;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.kiosel.villages.AdvancedVillages;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UserManager {

	private final AdvancedVillages plugin;
	private final ConcurrentMap<UUID, User> usersByUuid = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, User> usersByName = new ConcurrentHashMap<>();

	public UserManager(AdvancedVillages plugin) {
		this.plugin = Objects.requireNonNull(plugin, "plugin");
	}

	public int countUsers() {
		return this.usersByUuid.size();
	}

	public Set<User> getUsers() {
		return new HashSet<>(this.usersByUuid.values());
	}

	public void clearUsers() {
		this.usersByUuid.clear();
		this.usersByName.clear();
	}

	public Optional<Player> getPlayer(User user) {
		return user == null ? Optional.empty() : Optional.ofNullable(Bukkit.getPlayer(user.getUUID()));
	}

	public User getOrCreate(Player player) {
		Objects.requireNonNull(player, "player");
		User user = this.findByUuid(player.getUniqueId()).orElseGet(() -> this.create(
				player.getUniqueId(),
				player.getName(),
				new BukkitUserProfile(player.getUniqueId())
		));
		user.getProfile().refresh();
		if (!user.getName().equals(player.getName())) this.updateUsername(user, player.getName());
		return user;
	}

	public Set<User> findByNames(Collection<String> names) {
		if (names == null || names.isEmpty()) return Collections.emptySet();
		Set<User> users = new HashSet<>();
		for (String name : names) this.findByName(name, true).ifPresent(users::add);
		return users;
	}

	public Optional<User> findByUuid(UUID uuid) {
		return uuid == null ? Optional.empty() : Optional.ofNullable(this.usersByUuid.get(uuid));
	}

	public Optional<User> findByName(String nickname) {
		return this.findByName(nickname, false);
	}

	public Optional<User> findByName(String nickname, boolean ignoreCase) {
		if (nickname == null || nickname.isBlank()) return Optional.empty();
		User user = this.usersByName.get(normalizeName(nickname));
		if (user == null || (!ignoreCase && !user.getName().equals(nickname))) return Optional.empty();
		return Optional.of(user);
	}

	public Optional<User> findByPlayer(@NotNull Player player) {
		Objects.requireNonNull(player, "player");
		if (player.getUniqueId().version() == 2) {
			return Optional.of(new User(
					player.getUniqueId(),
					player.getName(),
					new NPCUserProfile(),
					this.startingPoints()
			));
		}
		return this.findByUuid(player.getUniqueId());
	}

	public Optional<User> findByPlayer(OfflinePlayer player) {
		return player == null ? Optional.empty() : this.findByUuid(player.getUniqueId());
	}

	public User createFake(UUID uuid, String name) {
		return this.createFake(uuid, name, FakeUserProfile.offline());
	}

	public User createFake(UUID uuid, String name, FakeUserProfile profile) {
		return this.create(uuid, name, profile);
	}

	public User create(UUID uuid, String name, UserProfile profile) {
		Objects.requireNonNull(uuid, "uuid");
		Objects.requireNonNull(profile, "profile");
		requireValidName(name, "name");
		User user = new User(uuid, name, profile, this.startingPoints());
		this.addUser(user);
		return user;
	}

	public void addUser(User user) {
		Objects.requireNonNull(user, "user");
		User existingByUuid = this.usersByUuid.putIfAbsent(user.getUUID(), user);
		if (existingByUuid != null && existingByUuid != user) {
			throw new IllegalArgumentException("A user with UUID " + user.getUUID() + " is already loaded");
		}

		String nameKey = normalizeName(user.getName());
		User existingByName = this.usersByName.putIfAbsent(nameKey, user);
		if (existingByName != null && existingByName != user) {
			if (existingByUuid == null) this.usersByUuid.remove(user.getUUID(), user);
			throw new IllegalArgumentException("A user with name " + user.getName() + " is already loaded");
		}
	}

	public void removeUser(User user) {
		if (user == null) return;
		this.usersByUuid.remove(user.getUUID(), user);
		this.usersByName.remove(normalizeName(user.getName()), user);
	}

	public void updateUsername(User user, String newUsername) {
		Objects.requireNonNull(user, "user");
		requireValidName(newUsername, "newUsername");
		if (user.getName().equals(newUsername)) return;

		String oldKey = normalizeName(user.getName());
		String newKey = normalizeName(newUsername);
		if (!oldKey.equals(newKey)) {
			User existing = this.usersByName.putIfAbsent(newKey, user);
			if (existing != null && existing != user) {
				throw new IllegalArgumentException("A user with name " + newUsername + " is already loaded");
			}
			this.usersByName.remove(oldKey, user);
		}
		user.setName(newUsername);
	}

	public boolean playedBefore(String nickname) {
		return this.playedBefore(nickname, false);
	}

	public boolean playedBefore(String nickname, boolean ignoreCase) {
		return this.findByName(nickname, ignoreCase).isPresent();
	}

	private int startingPoints() {
		return this.plugin.getRankingManager() == null || !this.plugin.getRankingManager().isEnabled()
				? 1000
				: this.plugin.getRankingManager().getStartingPoints();
	}

	private static String normalizeName(String name) {
		return name.toLowerCase(Locale.ROOT);
	}

	private static void requireValidName(String name, String field) {
		if (name == null || name.isBlank()) throw new IllegalArgumentException(field + " can't be blank!");
		if (UserValidator.validateUsername(name) != UserValidator.NameResult.VALID) {
			throw new IllegalArgumentException(field + " is not valid!");
		}
	}
}
