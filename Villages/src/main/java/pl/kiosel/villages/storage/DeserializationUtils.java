package pl.kiosel.villages.storage;

import org.bukkit.Location;
import pl.kiosel.rosacore.utils.TextUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.BukkitUserProfile;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.UserManager;
import pl.kiosel.villages.data.village.Region;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.VillageManager;
import pl.kiosel.villages.data.village.level.Level;
import pl.kiosel.villages.data.village.level.LevelManager;

import java.util.List;
import java.util.Optional;

public final class DeserializationUtils {

	private DeserializationUtils() {
	}

	static Optional<User> deserializeUser(UserManager userManager, UserData data) {
		if (userManager == null || data == null) return Optional.empty();
		User user = userManager.create(
				data.uuid(),
				data.name(),
				new BukkitUserProfile(data.uuid())
		);
		user.getRank().setPoints(data.points());
		user.getRank().setKills(data.kills());
		user.getRank().setDeaths(data.deaths());
		user.getRank().setAssists(data.assists());
		AdvancedVillages.getInstance().getRoleManager().deserialize(user, data.role());
		user.markUnchanged();
		return Optional.of(user);
	}

	static Optional<Village> deserializeVillage(VillageManager villageManager, VillageData data) {
		if (villageManager == null || data == null) return Optional.empty();
		AdvancedVillages plugin = AdvancedVillages.getInstance();
		plugin.getDebug().debug("Loading deserialization " + data.name());

		Village village = villageManager.findByUuid(data.uuid()).orElseGet(() -> {
			Village created = new Village(data.uuid(), data.name(), data.tag());
			villageManager.addVillage(created);
			return created;
		});

		village.setOwner(data.owner());
		village.setLocation(data.location());
		village.setHome(data.home());
		village.setMembers(data.members());
		village.setPvP(data.pvp());
		village.setLives(data.lives());
		village.setBank(data.bank());

		Level level = resolveLevel(plugin.getLevelManager(), data.level(), data.name(), plugin);
		village.setLevel(level);
		Location location = village.getLocation().orElse(null);
		if (location != null) {
			Region region = new Region(village, location, level.getSize());
			village.setRegion(region);
			region.markUnchanged();
		}

		List<String> purchasedEffects = parseBooleans(data.purchasedEffects());
		List<String> activeEffects = parseBooleans(data.activeEffects());
		village.setRegeneration(readBoolean(purchasedEffects, 0));
		village.setSpeed(readBoolean(purchasedEffects, 1));
		village.setJump(readBoolean(purchasedEffects, 2));
		village.setHaste(readBoolean(purchasedEffects, 3));
		village.setRegenerationActive(readBoolean(activeEffects, 0));
		village.setSpeedActive(readBoolean(activeEffects, 1));
		village.setJumpActive(readBoolean(activeEffects, 2));
		village.setHasteActive(readBoolean(activeEffects, 3));
		village.setProtection(data.protection());
		village.setTnt(data.tnt());
		village.setAnimationsEnabled(data.animationsEnabled());
		village.deserializationUpdate();
		village.markUnchanged();
		return Optional.of(village);
	}

	private static Level resolveLevel(LevelManager manager, int storedLevel, String villageName, AdvancedVillages plugin) {
		Level level = manager.getLevel(storedLevel);
		if (level != null) return level;
		Level fallback = storedLevel > manager.getHighestLevel().getLevel()
				? manager.getHighestLevel()
				: manager.getLowestLevel();
		plugin.getRosaLogger().warning("Village '" + villageName + "' references unavailable level "
				+ storedLevel + "; using level " + fallback.getLevel());
		return fallback;
	}

	private static List<String> parseBooleans(String serialized) {
		return TextUtils.isEmpty(serialized) ? List.of() : TextUtils.fromString(serialized);
	}

	private static boolean readBoolean(List<String> values, int index) {
		return index < values.size() && Boolean.parseBoolean(values.get(index));
	}
}
