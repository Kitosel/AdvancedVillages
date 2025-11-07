package pl.kiosel.villages.data.village;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import pl.kiosel.core.utils.LocationUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Village {

	private final AdvancedVillages plugin;

	@Getter @Setter private String owner;
	@Getter @Setter private UUID ownerUUID;
    @Getter @Setter private Location location;
    @Getter @Setter private List<UUID> members;
    @Getter @Setter private String villageName;
    @Getter @Setter private VillageSettings villageSettings;
	@Getter @Setter private int bank;
	@Getter @Setter private Level level;

    @Getter @Setter
    private String tag;
    @Getter @Setter
    private Location teleport;
    @Getter @Setter
    private int size, life;
	@Getter @Setter
	private boolean speed, jump, regeneration, haste, protection;
	@Getter @Setter
	private boolean speedActive, jumpActive, regenerationActive, hasteActive;

    public Village(String owner, Location location, Location teleport, List<UUID> members, String villageName, String effects_data, String effects_active, String ints_data, String tag) {
        this.plugin = AdvancedVillages.getInstance();
		this.owner = owner;
		this.ownerUUID = Bukkit.getOfflinePlayer(owner).getUniqueId();
        this.location = location;
        this.teleport = teleport;
		this.members = new ArrayList<>(members);
        this.villageName = villageName;
        this.villageSettings = new VillageSettings(this);

        String[] effects_data_array = effects_data.split(";");
        this.speed = Boolean.parseBoolean(effects_data_array[0]);
        this.jump = Boolean.parseBoolean(effects_data_array[1]);
        this.regeneration = Boolean.parseBoolean(effects_data_array[2]);
        this.haste = Boolean.parseBoolean(effects_data_array[3]);

		String[] effects_active_array = effects_active.split(";");
		this.speedActive = Boolean.parseBoolean(effects_active_array[0]);
		this.jumpActive = Boolean.parseBoolean(effects_active_array[1]);
		this.regenerationActive = Boolean.parseBoolean(effects_active_array[2]);
		this.hasteActive = Boolean.parseBoolean(effects_active_array[3]);

        String[] ints = ints_data.split(";");
        this.level = plugin.getLevelManager().getLevel(Integer.parseInt(ints[0]));
        this.life = Integer.parseInt(ints[1]);
        this.bank = Integer.parseInt(ints[2]);
        this.tag = tag;
    }

	public Village(Location location) {
		this.plugin = AdvancedVillages.getInstance();
		this.location = location;
		this.villageSettings = new VillageSettings(this);
	}

	public String getInts() {
		return level.getLevel() + ";" + life + ";" + bank + ";";
	}

	public boolean isTag() {
		if (Objects.equals(tag, "") || Objects.equals(tag, "none")) {
			return false;
		}
		return tag!=null;
	}

    public void addMember(UUID new_member) {
        members.add(new_member);
    }

    public void removeMember(UUID new_member) {
        members.remove(new_member);
    }

    public boolean isMember(Player player) {
        for(UUID member : members)
            if(member.equals(player.getUniqueId()))
                return true;
        return false;
    }

	public boolean isSameVillage(Player p1, Player p2) {
		if (p1 == null || p2 == null) return false;

		UUID uuid1 = p1.getUniqueId();
		UUID uuid2 = p2.getUniqueId();

		return (members.contains(uuid1) && members.contains(uuid2))
				|| (uuid1.equals(owner) && (members.contains(uuid2) || uuid2.equals(owner)))
				|| (uuid2.equals(owner) && (members.contains(uuid1) || uuid1.equals(owner)));
	}


	public boolean isOwner(Player player) {
		return owner.equalsIgnoreCase(player.getName());
	}

	public boolean isOwner(UUID uuid) {
		return ownerUUID.toString().equalsIgnoreCase(uuid.toString());
	}

    public void addBank(int add) {
        bank = bank + add;
    }

    public void removeBank(int remove) {
        bank = bank - remove;
    }

    public String tpToString() {
        return "&eTeleport: &c" + teleport.getBlockX() + "&7&l-&c" + teleport.getBlockY() + "&7&l-&c" + teleport.getBlockZ();
    }

	public void upgrade() {
		setLevel(plugin.getLevelManager().getLevel(getLevel().getLevel() + 1));
	}

	public boolean isCentralBlock(Location loc) {
		return LocationUtils.isLocationMatching(loc, getLocation());
	}

    @Nullable
    public Village getVillageAt(Location loc) {
        boolean isVillage = false;
        int x = location.getBlockX();
        int z = location.getBlockZ();
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
        int x = location.getBlockX();
        int z = location.getBlockZ();
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

	public String effectsBuyedToString() {
		return isRegeneration() + ";" + isSpeed() + ";" + isJump() + ";" + isHaste() + ";";
	}

	public String effectsActiveToString() {
		return isRegenerationActive() + ";" + isSpeedActive() + ";" + isSpeedActive() + ";" + isHasteActive() + ";";
	}

	public String intsToString() {
		return getLevel().getLevel() + ";" + getLife() + ";" + getBank() + ";";
	}
}