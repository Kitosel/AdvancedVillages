package pl.kiosel.villages.data.village;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import panda.std.Option;
import panda.std.stream.PandaStream;
import pl.kiosel.core.utils.LocationUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.AbstractMutableEntity;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.level.Level;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntFunction;

public class Village extends AbstractMutableEntity {

    private final UUID uuid;

    private String name;
    @Getter private String tag;

    @Getter private final VillageRank rank;
    @Getter private int lives, bank;
	@Getter @Setter private Level level;

	@Getter private Option<Region> region = Option.none();
	@Getter private Option<Location> home = Option.none();
	@Getter private Option<Location> location = Option.none();

    @Getter private User owner;
    @Getter private Set<User> members = ConcurrentHashMap.newKeySet();

    @Getter private Instant born;
    @Getter private Instant protection = Instant.EPOCH;
    @Getter private Option<Instant> build = Option.none();

	@Getter
	private boolean speed, jump, regeneration, haste, pvp, tnt;

	@Getter
	private boolean animationsEnabled = true;
	@Getter @Setter
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
        this.owner.setVillage(this);
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

	public void updateLives(IntFunction<Integer> update) {
		this.setLives(update.apply(this.lives));
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

	public void updateBank(IntFunction<Integer> update) {
		this.setBank(update.apply(this.bank));
	}

	public boolean hasRegion() {
        return this.region.isPresent();
    }

    public void setRegion(@Nullable Region region) {
        this.region = Option.of(region);
        this.region.peek(peekRegion -> peekRegion.setVillage(this));
        this.markChanged();
    }

    public Option<Location> getCenter() {
        return this.region
                .map(Region::getCenter)
                .map(Location::clone);
    }

	public boolean hasHome() {
        return this.home.isPresent();
    }

    public void setHome(@Nullable Location home) {
        this.home = Option.of(home);
        this.markChanged();
    }

    public void teleportHome(Player player) {
        this.home.peek(player::teleport);
    }


	public void setLocation(@Nullable Location location) {
		this.location = Option.of(location);
		this.markChanged();
	}

	public boolean isOwner(User user) {
		return Objects.equals(this.owner, user);
    }

    public void setOwner(User user) {
        this.owner = user;
        this.addMember(user);
        this.markChanged();
    }

	public Set<User> getOnlineMembers() {
        return PandaStream.of(this.members)
                .filter(User::isOnline)
                .toSet();
    }

    public boolean isMember(User user) {
		return user != null && this.members.contains(user);
    }

    public void setMembers(Set<User> members) {
		Set<User> concurrentMembers = ConcurrentHashMap.newKeySet();
		if (members != null) {
			concurrentMembers.addAll(members);
		}
		if (this.owner != null) {
			concurrentMembers.add(this.owner);
		}
		this.members = concurrentMembers;
        this.markChanged();
    }

    public void addMember(User user) {
        this.members.add(user);
        this.markChanged();
    }

	public void removeMember(User user) {
		this.members.remove(user);
		this.markChanged();
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
        if (this.build.is(build -> build.isAfter(Instant.now()))) {
            return false;
        }

        this.build = Option.none();
        this.markChanged();
        return true;
    }

    public void setBuild(@Nullable Instant time) {
        if (time != null && time.isBefore(Instant.now())) {
            time = null;
        }

        this.build = Option.of(time);
        this.markChanged();
    }

    public boolean hasPvPEnabled() {
        return this.pvp;
    }

    public void setPvP(boolean pvp) {
        this.pvp = pvp;
        this.markChanged();
    }

    public boolean togglePvP() {
		setPvP(!this.pvp);
        return this.pvp;
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

    @Override
    public UnitType getType() {
        return UnitType.VILLAGE;
    }

    @Override
    public int hashCode() {
        return this.uuid.hashCode();
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

	public String getInts() {
		return level.getLevel() + ";" + lives + ";" + bank + ";";
	}

	public boolean isTag() {
		if (Objects.equals(tag, "") || Objects.equals(tag, "none")) {
			return false;
		}
		return tag!=null;
	}

	public void upgrade() {
		setLevel(AdvancedVillages.getInstance().getLevelManager().getLevel(getLevel().getLevel() + 1));
	}

	public boolean isCentralBlock(Location loc) {
		return LocationUtils.isLocationMatching(loc, getLocation().get());
	}

	public boolean isCentralBlock(Block block) {
		return isCentralBlock(block.getLocation());
	}

	@Nullable
	public Village getVillageAt(Location loc) {
		boolean isVillage = false;
		int x = location.get().getBlockX();
		int z = location.get().getBlockZ();
		int blockX = loc.getBlockX();
		int blockZ = loc.getBlockZ();
		int size = getLevel().getSize();

		if((blockX >= x && blockX <= x + size) && (blockZ >= z && blockZ <= z + size)) {
			isVillage = true;
		} else if((blockX <= x && blockX >= x - size) && (blockZ <= z && blockZ >= z - size)) {
			isVillage = true;
		} else if((blockX >= x && blockX <= x + size) && (blockZ <= z && blockZ >= z - size)) {
			isVillage = true;
		} else if((blockX <= x && blockX >= x - size) && (blockZ >= z && blockZ <= z + size)) {
			isVillage = true;
		}
		if(isVillage) {
			return this;
		}
		return null;
	}

	@Nullable
	public Village getVillageAt(Location loc, int custom_size) {
		boolean isVillage = false;
		int x = location.get().getBlockX();
		int z = location.get().getBlockZ();
		int blockX = loc.getBlockX();
		int blockZ = loc.getBlockZ();

		if((blockX >= x && blockX <= x + custom_size) && (blockZ >= z && blockZ <= z + custom_size)) {
			isVillage = true;
		} else if((blockX <= x && blockX >= x - custom_size) && (blockZ <= z && blockZ >= z - custom_size)) {
			isVillage = true;
		} else if((blockX >= x && blockX <= x + custom_size) && (blockZ <= z && blockZ >= z - custom_size)) {
			isVillage = true;
		} else if((blockX <= x && blockX >= x - custom_size) && (blockZ >= z && blockZ <= z + custom_size)) {
			isVillage = true;
		}
		if(isVillage) return this;
		return null;
	}

	public static boolean getValueFromString(String string, String flag) {
		String[] array = string.split(";");
		for(String text : array) {
			String[] text_array = text.split(":");
			if(text_array[0].equals(flag))
				return Boolean.parseBoolean(text_array[1]);
		}
		return false;
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
		return "&eTeleport: &c" + home.get().getBlockX() + "&7&l-&c" + home.get().getBlockY() + "&7&l-&c" + home.get().getBlockZ();
	}

}
