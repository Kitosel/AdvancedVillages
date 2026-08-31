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
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;

public class Village extends AbstractMutableEntity {

    private final UUID uuid;

    private String name;
    @Getter private String tag;

    @Getter private final VillageRank rank;
    @Getter private int lives, bank;
	@Getter private Level level;

	@Nullable private volatile Region region;
	@Nullable private volatile Location home;
	@Nullable private volatile Location location;

    @Getter private User owner;
    private final Set<User> members = ConcurrentHashMap.newKeySet();
    private final Set<User> membersView = Collections.unmodifiableSet(this.members);

    @Getter private Instant born;
    @Getter private Instant protection = Instant.EPOCH;
    @Nullable private volatile Instant build;

	@Getter
	private boolean speed, jump, regeneration, haste, pvp, tnt;

	@Getter
	private boolean animationsEnabled = true;
	@Getter
	private boolean speedActive, jumpActive, regenerationActive, hasteActive;

    public Village(UUID uuid, String name, String tag) {
        this.uuid = uuid != null ? uuid : UUID.randomUUID();
        this.name = name;
        this.tag = tag;

        this.rank = new VillageRank(this);
        this.born = Instant.now();
    }

	public Village(UUID uuid, Location location) {
		this.uuid = uuid != null ? uuid : UUID.randomUUID();
		setLocation(location);

		this.rank = new VillageRank(this);
		this.born = Instant.now();
	}

    public Village(String name, String tag) {
        this(null, name, tag);
    }

    public void broadcast(String message) {
        this.members.forEach(user -> user.sendMessage(message));
    }

