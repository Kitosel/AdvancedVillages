package pl.kiosel.villages.data.village;

import org.jetbrains.annotations.Nullable;
import pl.kiosel.villages.data.user.User;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

final class VillageMembers {

	private final Village village;
	private final Runnable changeListener;
	private final Set<User> members = ConcurrentHashMap.newKeySet();
	private final Set<User> membersView = Collections.unmodifiableSet(this.members);
	@Nullable private volatile User owner;

	VillageMembers(Village village, Runnable changeListener) {
		this.village = Objects.requireNonNull(village, "village");
		this.changeListener = Objects.requireNonNull(changeListener, "changeListener");
	}

	@Nullable
	User getOwner() {
		return this.owner;
	}

	boolean isOwner(@Nullable User user) {
		return user != null && Objects.equals(this.owner, user);
	}

	synchronized void setOwner(User owner) {
		Objects.requireNonNull(owner, "owner");
		boolean ownerChanged = !Objects.equals(this.owner, owner);
		this.owner = owner;
		boolean memberAdded = this.members.add(owner);
		this.attach(owner);
		if (ownerChanged || memberAdded) this.changeListener.run();
	}

	Set<User> getMembers() {
		return this.membersView;
	}

	Set<User> getOnlineMembers() {
		return this.members.stream()
				.filter(User::isOnline)
				.collect(Collectors.toUnmodifiableSet());
	}

	Set<String> getMemberNames() {
		return this.members.stream()
				.map(User::getName)
				.collect(Collectors.toUnmodifiableSet());
	}

	boolean contains(@Nullable User user) {
		return user != null && this.members.contains(user);
	}

	boolean contains(UUID uuid) {
		return uuid != null && this.members.stream().anyMatch(user -> uuid.equals(user.getUUID()));
	}

	synchronized void replace(Set<User> users) {
		Set<User> updated = users == null ? new HashSet<>() : new HashSet<>(users);
		updated.remove(null);
		if (this.owner != null) updated.add(this.owner);
		if (this.members.equals(updated)) return;

		Set<User> removed = new HashSet<>(this.members);
		removed.removeAll(updated);
		this.members.clear();
		this.members.addAll(updated);

		for (User user : removed) {
			if (user.getPresentVillage() == this.village) user.removeVillage();
		}
		for (User user : updated) this.attach(user);
		this.changeListener.run();
	}

	synchronized void add(@Nullable User user) {
		if (user == null) return;
		boolean added = this.members.add(user);
		this.attach(user);
		if (added) this.changeListener.run();
	}

	synchronized void remove(@Nullable User user) {
		if (user == null || Objects.equals(this.owner, user) || !this.members.remove(user)) return;
		if (user.getPresentVillage() == this.village) user.removeVillage();
		this.changeListener.run();
	}

	void restoreLinks() {
		for (User user : this.members) this.attach(user);
	}

	private void attach(User user) {
		Village previous = user.getPresentVillage();
		if (previous != null && previous != this.village) {
			if (previous.isOwner(user)) {
				throw new IllegalStateException("A village owner cannot be moved to another village");
			}
			previous.removeMember(user);
		}
		user.setVillage(this.village);
	}
}
