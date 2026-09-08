package pl.kiosel.villages.data.village;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import pl.kiosel.rosacore.location.LocationUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.AbstractMutableEntity;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.level.Level;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.IntUnaryOperator;

public class Village extends AbstractMutableEntity {

	private final UUID uuid;
	@Getter	private final VillageRank rank;
	private final VillageMembers membership;
	private final VillageEffects effects;
	private volatile String name;
	@Getter	private volatile String tag;
	@Getter	private volatile int lives;
	@Getter	private volatile int bank;
	@Getter	private volatile Level level;
	@Nullable private volatile VillageRegion region;
	@Nullable private volatile VillageRegion turretRegion;
	@Nullable private volatile Location home;
	@Nullable private volatile Location location;
	@Nullable private volatile Instant build;
	@Getter	private volatile Instant born;
	@Getter	private volatile Instant protection = Instant.EPOCH;
	@Getter	private volatile boolean pvp;
	@Getter	private volatile boolean tnt;
	@Getter	private volatile boolean animationsEnabled = true;

	private Village(UUID uuid) {
		this.uuid = uuid == null ? UUID.randomUUID() : uuid;
		this.rank = new VillageRank(this);
		this.membership = new VillageMembers(this, this::markChanged);
		this.effects = new VillageEffects(this::markChanged);
		this.born = Instant.now();
	}

	public Village(UUID uuid, String name, String tag) {
		this(uuid);
		this.name = name;
		this.tag = tag;
	}

	public Village(UUID uuid, Location location) {
		this(uuid);
		this.location = cloneLocation(location);
	}

	public Village(String name, String tag) {
		this(null, name, tag);
	}

	public UUID getUUID() {
		return this.uuid;
	}

	public String getID() {
		return this.uuid.toString();
	}

