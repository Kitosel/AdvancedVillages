package pl.kiosel.villages.addons.buildeditor;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.*;
import pl.kiosel.villages.enums.Lang;

public final class BuildEditorListener implements Listener {

    private final VillageBuildEditorManager manager;

    public BuildEditorListener(VillageBuildEditorManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (canModify(event.getPlayer(), event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();
        BuildEditorSession ownSession = manager.getSession(player.getUniqueId());
        if (ownSession != null && isSameBlock(ownSession.getOrigin(), location)) {
            event.setCancelled(true);
            manager.sendLocalized(player, Lang.BUILD_EDITOR_CORE_PROTECTED);
            return;
        }
        if (canModify(player, location)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDropItem(PlayerDropItemEvent event) {
        if (manager.hasSession(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        if (manager.hasSession(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Block target = event.getBlockClicked().getRelative(event.getBlockFace());
        if (canModify(event.getPlayer(), target.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        manager.cancelOnQuit(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKick(PlayerKickEvent event) {
        manager.cancelOnQuit(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!manager.hasConversation(event.getPlayer())) {
            return;
        }
        event.setCancelled(true);
        manager.handleChatInput(event.getPlayer(), event.getMessage());
    }

    private boolean canModify(Player player, Location location) {
        BuildEditorSession ownSession = manager.getSession(player.getUniqueId());
        if (ownSession != null) {
            if (!ownSession.getBounds().contains(location)) {
                manager.sendLocalized(player, Lang.BUILD_EDITOR_OUTSIDE_BOUNDS);
                return true;
            }
            return false;
        }

        BuildEditorSession foreignSession = manager.findSessionAt(location);
        if (foreignSession != null) {
            manager.sendLocalized(player, Lang.BUILD_EDITOR_AREA_IN_USE);
            return true;
        }
        return false;
    }

    private boolean isSameBlock(Location first, Location second) {
        return first.getWorld() != null && first.getWorld().equals(second.getWorld())
                && first.getBlockX() == second.getBlockX()
                && first.getBlockY() == second.getBlockY()
                && first.getBlockZ() == second.getBlockZ();
    }
}
