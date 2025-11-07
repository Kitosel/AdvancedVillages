package pl.kiosel.villages.manager.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.manager.VillageManager;

public class PlaceholderVillage extends PlaceholderExpansion {

    private final AdvancedVillages plugin;

    public PlaceholderVillage(AdvancedVillages plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() { return "villages"; }

    @Override
    public @NotNull String getAuthor() { return "Kiosel"; }

    @Override
    public @NotNull String getVersion() { return "1.0"; }

    @Override
    public boolean canRegister() { return true; }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if(player == null) {
            return "";
        }

        Village village = VillageManager.getVillageByOwner(player.getName());
        if (village == null) {
            return "";
        }

        switch (params) {
			case "owner":
				return village.getOwner();
			case "ownerUUID":
				return village.getOwnerUUID().toString();
            case "size":
                return village.getLevel().getSize() + "";
            case "life":
                return village.getLife() + "";
            case "name":
                return village.getVillageName();
			case "level":
				return village.getLevel().getLevel() + "";
			case "tag":
				return village.getTag();
			case "bank":
				return village.getBank() + "";
            default:
                return "";
        }
    }
}