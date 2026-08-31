package pl.kiosel.villages.data.village;

import lombok.Getter;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class VillageManager {

	private final Map<UUID, Village> villageMap = new ConcurrentHashMap<>();

	@Getter
	private final Collection<Village> villagesView = Collections.unmodifiableCollection(this.villageMap.values());

    public int countVillage() {
        return this.villageMap.size();
    }

    public Set<Village> getVillages() {
        return new HashSet<>(this.villageMap.values());
    }

    public void clearVillage() {
        this.villageMap.clear();
    }

    public Set<Village> findByNames(Collection<String> names) {
		if (names == null || names.isEmpty()) return Collections.emptySet();
		Set<Village> villages = new HashSet<>();
		for (String name : names) {
			this.findByName(name, true).ifPresent(villages::add);
		}
		return villages;
    }

    public Set<Village> findByTags(Collection<String> tags) {
		if (tags == null || tags.isEmpty()) return Collections.emptySet();
		Set<Village> villages = new HashSet<>();
		for (String tag : tags) {
			this.findByTag(tag, true).ifPresent(villages::add);
		}
		return villages;
    }

    public Optional<Village> findByUuid(UUID uuid) {
        return Optional.ofNullable(uuid == null ? null : this.villageMap.get(uuid));
    }

	public Optional<Village> findByOwner(String name, boolean ignoreCase) {
		if (name == null || name.isBlank()) return Optional.empty();
		return this.villageMap.values().stream()
				.filter(Objects::nonNull)
				.filter(village -> village.getOwner() != null && village.getOwner().getName() != null)
				.filter(village -> ignoreCase
						? village.getOwner().getName().equalsIgnoreCase(name)
						: village.getOwner().getName().equals(name))
				.findFirst();
	}

	public Optional<Village> findByOwner(String name) {
		return this.findByOwner(name, false);
	}

    public Optional<Village> findByName(String name, boolean ignoreCase) {
		if (name == null || name.isBlank()) return Optional.empty();
		return this.villageMap.values().stream()
				.filter(Objects::nonNull)
				.filter(village -> ignoreCase
						? village.getName().equalsIgnoreCase(name)
						: village.getName().equals(name))
				.findFirst();
    }

    public Optional<Village> findByName(String name) {
        return this.findByName(name, false);
    }

    public Optional<Village> findByTag(String tag, boolean ignoreCase) {
		if (tag == null || tag.isBlank()) return Optional.empty();
		return this.villageMap.values().stream()
				.filter(Objects::nonNull)
				.filter(village -> village.getTag() != null)
				.filter(village -> ignoreCase
						? village.getTag().equalsIgnoreCase(tag)
						: village.getTag().equals(tag))
				.findFirst();
    }

    public Optional<Village> findByTag(String tag) {
        return this.findByTag(tag, false);
    }

    public void addVillage(Village village) {
		Objects.requireNonNull(village, "village can't be null!");
		Village existing = this.villageMap.putIfAbsent(village.getUUID(), village);
		if (existing != null && existing != village) {
			throw new IllegalArgumentException("A village with UUID " + village.getUUID() + " is already loaded");
		}
	}

    public void deleteVillage(Village village) {
		Objects.requireNonNull(village, "village can't be null!");
        this.villageMap.remove(village.getUUID());
    }

	public void deleteVillage(AdvancedVillages plugin, Village village) {
        if (village == null) {
            return;
        }

		village.getMembers().forEach(User::removeVillage);
		if (plugin.getQuestManager() != null) {
			plugin.getQuestManager().delete(village);
		}
		if (plugin.getDiplomacyManager() != null) {
			plugin.getDiplomacyManager().removeVillage(village);
		}
		if (plugin.getLogManager() != null) {
			plugin.getLogManager().delete(village);
		}
		if (plugin.getDevelopmentManager() != null) {
			plugin.getDevelopmentManager().delete(village);
		}
		if (plugin.getUpkeepManager() != null) {
			plugin.getUpkeepManager().delete(village);
		}
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
		return new ArrayList<>(villageMap.values());
	}

	public List<String> getVillageOwners() {
		List<String> names = new ArrayList<>();
		for (Village village : getVillageAsList()) {
			if (village != null && village.getOwner() != null) {
				names.add(village.getOwner().getName());
			}
		}
		return names;
	}

	public List<String> getVillageNamesAsList() {
		return villageMap.values().stream()
				.filter(Objects::nonNull)
				.map(Village::getName)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	public List<String> getVillageTagsAsList() {
		return villageMap.values().stream()
				.filter(Objects::nonNull)
				.map(Village::getTag)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	public void onDisable() {
		villageMap.clear();
	}
}
