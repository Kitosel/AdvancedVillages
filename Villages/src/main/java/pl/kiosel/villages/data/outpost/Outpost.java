package pl.kiosel.villages.data.outpost;

import lombok.Getter;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;
import pl.kiosel.villages.data.AbstractMutableEntity;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.data.village.level.Level;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class Outpost extends AbstractMutableEntity {

	@Getter private final UUID uuid;
	@Getter private final Village village;
	@Getter private final Instant born;
	private volatile String name;
	@Getter private volatile Level level;
	@Nullable private volatile Location location;

	public Outpost(Village village, String name, Location location, Level level) {
		this(null, village, name, location, level, Instant.now());
	}

	public Outpost(@Nullable UUID uuid, Village village, String name, Location location,
				   Level level, Instant born) {
		this.uuid = uuid == null ? UUID.randomUUID() : uuid;
		this.village = Objects.requireNonNull(village, "village");
		this.name = requireName(name);
		this.location = cloneLocation(Objects.requireNonNull(location, "location"));
		this.level = Objects.requireNonNull(level, "level");
		this.born = Objects.requireNonNull(born, "born");
	}

	public String getID() {
		return this.uuid.toString();
	}

	@Override
	public UnitType getType() {
		return UnitType.OUTPOST;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		String updated = requireName(name);
		if (this.name.equals(updated)) return;
		this.name = updated;
		this.markChanged();
	}

	public Optional<Location> getLocation() {
		return Optional.ofNullable(cloneLocation(this.location));
	}

	public void setLocation(Location location) {
		Location updated = cloneLocation(Objects.requireNonNull(location, "location"));
		if (Objects.equals(this.location, updated)) return;
		this.location = updated;
		this.markChanged();
	}

	public Location getTeleportLocation() {
		return this.getLocation()
				.orElseThrow(() -> new IllegalStateException("Outpost " + this.uuid + " has no location"))
				.add(0.5D, 1.0D, 0.5D);
	}

	public void setLevel(Level level) {
		Level updated = Objects.requireNonNull(level, "level");
		if (Objects.equals(this.level, updated)) return;
		this.level = updated;
		this.markChanged();
	}

	public boolean contains(Location target) {
		Location center = this.location;
		if (target == null || center == null || target.getWorld() == null || center.getWorld() == null) return false;
		if (!center.getWorld().equals(target.getWorld())) return false;
		long radius = Math.max(0, this.level.getSize());
		return Math.abs((long) target.getBlockX() - center.getBlockX()) <= radius
				&& Math.abs((long) target.getBlockZ() - center.getBlockZ()) <= radius;
	}

	public boolean containsStructure(Location target) {
		Location center = this.location;
		if (target == null || center == null || target.getWorld() == null || center.getWorld() == null) return false;
		if (!center.getWorld().equals(target.getWorld())) return false;
		long x = Math.abs((long) target.getBlockX() - center.getBlockX());
		long z = Math.abs((long) target.getBlockZ() - center.getBlockZ());
		int relativeY = target.getBlockY() - center.getBlockY();
		return x <= OutpostManager.BUILD_RADIUS && z <= OutpostManager.BUILD_RADIUS
				&& relativeY >= OutpostManager.BUILD_MIN_Y && relativeY <= OutpostManager.BUILD_MAX_Y;
	}

	@Override
	public int hashCode() {
		return this.uuid.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (!(object instanceof Outpost)) return false;
		return this.uuid.equals(((Outpost) object).uuid);
	}

	@Override
	public String toString() {
		return this.name;
	}

	private static String requireName(String name) {
		if (name == null || name.isBlank()) throw new IllegalArgumentException("Outpost name cannot be blank");
		return name.trim();
	}

	@Nullable
	private static Location cloneLocation(@Nullable Location location) {
		return location == null ? null : location.clone();
	}
}
