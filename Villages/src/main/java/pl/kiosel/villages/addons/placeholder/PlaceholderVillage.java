package pl.kiosel.villages.addons.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;

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
    public @NotNull String getVersion() { return "1.2"; }

    @Override
    public boolean canRegister() { return true; }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if(player == null) {
            return "";
        }

		User user = plugin.getUserManager().findByUuid(player.getUniqueId()).orElse(null);
		if (user == null) {
			return "";
		}
        Village village = user.getPresentVillage();
        if (village == null) {
            return "";
        }

		switch (params) {
			case "role":
				return plugin.getRoleManager().getRole(user).getName();
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
			case "allies":
				return Integer.toString(plugin.getDiplomacyManager().getAllies(village).size());
			case "wars":
				return Integer.toString(plugin.getDiplomacyManager().countCurrentWars(village));
			case "upkeep_cost":
				return Integer.toString(plugin.getUpkeepManager().calculateCost(village));
			case "upkeep_time":
				return plugin.getVillageMessages().formatDuration(plugin.getUpkeepManager().getRemaining(village));
			case "upkeep_missed":
				return Integer.toString(plugin.getUpkeepManager().getMissedPayments(village));
            default:
                return "";
        }
    }
}
