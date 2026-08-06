package pl.kiosel.villages.addons.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.manager.VillageUtilsManager;

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
    public @NotNull String getVersion() { return "1.1"; }

    @Override
    public boolean canRegister() { return true; }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if(player == null) {
            return "";
        }

        Village village = plugin.getUserManager().findByUuid(player.getUniqueId()).get().getPresentVillage();
        if (village == null) {
            return "";
        }

        switch (params) {
			case "owner":
				return village.getOwner().getName();
            case "size":
                return village.getLevel().getSize() + "";
            case "life":
                return village.getLives() + "";
            case "name":
                return village.getName();
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