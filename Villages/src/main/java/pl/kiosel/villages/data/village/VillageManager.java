package pl.kiosel.villages.data.village;

import lombok.Getter;
import org.apache.commons.lang3.Validate;
import panda.std.Option;
import panda.std.stream.PandaStream;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class VillageManager {

    private final Map<UUID, Village> villageMap = new ConcurrentHashMap<>();
	/**
	 * -- GETTER --
	 *  Returns a read-only live view for frequent iterations that do not need a snapshot.
	 */
	@Getter
	private final Collection<Village> villagesView = Collections.unmodifiableCollection(this.villageMap.values());

    public int countVillage() {
        return this.villageMap.size();
    }

    /**
     * Gets the copied set of villages.
     *
     * @return set of village
     */
    public Set<Village> getVillages() {
        return new HashSet<>(this.villageMap.values());
    }

	/**
     * Deletes all loaded villages data
     */
    public void clearVillage() {
        this.villageMap.clear();
    }

    /**
     * Gets the set of villages from collection of strings (names).
     *
     * @param names collection of names
     * @return set of village
     */
    public Set<Village> findByNames(Collection<String> names) {
        return PandaStream.of(names)
                .flatMap(this::findByName)
                .collect(Collectors.toSet());
    }

    /**
     * Gets the set of villages from collection of strings (tags).
     *
     * @param tags collection of tags
     * @return set of village
     */
    public Set<Village> findByTags(Collection<String> tags) {
        return PandaStream.of(tags)
                .flatMap(this::findByTag)
                .collect(Collectors.toSet());
    }

    /**
     * Gets the village.
     *
     * @param uuid the uuid of village
     * @return the village
     */
    public Option<Village> findByUuid(UUID uuid) {
        return Option.of(this.villageMap.get(uuid));
    }

    /**
     * Gets the village.
     *
     * @param name       the name of village
     * @param ignoreCase ignore the case of the name
     * @return the village
     */
    public Option<Village> findByName(String name, boolean ignoreCase) {
        if (ignoreCase) {
            return PandaStream.of(this.villageMap.values()).find(village -> village.getName().equalsIgnoreCase(name));
        }

        return PandaStream.of(this.villageMap.values()).find(village -> village.getName().equals(name));
    }

    /**
     * Gets the village.
     *
     * @param name the name of village
     * @return the village
     */
    public Option<Village> findByName(String name) {
        return this.findByName(name, false);
    }

    /**
     * Gets the village.
     *
     * @param tag        the tag of village
     * @param ignoreCase ignore the case of the tag
     * @return the village
     */
    public Option<Village> findByTag(String tag, boolean ignoreCase) {
        if (ignoreCase) {
            return PandaStream.of(this.villageMap.values()).find(village -> village.getTag().equalsIgnoreCase(tag));
        }

        return PandaStream.of(this.villageMap.values()).find(village -> village.getTag().equals(tag));
    }

    /**
     * Gets the village.
     *
     * @param tag the tag of village
     * @return the village
     */
    public Option<Village> findByTag(String tag) {
        return this.findByTag(tag, false);
    }

    /**
     * Add village to storage. If you think you should use this method you probably shouldn't.
     *
     * @param village village to add
     */
    public Village addVillage(Village village) {
        Validate.notNull(village, "village can't be null!");
        this.villageMap.put(village.getUUID(), village);
        return village;
    }

    /**
     * Remove village from storage. If you think you should use this method you probably shouldn't - instead use {@link VillageManager#deleteVillage(AdvancedVillages, Village)}.
     *
     * @param village village to remove
     */
    public void deleteVillage(Village village) {
        Validate.notNull(village, "village can't be null!");
        this.villageMap.remove(village.getUUID());
    }

    /**
     * Delete village in every possible way.
     *
     * @param village village to delete
     */
    public void deleteVillage(AdvancedVillages plugin, Village village) {
        if (village == null) {
            return;
        }

		village.getMembers().forEach(User::removeVillage);
        this.deleteVillage(village);
    }

    /**
     * Checks if village with given name exists.
     *
     * @param name the village name to check if exists
     * @return if village with given name exists
     */
    public boolean nameExists(String name) {
        return this.findByName(name, true).isPresent();
    }

    /**
     * Checks if village with given tag exists.
     *
     * @param tag the village tag to check if exists
     * @return if village with given tag exists
     */
    public boolean tagExists(String tag) {
        return this.findByTag(tag, true).isPresent();
    }

	/**
	 * Getting all villages tags.
	 *
	 * @param villages collection of villages
	 * @return all village tags
	 */
	public static Set<String> getTags(Collection<Village> villages) {
		return villages.stream()
				.filter(Objects::nonNull)
				.map(Village::getTag)
				.collect(Collectors.toSet());
	}

	/**
	 * Get all villages as list.
	 *
	 * @return all village as list
	 */
	public List<Village> getVillageAsList() {
		return new ArrayList<>(villageMap.values());
	}

	/**
	 * Get all villages owners.
	 *
	 * @return all village owners
	 */
	public List<String> getVillageOwners() {
		List<String> names = new ArrayList<>();
		for (Village village : getVillageAsList()) {
			if (village != null) {
				names.add(village.getOwner().getName());
			}
		}
		return names;
	}

	/**
	 * Get all villages names as list.
	 *
	 * @return all village as string list
	 */
	public List<String> getVillageNamesAsList() {
		return villageMap.values().stream()
				.filter(Objects::nonNull)
				.map(Village::getName)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	/**
	 * Get all villages tags as list.
	 *
	 * @return all village as string list
	 */
	public List<String> getVillageTagsAsList() {
		return villageMap.values().stream()
				.filter(Objects::nonNull)
				.map(Village::getTag)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	/**
	 * Disable plugin and remove all data from ram
	 */
	public void onDisable() {
		villageMap.clear();
	}
}
