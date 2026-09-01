package pl.kiosel.villages.data.village;

import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VillageManager {

	private final ConcurrentMap<UUID, Village> villagesByUuid = new ConcurrentHashMap<>();
	private final Collection<Village> villagesView = Collections.unmodifiableCollection(this.villagesByUuid.values());

	public int countVillage() {
		return this.villagesByUuid.size();
	}

	public Set<Village> getVillages() {
		return new HashSet<>(this.villagesByUuid.values());
	}

	public Collection<Village> getVillagesView() {
		return this.villagesView;
	}

	public void clearVillage() {
		this.villagesByUuid.clear();
	}

	public Set<Village> findByNames(Collection<String> names) {
		return this.findMany(names, this::findByName);
	}

	public Set<Village> findByTags(Collection<String> tags) {
		return this.findMany(tags, this::findByTag);
	}

	public Optional<Village> findByUuid(UUID uuid) {
		return uuid == null ? Optional.empty() : Optional.ofNullable(this.villagesByUuid.get(uuid));
	}

	public Optional<Village> findByOwner(String name) {
		return this.findByOwner(name, false);
	}

	public Optional<Village> findByOwner(String name, boolean ignoreCase) {
		return this.findByValue(name, ignoreCase, village -> {
			User owner = village.getOwner();
			return owner == null ? null : owner.getName();
		});
	}

	public Optional<Village> findByName(String name) {
		return this.findByName(name, false);
	}

	public Optional<Village> findByName(String name, boolean ignoreCase) {
		return this.findByValue(name, ignoreCase, Village::getName);
	}

	public Optional<Village> findByTag(String tag) {
		return this.findByTag(tag, false);
	}

	public Optional<Village> findByTag(String tag, boolean ignoreCase) {
		return this.findByValue(tag, ignoreCase, Village::getTag);
	}

	public void addVillage(Village village) {
		Objects.requireNonNull(village, "village");
		Village existing = this.villagesByUuid.putIfAbsent(village.getUUID(), village);
		if (existing != null && existing != village) {
			throw new IllegalArgumentException("A village with UUID " + village.getUUID() + " is already loaded");
		}
	}

	public void deleteVillage(Village village) {
		if (village != null) this.villagesByUuid.remove(village.getUUID(), village);
	}

	public void deleteVillage(AdvancedVillages plugin, Village village) {
		if (plugin == null || village == null) return;
		village.getMembers().forEach(User::removeVillage);
		if (plugin.getQuestManager() != null) plugin.getQuestManager().delete(village);
		if (plugin.getDiplomacyManager() != null) plugin.getDiplomacyManager().removeVillage(village);
		if (plugin.getLogManager() != null) plugin.getLogManager().delete(village);
		if (plugin.getDevelopmentManager() != null) plugin.getDevelopmentManager().delete(village);
		if (plugin.getUpkeepManager() != null) plugin.getUpkeepManager().delete(village);
		this.deleteVillage(village);
	}

	public boolean nameExists(String name) {
		return this.findByName(name, true).isPresent();
	}

	public boolean tagExists(String tag) {
		return this.findByTag(tag, true).isPresent();
	}

	public static Set<String> getTags(Collection<Village> villages) {
		if (villages == null || villages.isEmpty()) return Collections.emptySet();
		return villages.stream()
				.filter(Objects::nonNull)
				.map(Village::getTag)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	public List<Village> getVillageAsList() {
		return new ArrayList<>(this.villagesByUuid.values());
	}

	public List<String> getVillageOwners() {
		return this.villagesByUuid.values().stream()
				.map(Village::getOwner)
				.filter(Objects::nonNull)
				.map(User::getName)
				.collect(Collectors.toList());
	}

	public List<String> getVillageNamesAsList() {
		return this.values(Village::getName);
	}

	public List<String> getVillageTagsAsList() {
		return this.values(Village::getTag);
	}

	public void onDisable() {
		this.clearVillage();
	}

	private Set<Village> findMany(Collection<String> values, Function<String, Optional<Village>> finder) {
		if (values == null || values.isEmpty()) return Collections.emptySet();
		Set<Village> villages = new HashSet<>();
		for (String value : values) {
			if (value != null) finder.apply(value).ifPresent(villages::add);
		}
		return villages;
	}

	private Optional<Village> findByValue(String value, boolean ignoreCase, Function<Village, String> extractor) {
		if (value == null || value.isBlank()) return Optional.empty();
		return this.villagesByUuid.values().stream()
				.filter(Objects::nonNull)
				.filter(village -> matches(extractor.apply(village), value, ignoreCase))
				.findFirst();
	}

	private List<String> values(Function<Village, String> extractor) {
		return this.villagesByUuid.values().stream()
				.filter(Objects::nonNull)
				.map(extractor)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	private static boolean matches(String candidate, String expected, boolean ignoreCase) {
		if (candidate == null) return false;
		return ignoreCase ? candidate.equalsIgnoreCase(expected) : candidate.equals(expected);
	}
}