    public void deserializationUpdate() {
        if (this.owner != null) this.owner.setVillage(this);
        this.members.forEach(user -> user.setVillage(this));
    }

    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
        this.markChanged();
    }

	public void setTag(String tag) {
        this.tag = tag;
        this.markChanged();
    }

	public void setLives(int lives) {
		this.lives = Math.max(0, lives);
		this.markChanged();
	}

	public void updateLives(IntUnaryOperator update) {
		this.setLives(update.applyAsInt(this.lives));
	}

	public void addBank(int bank) {
		this.bank = this.bank + Math.max(0, bank);
		this.markChanged();
	}

	public void removeBank(int bank) {
		this.setBank(this.bank - Math.max(0, bank));
	}

	public void setBank(int bank) {
		this.bank = Math.max(0, bank);
		this.markChanged();
	}

	public void setLevel(Level level) {
		this.level = level;
		this.markChanged();
	}

	public Location getAnimation() {
		return getLocation().orElseThrow(() ->
				new IllegalStateException("Village " + this.uuid + " has no central location"))
				.add(0.5, 1.0, 0.5);
	}

	public void updateBank(IntUnaryOperator update) {
		this.setBank(update.applyAsInt(this.bank));
	}

	public boolean hasRegion() {
        return this.region != null;
    }

    public void setRegion(@Nullable Region region) {
		if (this.region != region) {
			this.region = region;
			if (region != null) region.setVillage(this);
			this.markChanged();
		}
    }

    public Optional<Location> getCenter() {
        return this.getRegion()
                .map(Region::getCenter)
                .map(Location::clone);
    }

	public Optional<Region> getRegion() {
		return Optional.ofNullable(this.region);
	}

	public boolean hasHome() {
        return this.home != null;
    }

    public void setHome(@Nullable Location home) {
		Location updated = cloneLocation(home);
		if (!Objects.equals(this.home, updated)) {
			this.home = updated;
			this.markChanged();
		}
    }

    public void teleportHome(Player player) {
        if (player != null) this.getHome().ifPresent(player::teleport);
    }

	public Optional<Location> getHome() {
		return Optional.ofNullable(cloneLocation(this.home));
	}

	public void setLocation(@Nullable Location location) {
		Location updated = cloneLocation(location);
		if (!Objects.equals(this.location, updated)) {
			this.location = updated;
			this.markChanged();
		}
	}

	public Optional<Location> getLocation() {
		return Optional.ofNullable(cloneLocation(this.location));
	}

	public boolean isOwner(User user) {
		return Objects.equals(this.owner, user);
    }

    public void setOwner(User user) {
		Objects.requireNonNull(user, "owner");
		if (!Objects.equals(this.owner, user)) {
			this.owner = user;
			this.addMember(user);
			this.markChanged();
		}
    }

	public Set<User> getOnlineMembers() {
        return this.members.stream()
                .filter(User::isOnline)
                .collect(Collectors.toUnmodifiableSet());
    }

	public Set<User> getMembers() {
		return this.membersView;
	}

	public Set<String> getMembersName() {
		Set<String> names = new HashSet<>();
		for (User user : this.members) {
			names.add(user.getName());
		}
		return names;
	}

    public boolean isMember(User user) {
		return user != null && this.members.contains(user);
    }

    public void setMembers(Set<User> members) {
		Set<User> updated = members == null ? new HashSet<>() : new HashSet<>(members);
		if (this.owner != null) updated.add(this.owner);
		for (User member : new HashSet<>(this.members)) {
			if (!updated.contains(member)) this.removeMember(member);
		}
		for (User member : updated) this.addMember(member);
    }

    public void addMember(User user) {
		if (user != null && this.members.add(user)) {
			user.setVillage(this);
			this.markChanged();
		}
    }

	public void removeMember(User user) {
		if (user != null && this.members.remove(user)) {
			if (user.getPresentVillage() == this) user.removeVillage();
			this.markChanged();
		}
	}

	public void setBorn(Instant time) {
        this.born = time;
        this.markChanged();
    }

	public boolean canBeAttacked() {
        return this.protection.isBefore(Instant.now().minus(1, ChronoUnit.SECONDS));
    }

    public void setProtection(Instant protection) {
        this.protection = protection;
        this.markChanged();
    }

	public boolean canBuild() {
        Instant buildUntil = this.build;
        if (buildUntil != null && buildUntil.isAfter(Instant.now())) {
            return false;
        }

        if (buildUntil != null) {
			this.build = null;
			this.markChanged();
		}
        return true;
    }

    public void setBuild(@Nullable Instant time) {
        if (time != null && time.isBefore(Instant.now())) {
            time = null;
        }

		if (!Objects.equals(this.build, time)) {
			this.build = time;
			this.markChanged();
		}
    }

	public Optional<Instant> getBuild() {
		return Optional.ofNullable(this.build);
	}

    public boolean hasPvPEnabled() {
        return this.pvp;
    }

    public void setPvP(boolean pvp) {
        this.pvp = pvp;
        this.markChanged();
    }

    public void togglePvP() {
		setPvP(!this.pvp);
	}

	public boolean hasTntEnabled() {
		return this.tnt;
	}

	public void setTnt(boolean tnt) {
		this.tnt = tnt;
		this.markChanged();
	}

	public boolean toggleTnt() {
		setTnt(!this.tnt);
		return this.tnt;
	}

	public void setAnimationsEnabled(boolean animationsEnabled) {
		this.animationsEnabled = animationsEnabled;
		this.markChanged();
	}

	public boolean toggleAnimations() {
		setAnimationsEnabled(!this.animationsEnabled);
		return this.animationsEnabled;
	}

	public void setRegeneration(boolean regeneration) {
		this.regeneration = regeneration;
		this.markChanged();
	}

	public void setSpeed(boolean speed) {
		this.speed = speed;
		this.markChanged();
	}

	public void setJump(boolean jump) {
		this.jump = jump;
		this.markChanged();
	}

	public void setHaste(boolean haste) {
		this.haste = haste;
		this.markChanged();
	}

	public void setSpeedActive(boolean speedActive) {
		this.speedActive = speedActive;
		this.markChanged();
	}

	public void setJumpActive(boolean jumpActive) {
		this.jumpActive = jumpActive;
		this.markChanged();
	}

	public void setRegenerationActive(boolean regenerationActive) {
		this.regenerationActive = regenerationActive;
		this.markChanged();
	}

	public void setHasteActive(boolean hasteActive) {
		this.hasteActive = hasteActive;
		this.markChanged();
	}

    @Override
    public UnitType getType() {
        return UnitType.VILLAGE;
    }

    @Override
    public int hashCode() {
        return this.uuid.hashCode();
    }

	public String getID() {
		return this.uuid.toString();
	}

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }

        Village village = (Village) obj;
        return this.uuid.equals(village.uuid);
    }

	public boolean isSameVillage(Player p1, Player p2) {
		if (p1 == null || p2 == null) return false;

		UUID uuid1 = p1.getUniqueId();
		UUID uuid2 = p2.getUniqueId();
		return containsMember(uuid1) && containsMember(uuid2);
	}

	private boolean containsMember(UUID playerId) {
		if (this.owner != null && this.owner.getUUID().equals(playerId)) {
			return true;
		}
		for (User member : this.members) {
			if (member.getUUID().equals(playerId)) {
				return true;
			}
		}
		return false;
	}

    @Override
    public String toString() {
        return this.name;
    }

	public boolean isTag() {
		return tag != null && !tag.isBlank() && !tag.equalsIgnoreCase("none");
	}

	public void upgrade() {
		setLevel(AdvancedVillages.getInstance().getLevelManager().getLevel(getLevel().getLevel() + 1));
	}

	public boolean isCentralBlock(Location loc) {
		Location center = this.location;
		return center != null && LocationUtils.isLocationMatching(loc, center);
	}

	public boolean isCentralBlock(Block block) {
		return isCentralBlock(block.getLocation());
	}

	@Nullable
	public Village getVillageAt(Location loc) {
		return this.contains(loc, this.getLevel().getSize()) ? this : null;
	}

	@Nullable
	public Village getVillageAt(Location loc, int customSize) {
		return this.contains(loc, customSize) ? this : null;
	}

	private boolean contains(Location target, int size) {
		Location center = this.location;
		if (target == null || center == null
				|| target.getWorld() == null || center.getWorld() == null
				|| !center.getWorld().equals(target.getWorld())) {
			return false;
		}

		long radius = Math.max(0, size);
		long distanceX = Math.abs((long) target.getBlockX() - center.getBlockX());
		long distanceZ = Math.abs((long) target.getBlockZ() - center.getBlockZ());
		return distanceX <= radius && distanceZ <= radius;
	}

	public static String getDefaultString() {
		return "tnt:true;pvp:true;animations:true;protection:false;";
	}

	public String convertToString() {
		return "tnt:" + tnt + ";pvp:" + pvp + ";animations:" + animationsEnabled + ";protection:" + protection+";";
	}

	public String effectsBuyedToString() {
		return isRegeneration() + ";" + isSpeed() + ";" + isJump() + ";" + isHaste() + ";";
	}

	public String effectsActiveToString() {
		return isRegenerationActive() + ";" + isSpeedActive() + ";" + isJumpActive() + ";" + isHasteActive() + ";";
	}

	public String intsToString() {
		return getLevel().getLevel() + ";" + getLives() + ";" + getBank() + ";";
	}

	public String tpToString() {
		Location teleport = this.home;
		if (teleport == null) return "&eTeleport: &cnot set";
		return "&eTeleport: &c" + teleport.getBlockX() + "&7&l-&c" + teleport.getBlockY()
				+ "&7&l-&c" + teleport.getBlockZ();
	}

	@Nullable
	private static Location cloneLocation(@Nullable Location location) {
		return location == null ? null : location.clone();
	}

}
