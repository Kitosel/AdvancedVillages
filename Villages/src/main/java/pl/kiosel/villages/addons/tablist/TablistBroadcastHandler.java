package pl.kiosel.villages.addons.tablist;

import pl.kiosel.villages.AdvancedVillages;

public class TablistBroadcastHandler implements Runnable {

    private final AdvancedVillages plugin;

    public TablistBroadcastHandler(AdvancedVillages plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
//        UserManager userManager = this.plugin.getDatabaseUserManager();
//        TablistConfiguration tablistConfig = this.plugin.getTablistConfiguration();
//
//        if (!tablistConfig.enabled) {
//            return;
//        }
//
//        // Don't remove this toArray - iterating over online players asynchronously without shallow copy could occur with ConcurrentModificationException (See GH-2031).
//        PandaStream.of(Bukkit.getOnlinePlayers().toArray(new Player[0]))
//                .flatMap(player -> userManager.findByUuid(player.getUniqueId()))
//                .flatMap(user -> user.getCache().getPlayerList())
//                .forEach(playerList -> {
//                    playerList.updatePageCycle();
//                    playerList.send();
//                });
    }
}
