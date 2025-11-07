package pl.kiosel.villages.manager;

import lombok.Getter;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.VillageMember;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class VillageDataManager {

	@Getter private final Map<UUID, VillageMember> members = new HashMap<>();
    @Getter private final Map<String, Village> villages = new HashMap<>();

    private final AdvancedVillages plugin;

    public VillageDataManager(AdvancedVillages plugin) {
        this.plugin = plugin;
    }

    public void putVillage(String name, Village village) {
        villages.put(name, village);
    }

	public void removeVillage(Village village) {
		villages.remove(village.getVillageName());
	}

    public void flush() {
        villages.clear();
    }

	public List<Village> getVillageAsList() {
		return new ArrayList<>(villages.values());
	}

	public int countVillages() {
		return this.villages.size();
	}

	public List<String> getVillageOwners() {
		List<String> names = new ArrayList<>();
		for (Village village : getVillageAsList()) {
			if (village != null) {
				names.add(village.getOwner());
			}
		}
		return names;
	}

	public List<String> getVillageNamesAsList() {
		return villages.values().stream()
				.filter(Objects::nonNull)
				.map(Village::getVillageName)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

    public void addPlayerMember(UUID player_name, VillageMember member) {
        members.put(player_name, member);
    }

    public void removePlayerMember(UUID player_name) {
        if(members.get(player_name) != null)
            members.remove(player_name);
    }

    @Nullable
    public VillageMember getVillageMember(UUID player_name) {
        if(members.containsKey(player_name))
            return members.get(player_name);
        return null;
    }

    public void saveVillages() {
        for(Village village : villages.values()) {
            plugin.getDataHelper().saveVillageSync(village);
        }
    }

    public void onDisable() {
        saveVillages();
        members.clear();
        villages.clear();
    }
}