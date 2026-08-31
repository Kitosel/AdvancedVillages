package pl.kiosel.villages.storage;

import org.bukkit.Location;
import pl.kiosel.rosacore.utils.TextUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.BukkitUserProfile;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.UserManager;
import pl.kiosel.villages.data.user.UserProfile;
import pl.kiosel.villages.data.village.Region;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.VillageManager;
import pl.kiosel.villages.data.village.level.Level;
import pl.kiosel.villages.data.village.level.LevelManager;

import java.time.Instant;
import java.util.*;

public final class DeserializationUtils {

    private DeserializationUtils() {
    }

    public static Optional<User> deserializeUser(UserManager userManager, Object[] values) {
        UUID playerUniqueId = UUID.fromString((String) values[0]);
        String playerName = (String) values[1];

		UserProfile profile = new BukkitUserProfile(playerUniqueId);
        User user = userManager.create(playerUniqueId, playerName, profile);

        user.getRank().setPoints((int) values[2]);
        user.getRank().setKills((int) values[3]);
        user.getRank().setDeaths((int) values[4]);
        user.getRank().setAssists((int) values[5]);
        user.getRank().setLogouts((int) values[6]);
		AdvancedVillages.getInstance().getRoleManager().deserialize(user, (String) values[7]);

        user.markUnchanged();
        return Optional.of(user);
    }

    @SuppressWarnings("unchecked")
    public static Optional<Village> deserializeVillage(VillageManager villageManager, Object[] values) {
        if (values == null) {
			AdvancedVillages.getInstance().getRosaLogger().warning("Cannot deserialize village, caused by: null");
            return Optional.empty();
        }
		AdvancedVillages plugin = AdvancedVillages.getInstance();
		plugin.getDebug().debug("Loading deserialization " + values[1]);

		boolean ignoreUppercase = true;
		boolean upperCase = true;

        UUID villageUuid = (UUID) values[0];
        String villageName = (String) values[1];
        String rawVillageTag = (String) values[13];
        String villageTag = ignoreUppercase
                ? rawVillageTag
                : upperCase
                ? rawVillageTag.toUpperCase(Locale.ROOT)
                : rawVillageTag.toLowerCase(Locale.ROOT);

		Village village = villageManager.findByUuid(villageUuid).orElseGet(() -> {
			Village newVillage = new Village(villageUuid, villageName, villageTag);
			villageManager.addVillage(newVillage);
            return newVillage;
        });

		village.setOwner((User) values[2]);
		village.setLocation((Location) values[3]);
		village.setHome((Location) values[4]);

		village.setMembers((Set<User>) values[5]);
		village.setPvP((boolean) values[6]);
		village.setLives((int) values[7]);
		village.setBank((int) values[8]);
		LevelManager levelManager = plugin.getLevelManager();
		int storedLevel = (int) values[9];
		Level villageLevel = levelManager.getLevel(storedLevel);
		if (villageLevel == null) {
			villageLevel = storedLevel > levelManager.getHighestLevel().getLevel()
					? levelManager.getHighestLevel()
					: levelManager.getLowestLevel();
			plugin.getRosaLogger().warning("Village '" + villageName + "' references unavailable level "
					+ storedLevel + "; using level " + villageLevel.getLevel());
		}
		village.setLevel(villageLevel);

		Location villageLocation = village.getLocation().orElse(null);
		if (villageLocation != null) {
			Region region = new Region(village, villageLocation, villageLevel.getSize());
			village.setRegion(region);
			region.markUnchanged();
		}

		String effects_data = (String) values[10];
		String effects_active = (String) values[11];

		List<String> data = TextUtils.isEmpty(effects_data)
				? List.of()
				: TextUtils.fromString(effects_data);
		List<String> active = TextUtils.isEmpty(effects_active)
				? List.of()
				: TextUtils.fromString(effects_active);

		village.setRegeneration(readBoolean(data, 0));
		village.setSpeed(readBoolean(data, 1));
		village.setJump(readBoolean(data, 2));
		village.setHaste(readBoolean(data, 3));

		village.setRegenerationActive(readBoolean(active, 0));
		village.setSpeedActive(readBoolean(active, 1));
		village.setJumpActive(readBoolean(active, 2));
		village.setHasteActive(readBoolean(active, 3));

		village.setProtection((Instant) values[12]);
		village.setTnt((boolean) values[14]);

		if (values.length > 15 && values[15] != null) {
			village.setAnimationsEnabled((boolean) values[15]);
		}
		village.deserializationUpdate();

		village.markUnchanged();
        return Optional.of(village);
    }

	private static boolean readBoolean(List<String> values, int index) {
		return index < values.size() && Boolean.parseBoolean(values.get(index));
	}

}
