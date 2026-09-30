package pl.kiosel.villages.data.outpost;

import org.bukkit.Location;
import pl.kiosel.rosacore.location.LocationUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.outpost.turets.TurretReset;
import pl.kiosel.villages.data.outpost.turets.TurretSetGreen;
import pl.kiosel.villages.data.outpost.turets.TurretSetRed;
import pl.kiosel.villages.data.outpost.turets.TurretSetWhite;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.turets.Turret;
import pl.kiosel.villages.data.village.turets.worldedit.WorldEditTurret;
import pl.kiosel.villages.storage.OutpostStorage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

public final class OutpostManager {

	public static final int BUILD_RADIUS = 3;
	public static final int BUILD_MIN_Y = -1;
	public static final int BUILD_MAX_Y = 4;

	private final AdvancedVillages plugin;
	private final OutpostStorage storage;
	private final ConcurrentMap<UUID, Outpost> outposts = new ConcurrentHashMap<>();
	private final Turret[] internalStructures = {
			new TurretSetWhite(), new TurretSetRed(), new TurretSetGreen()
	};
	private final TurretReset reset = new TurretReset();

	public OutpostManager(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.storage = new OutpostStorage(plugin);
	}

	public void load() {
		this.outposts.clear();
		this.storage.load(outpost -> this.outposts.put(outpost.getUuid(), outpost));
		this.plugin.getDebug().debug("Loaded outposts: " + this.outposts.size());
	}

	public void save(boolean ignoreUnchanged) {
		for (Outpost outpost : this.outposts.values()) {
			if (ignoreUnchanged && !outpost.wasChanged()) continue;
			try {
				this.storage.save(outpost);
			} catch (RuntimeException exception) {
				this.plugin.getRosaLogger().log(Level.SEVERE,
						"Could not save outpost " + outpost.getUuid(), exception);
			}
		}
	}

	public void add(Outpost outpost) {
		Objects.requireNonNull(outpost, "outpost");
		Outpost existing = this.outposts.putIfAbsent(outpost.getUuid(), outpost);
		if (existing != null && existing != outpost) {
			throw new IllegalArgumentException("An outpost with UUID " + outpost.getUuid() + " is already loaded");
		}
		try {
			this.storage.save(outpost);
		} catch (RuntimeException exception) {
			this.outposts.remove(outpost.getUuid(), outpost);
			throw exception;
		}
	}

	public void delete(Outpost outpost) {
		if (outpost == null) return;
		if (this.outposts.remove(outpost.getUuid(), outpost)) {
			this.storage.delete(outpost.getUuid());
		}
	}

	public void deleteByVillage(Village village) {
		if (village == null) return;
		this.outposts.values().removeIf(outpost -> outpost.getVillage().equals(village));
		this.storage.deleteByVillage(village.getUUID());
	}

	public Collection<Outpost> getOutposts() {
		return Collections.unmodifiableCollection(new ArrayList<>(this.outposts.values()));
	}

	public List<Outpost> getOutposts(Village village) {
		if (village == null) return Collections.emptyList();
		return this.outposts.values().stream()
				.filter(outpost -> outpost.getVillage().equals(village))
				.sorted(Comparator.comparing(Outpost::getBorn))
				.toList();
	}

	public int count(Village village) {
		return this.getOutposts(village).size();
	}

	public Optional<Outpost> findByUuid(UUID uuid) {
		return uuid == null ? Optional.empty() : Optional.ofNullable(this.outposts.get(uuid));
	}

	public Optional<Outpost> findAt(Location location) {
		if (location == null) return Optional.empty();
		return this.outposts.values().stream().filter(outpost -> outpost.contains(location)).findFirst();
	}

	public Optional<Outpost> findCore(Location location) {
		if (location == null) return Optional.empty();
		return this.outposts.values().stream()
				.filter(outpost -> outpost.getLocation().map(center -> LocationUtils.isLocationMatching(center, location)).orElse(false))
				.findFirst();
	}

	public Optional<Outpost> findStructure(Location location) {
		if (location == null) return Optional.empty();
		return this.outposts.values().stream().filter(outpost -> outpost.containsStructure(location)).findFirst();
	}

	public boolean isNearby(Location location, int minimumDistance) {
		if (location == null || location.getWorld() == null) return false;
		long distanceSquared = (long) Math.max(0, minimumDistance) * Math.max(0, minimumDistance);
		return this.outposts.values().stream().anyMatch(outpost -> outpost.getLocation().map(center ->
				center.getWorld() != null && center.getWorld().equals(location.getWorld())
						&& horizontalDistanceSquared(center, location) < distanceSquared).orElse(false));
	}

	public void paste(Outpost outpost) {
		Location location = outpost.getLocation().orElseThrow();
		WorldEditTurret worldEdit = new WorldEditTurret(this.plugin);
		if (plugin.getOutpostLevelManager().hasSchematic(outpost.getLevel().getLevel())) {
			worldEdit.pasteOutpost(location, outpost.getLevel().getLevel());
			return;
		}
		pasteInternal(outpost.getLevel().getLevel(), location);
	}

	public void pasteInternal(int level, Location location) {
		int index = level - 1;
		if (index < 0 || index >= this.internalStructures.length) {
			throw new IllegalStateException("Outpost level " + level + " has no internal structure");
		}
		this.internalStructures[index].setTurret(
				Objects.requireNonNull(location.getWorld(), "outpost world"),
				location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	public boolean canPasteLevel(int level) {
		return level >= 1 && (level <= this.internalStructures.length
				|| plugin.getOutpostLevelManager().hasSchematic(level));
	}

	public boolean upgrade(Outpost outpost) {
		if (outpost == null) return false;

		int nextNumber = outpost.getLevel().getLevel() + 1;
		pl.kiosel.villages.data.village.level.Level next = this.plugin.getOutpostLevelManager().getLevel(nextNumber);
		if (next == null || !canPasteLevel(nextNumber)) return false;

		Location location = outpost.getLocation().orElseThrow();
		this.reset.setAir(location);
		outpost.setLevel(next);

		paste(outpost);
		this.storage.save(outpost);
		return true;
	}

	public void clear() {
		this.outposts.clear();
	}

	private long horizontalDistanceSquared(Location first, Location second) {
		long x = (long) first.getBlockX() - second.getBlockX();
		long z = (long) first.getBlockZ() - second.getBlockZ();
		return x * x + z * z;
	}
}