	@Override
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		if (Objects.equals(this.name, name)) return;
		this.name = name;
		this.markChanged();
	}

	public void setTag(String tag) {
		if (Objects.equals(this.tag, tag)) return;
		this.tag = tag;
		this.markChanged();
	}

	public boolean isTag() {
		return this.tag != null && !this.tag.isBlank() && !this.tag.equalsIgnoreCase("none");
	}

	public void setLives(int lives) {
		int updated = Math.max(0, lives);
		if (this.lives == updated) return;
		this.lives = updated;
		this.markChanged();
	}

	public void updateLives(IntUnaryOperator update) {
		this.setLives(Objects.requireNonNull(update, "update").applyAsInt(this.lives));
	}

	public void addBank(int amount) {
		if (amount > 0) this.setBank(this.bank + amount);
	}

	public void removeBank(int amount) {
		if (amount > 0) this.setBank(this.bank - amount);
	}

	public void setBank(int bank) {
		int updated = Math.max(0, bank);
		if (this.bank == updated) return;
		this.bank = updated;
		this.markChanged();
	}

	public void updateBank(IntUnaryOperator update) {
		this.setBank(Objects.requireNonNull(update, "update").applyAsInt(this.bank));
	}

	public void setLevel(Level level) {
		if (Objects.equals(this.level, level)) return;
		this.level = level;
		this.markChanged();
	}

	public void upgrade() {
		this.setLevel(AdvancedVillages.getInstance().getLevelManager().getLevel(this.level.getLevel() + 1));
	}

	public boolean hasRegion() {
		return this.region != null;
	}

	public Optional<VillageRegion> getRegion() {
		return Optional.ofNullable(this.region);
	}

	public void setRegion(@Nullable VillageRegion region) {
		if (this.region == region) return;
		this.region = region;
		if (region != null) region.setVillage(this);
		this.markChanged();
	}

	public Optional<Location> getCenter() {
		return this.getRegion().map(VillageRegion::getCenter).map(Location::clone);
	}

	public Optional<Location> getLocation() {
		return Optional.ofNullable(cloneLocation(this.location));
	}

	public void setLocation(@Nullable Location location) {
		Location updated = cloneLocation(location);
		if (Objects.equals(this.location, updated)) return;
		this.location = updated;
		this.markChanged();
	}

	public Location getAnimation() {
		return this.getLocation()
				.orElseThrow(() -> new IllegalStateException("Village " + this.uuid + " has no central location"))
				.add(0.5, 1.0, 0.5);
	}

	public boolean hasHome() {
		return this.home != null;
	}

	public Optional<Location> getHome() {
		return Optional.ofNullable(cloneLocation(this.home));
	}

	public void setHome(@Nullable Location home) {
		Location updated = cloneLocation(home);
		if (Objects.equals(this.home, updated)) return;
		this.home = updated;
		this.markChanged();
	}

	public void teleportHome(Player player) {
		if (player != null) this.getHome().ifPresent(player::teleport);
	}

	public User getOwner() {
		return this.membership.getOwner();
	}

	public boolean isOwner(User user) {
		return this.membership.isOwner(user);
	}

	public void setOwner(User user) {
		this.membership.setOwner(user);
	}

	public Set<User> getMembers() {
		return this.membership.getMembers();
	}

	public Set<User> getOnlineMembers() {
		return this.membership.getOnlineMembers();
	}

	public Set<String> getMembersName() {
		return this.membership.getMemberNames();
	}

	public boolean isMember(User user) {
		return this.membership.contains(user);
	}

	public void setMembers(Set<User> members) {
		this.membership.replace(members);
	}

	public void addMember(User user) {
		this.membership.add(user);
	}

	public void removeMember(User user) {
		this.membership.remove(user);
	}

	public void deserializationUpdate() {
		this.membership.restoreLinks();
	}

	public void broadcast(String message) {
		this.membership.getMembers().forEach(user -> user.sendMessage(message));
	}

	public void setBorn(Instant born) {
		Instant updated = Objects.requireNonNull(born, "born");
		if (this.born.equals(updated)) return;
		this.born = updated;
		this.markChanged();
	}

	public void setProtection(Instant protection) {
		Instant updated = Objects.requireNonNull(protection, "protection");
		if (this.protection.equals(updated)) return;
		this.protection = updated;
		this.markChanged();
	}

	public boolean canBeAttacked() {
		return !this.protection.isAfter(Instant.now());
	}

	public Optional<Instant> getBuild() {
		return Optional.ofNullable(this.build);
	}

	public void setBuild(@Nullable Instant build) {
		Instant updated = build != null && build.isAfter(Instant.now()) ? build : null;
		if (Objects.equals(this.build, updated)) return;
		this.build = updated;
		this.markChanged();
	}

	public boolean canBuild() {
		Instant buildUntil = this.build;
		if (buildUntil == null) return true;
		if (buildUntil.isAfter(Instant.now())) return false;
		this.build = null;
		this.markChanged();
		return true;
	}

	public boolean hasPvPEnabled() {
		return this.pvp;
	}

	public void setPvP(boolean pvp) {
		if (this.pvp == pvp) return;
		this.pvp = pvp;
		this.markChanged();
	}

	public void togglePvP() {
		this.setPvP(!this.pvp);
	}

	public boolean hasTntEnabled() {
		return this.tnt;
	}

	public void setTnt(boolean tnt) {
		if (this.tnt == tnt) return;
		this.tnt = tnt;
		this.markChanged();
	}

	public boolean toggleTnt() {
		this.setTnt(!this.tnt);
		return this.tnt;
	}

	public void setAnimationsEnabled(boolean animationsEnabled) {
		if (this.animationsEnabled == animationsEnabled) return;
		this.animationsEnabled = animationsEnabled;
		this.markChanged();
	}

	public boolean toggleAnimations() {
		this.setAnimationsEnabled(!this.animationsEnabled);
		return this.animationsEnabled;
	}

	public boolean isRegeneration() {
		return this.effects.isRegeneration();
	}

	public void setRegeneration(boolean regeneration) {
		this.effects.setRegeneration(regeneration);
	}

	public boolean isSpeed() {
		return this.effects.isSpeed();
	}

	public void setSpeed(boolean speed) {
		this.effects.setSpeed(speed);
	}

	public boolean isJump() {
		return this.effects.isJump();
	}

	public void setJump(boolean jump) {
		this.effects.setJump(jump);
	}

	public boolean isHaste() {
		return this.effects.isHaste();
	}

	public void setHaste(boolean haste) {
		this.effects.setHaste(haste);
	}

	public boolean isRegenerationActive() {
		return this.effects.isRegenerationActive();
	}

	public void setRegenerationActive(boolean regenerationActive) {
		this.effects.setRegenerationActive(regenerationActive);
	}

	public boolean isSpeedActive() {
		return this.effects.isSpeedActive();
	}

	public void setSpeedActive(boolean speedActive) {
		this.effects.setSpeedActive(speedActive);
	}

	public boolean isJumpActive() {
		return this.effects.isJumpActive();
	}

	public void setJumpActive(boolean jumpActive) {
		this.effects.setJumpActive(jumpActive);
	}

	public boolean isHasteActive() {
		return this.effects.isHasteActive();
	}

	public void setHasteActive(boolean hasteActive) {
		this.effects.setHasteActive(hasteActive);
	}

	@Override
	public UnitType getType() {
		return UnitType.VILLAGE;
	}

	public boolean isSameVillage(Player first, Player second) {
		if (first == null || second == null) return false;
		return this.membership.contains(first.getUniqueId()) && this.membership.contains(second.getUniqueId());
	}

	public boolean isCentralBlock(Location location) {
		Location center = this.location;
		return center != null && LocationUtils.isLocationMatching(location, center);
	}

	public boolean isCentralBlock(Block block) {
		return block != null && this.isCentralBlock(block.getLocation());
	}

	@Nullable
	public Village getVillageAt(Location location) {
		Level currentLevel = this.level;
		return currentLevel != null && this.contains(location, currentLevel.getSize()) ? this : null;
	}

	@Nullable
	public Village getVillageAt(Location location, int customSize) {
		return this.contains(location, customSize) ? this : null;
	}

	private boolean contains(Location target, int size) {
		Location center = this.location;
		if (target == null || center == null || target.getWorld() == null || center.getWorld() == null) return false;
		if (!center.getWorld().equals(target.getWorld())) return false;

		long radius = Math.max(0, size);
		long distanceX = Math.abs((long) target.getBlockX() - center.getBlockX());
		long distanceZ = Math.abs((long) target.getBlockZ() - center.getBlockZ());
		return distanceX <= radius && distanceZ <= radius;
	}

	public static String getDefaultString() {
		return "tnt:true;pvp:true;animations:true;protection:false;";
	}

	public String convertToString() {
		return "tnt:" + this.tnt + ";pvp:" + this.pvp + ";animations:" + this.animationsEnabled
				+ ";protection:" + this.protection + ";";
	}

	public String effectsBuyedToString() {
		return this.effects.purchasedToString();
	}

	public String effectsActiveToString() {
		return this.effects.activeToString();
	}

	public String intsToString() {
		return this.level.getLevel() + ";" + this.lives + ";" + this.bank + ";";
	}

	public String tpToString() {
		Location teleport = this.home;
		if (teleport == null) return "&eTeleport: &cnot set";
		return "&eTeleport: &c" + teleport.getBlockX() + "&7&l-&c" + teleport.getBlockY()
				+ "&7&l-&c" + teleport.getBlockZ();
	}

	@Override
	public int hashCode() {
		return this.uuid.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (!(object instanceof Village)) return false;
		Village village = (Village) object;
		return this.uuid.equals(village.uuid);
	}

	@Override
	public String toString() {
		return this.name;
	}

	@Nullable
	private static Location cloneLocation(@Nullable Location location) {
		return location == null ? null : location.clone();
	}
}
