package pl.kiosel.villages.village;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import pl.kiosel.villages.enums.Permission;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VillageMember {

    @Getter private final UUID uuid;
	@Getter @Setter private String owner;
	@Getter @Setter private Village village;
	@Getter private final String name;
	@Getter private final List<Permission> permissions;

    public VillageMember(UUID uuid, Village village, List<Permission> permissions) {
		this.permissions = permissions;
        this.uuid = uuid;
        this.owner = village.getOwner();
		this.village = village;
		this.name = Bukkit.getOfflinePlayer(uuid).getName();
    }

	public VillageMember(String player, Village village, List<Permission> permissions) {
		this(Bukkit.getOfflinePlayer(player).getUniqueId(), village, permissions);
	}

	public VillageMember(OfflinePlayer player, Village village, List<Permission> permissions) {
		this(player.getUniqueId(), village, permissions);
	}

	public VillageMember(Player player, Village village, List<Permission> permissions) {
		this(player.getUniqueId(), village, permissions);
	}
}