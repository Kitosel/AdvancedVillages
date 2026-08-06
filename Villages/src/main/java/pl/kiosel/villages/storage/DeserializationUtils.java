package pl.kiosel.villages.storage;

import org.bukkit.Location;
import panda.std.Option;
import pl.kiosel.core.CoreLogger;
import pl.kiosel.core.utils.TextUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.BukkitUserProfile;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.UserManager;
import pl.kiosel.villages.data.user.UserProfile;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.VillageManager;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public final class DeserializationUtils {

    private DeserializationUtils() {
    }

    public static Option<User> deserializeUser(UserManager userManager, Object[] values) {
        UUID playerUniqueId = UUID.fromString((String) values[0]);
        String playerName = (String) values[1];

        UserProfile profile = new BukkitUserProfile(playerUniqueId, AdvancedVillages.getInstance().getMetaServer());
        User user = userManager.create(playerUniqueId, playerName, profile);

        user.getRank().setPoints((int) values[2]);
        user.getRank().setKills((int) values[3]);
        user.getRank().setDeaths((int) values[4]);
        user.getRank().setAssists((int) values[5]);
        user.getRank().setLogouts((int) values[6]);
		user.setPermissions(AdvancedVillages.getInstance().getPermissionManager().fromString((String) values[7]));

        user.markUnchanged();
        return Option.of(user);
    }

    @SuppressWarnings("unchecked")
    public static Option<Village> deserializeVillage(VillageManager villageManager, Object[] values) {
        if (values == null) {
            CoreLogger.getInstance().warning("Cannot deserialize village, caused by: null");
            return Option.none();
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
		village.setLevel(plugin.getLevelManager().getLevel((int) values[9]));

		String effects_data = (String) values[10];
		String effects_active = (String) values[11];

		List<String> data = TextUtils.fromString(effects_data);
		List<String> active = TextUtils.fromString(effects_active);

		village.setRegeneration(Boolean.parseBoolean(data.get(0)));
		village.setSpeed(Boolean.parseBoolean(data.get(1)));
		village.setJump(Boolean.parseBoolean(data.get(2)));
		village.setHaste(Boolean.parseBoolean(data.get(3)));

		village.setRegenerationActive(Boolean.parseBoolean(active.get(0)));
		village.setSpeedActive(Boolean.parseBoolean(active.get(1)));
		village.setJumpActive(Boolean.parseBoolean(active.get(2)));
		village.setHasteActive(Boolean.parseBoolean(active.get(3)));

		village.setProtection((Instant) values[12]);
		village.setTnt((boolean) values[14]);
		// The last value was added by the animations migration.  Keeping the
		// fallback makes deserialization safe for integrations passing the old
		// 15-value representation.
		if (values.length > 15 && values[15] != null) {
			village.setAnimationsEnabled((boolean) values[15]);
		}
		village.deserializationUpdate();

		village.markUnchanged();
        return Option.of(village);
    }

}
