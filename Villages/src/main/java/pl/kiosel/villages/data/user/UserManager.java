package pl.kiosel.villages.data.user;

import org.bukkit.OfflinePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.kiosel.villages.AdvancedVillages;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {

	private final AdvancedVillages plugin;
    private final Map<UUID, User> usersByUuid = new ConcurrentHashMap<>();
    private final Map<String, User> usersByName = new ConcurrentHashMap<>();

	public UserManager(AdvancedVillages plugin) {
		this.plugin = plugin;
	}

	public Optional<Player> getPlayer(User user) {
		if (user == null) return Optional.empty();
		return Optional.ofNullable(Bukkit.getPlayer(user.getUUID()));
	}

	public User getOrCreate(Player player) {
		Objects.requireNonNull(player, "player can't be null!");

		User user = this.findByUuid(player.getUniqueId())
				.map(foundUser -> {
					foundUser.getProfile().refresh();
					return foundUser;
				})
				.orElseGet(() -> {
					UserProfile profile = new BukkitUserProfile(player.getUniqueId());
					return this.create(player.getUniqueId(), player.getName(), profile);
				});

		if (!user.getName().equals(player.getName())) {
			this.updateUsername(user, player.getName());
		}
		return user;
	}

    public int countUsers() {
        return this.usersByUuid.size();
    }

    /**
     * Gets the copied set of users.
     *
     * @return set of users
     */
    public Set<User> getUsers() {
        return new HashSet<>(this.usersByUuid.values());
    }

    /**
     * Deletes all loaded users data
     */
    public void clearUsers() {
        this.usersByUuid.clear();
        this.usersByName.clear();
    }

    /**
     * Gets the set of users from collection of strings (names).
     *
     * @param names collection of names
     * @return set of users
     */
    public Set<User> findByNames(Collection<String> names) {
		if (names == null || names.isEmpty()) return Collections.emptySet();
		Set<User> users = new HashSet<>();
		for (String name : names) {
			this.findByName(name, true).ifPresent(users::add);
		}
		return users;
    }

    /**
     * Gets the user.
     *
     * @param uuid the universally unique identifier of user
     * @return the user
     */
    public Optional<User> findByUuid(UUID uuid) {
        return Optional.ofNullable(uuid == null ? null : this.usersByUuid.get(uuid));
    }

    /**
     * Gets the user.
     *
     * @param nickname the name of user
     * @return the user
     */
    public Optional<User> findByName(String nickname) {
        return this.findByName(nickname, false);
    }

    /**
     * Gets the user.
     *
     * @param nickname   the name of user
     * @param ignoreCase ignore the case of the nickname
     * @return the user
     */
    public Optional<User> findByName(String nickname, boolean ignoreCase) {
		if (nickname == null || nickname.isBlank()) return Optional.empty();
		User foundUser = this.usersByName.get(normalizeName(nickname));
		if (foundUser == null || (!ignoreCase && !foundUser.getName().equals(nickname))) {
			return Optional.empty();
		}
        return Optional.of(foundUser);
    }

    /**
     * Gets the user.
     *
     * @return the user
     */
    public Optional<User> findByPlayer(@NotNull Player player) {
        if (player.getUniqueId().version() == 2) {
            return Optional.of(new User(player.getUniqueId(), player.getName(), new NPCUserProfile(), this.startingPoints()));
        }

        return this.findByUuid(player.getUniqueId());
    }

    /**
     * Gets the user.
     *
     * @return the user
     */
    public Optional<User> findByPlayer(OfflinePlayer offlinePlayer) {
		if (offlinePlayer == null) return Optional.empty();
        return this.findByUuid(offlinePlayer.getUniqueId());
    }

    public User createFake(UUID uuid, String name) {
        return this.create(uuid, name, FakeUserProfile.offline());
    }

    public User createFake(UUID uuid, String name, FakeUserProfile profile) {
        return this.create(uuid, name, profile);
    }

    /**
     * Create the user and add it to storage. If you think you should use this method you probably shouldn't - instead use {@link UserManager#findByUuid(UUID)}, {@link UserManager#findByName(String)} etc.
     *
     * @param uuid        the universally unique identifier which will be assigned to user
     * @param name        the nickname which will be assigned to User
     * @param userProfile the user profile which will be assigned to User
     * @return the user
     */
    public User create(UUID uuid, String name, UserProfile userProfile) {
		Objects.requireNonNull(uuid, "uuid can't be null!");
		Objects.requireNonNull(name, "name can't be null!");
		Objects.requireNonNull(userProfile, "userProfile can't be null!");
		requireValidName(name, "name");

        User user = new User(uuid, name, userProfile, this.startingPoints());
        this.addUser(user);

        return user;
    }

    /**
     * Add user to storage. If you think you should use this method you probably shouldn't.
     *
     * @param user user to add
     */
    public void addUser(User user) {
		Objects.requireNonNull(user, "user can't be null!");

		User existing = this.usersByUuid.putIfAbsent(user.getUUID(), user);
		if (existing != null && existing != user) {
			throw new IllegalArgumentException("A user with UUID " + user.getUUID() + " is already loaded");
		}
		String nameKey = normalizeName(user.getName());
		User nameOwner = this.usersByName.putIfAbsent(nameKey, user);
		if (nameOwner != null && nameOwner != user) {
			if (existing == null) this.usersByUuid.remove(user.getUUID(), user);
			throw new IllegalArgumentException("A user with name " + user.getName() + " is already loaded");
		}
    }

    /**
     * Remove user from storage. If you think you should use this method you probably shouldn't.
     *
     * @param user user to remove
     */
    public void removeUser(User user) {
		Objects.requireNonNull(user, "user can't be null!");

        this.usersByUuid.remove(user.getUUID());
        this.usersByName.remove(normalizeName(user.getName()), user);
    }

    /**
     * Update username for user.
     *
     * @param user        the user for which the nickname will be changed
     * @param newUsername the new nickname for user
     */
    public void updateUsername(User user, String newUsername) {
		Objects.requireNonNull(user, "user can't be null!");
		requireValidName(newUsername, "newUsername");

		String oldKey = normalizeName(user.getName());
		String newKey = normalizeName(newUsername);
		if (!oldKey.equals(newKey)) {
			User nameOwner = this.usersByName.putIfAbsent(newKey, user);
			if (nameOwner != null && nameOwner != user) {
				throw new IllegalArgumentException("A user with name " + newUsername + " is already loaded");
			}
			this.usersByName.remove(oldKey, user);
		}
		user.setName(newUsername);
    }

    /**
     * Checks if user with given nickname have ever played on a server.
     *
     * @param nickname the nickname of user to check if ever played on
     * @return if user with given name have ever played on a server
     */
    public boolean playedBefore(String nickname) {
        return this.playedBefore(nickname, false);
    }

    /**
     * Checks if user with given nickname have ever played on a server.
     *
     * @param nickname   the nickname of user to check if ever played on
     * @param ignoreCase ignore the case of the nickname
     * @return if user with given name have ever played on a server
     */
    public boolean playedBefore(String nickname, boolean ignoreCase) {
        return this.findByName(nickname, ignoreCase).isPresent();
    }

	private int startingPoints() {
		return this.plugin.getRankingManager() == null
				? 1000
				: this.plugin.getRankingManager().getStartingPoints();
	}

	private static String normalizeName(String name) {
		return name.toLowerCase(Locale.ROOT);
	}

	private static void requireValidName(String name, String field) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException(field + " can't be blank!");
		}
		if (UserValidator.validateUsername(name) != UserValidator.NameResult.VALID) {
			throw new IllegalArgumentException(field + " is not valid!");
		}
	}

}
