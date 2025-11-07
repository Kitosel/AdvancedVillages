package pl.kiosel.villages.addons.tablist.services.service;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import pl.kiosel.core.nms.Nms;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.tablist.placeholders.BasicPlaceholders;
import pl.kiosel.villages.addons.tablist.placeholders.StaticPlaceholdersService;

public class BasicPlaceholdersService extends StaticPlaceholdersService<Object, BasicPlaceholders<Object>> {

    public static final BasicPlaceholders<String> ONLINE = new BasicPlaceholders<String>()
            .property("<online>", () -> ChatColor.GREEN)
            .property("</online>", end -> end);

    public static BasicPlaceholders<Object> createSimplePlaceholders(AdvancedVillages plugin) {
        return new BasicPlaceholders<>()
                .property("tps", Nms.getImplementations().getServer()::getTpsInLastMinute)
                .property("players", Bukkit.getOnlinePlayers()::size)
                .property("villages", plugin.getVillageDataManager()::countVillages);
    }
}