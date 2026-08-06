package pl.kiosel.villages.data.user;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import panda.std.Option;
import pl.kiosel.core.MetaServer;
import pl.kiosel.core.nms.NmsUtils;
import pl.kiosel.core.utils.LocationUtils;
import pl.kiosel.core.utils.Position;
import pl.kiosel.core.utils.TextUtils;

import java.lang.ref.WeakReference;
import java.util.UUID;

public class BukkitUserProfile implements UserProfile {

    private final UUID uuid;
    private final MetaServer metaServer;

    private WeakReference<OfflinePlayer> offlinePlayerRef;
    private WeakReference<Player> playerRef;

    public BukkitUserProfile(UUID uuid, MetaServer metaServer) {
        this.uuid = uuid;
        this.metaServer = metaServer;

        this.offlinePlayerRef = new WeakReference<>(metaServer.getOfflinePlayer(uuid));
        this.playerRef = new WeakReference<>(metaServer.getPlayer(uuid).orNull());
    }

    private Option<Player> getPlayer() {
        Player player = this.playerRef.get();

        if (player == null) {
            this.refresh();
            player = this.playerRef.get();
        }

        return Option.of(player);
    }

    private void refreshOfflinePlayerRef() {
        if (this.offlinePlayerRef.get() == null) {
            this.offlinePlayerRef = new WeakReference<>(this.metaServer.getOfflinePlayer(this.uuid));
        }
    }

    @Override
    public boolean isOnline() {
        return this.getPlayer().is(Player::isOnline);
    }

    @Override
    public boolean isVanished() {
        // Should work with VanishNoPacket, SuperVanish and PremiumVanish
        return this.getPlayer()
                .map(player -> player.getMetadata("vanished"))
                .map(metadata -> metadata.stream().anyMatch(MetadataValue::asBoolean))
                .orElseGet(false);
    }

    @Override
    public boolean hasPermission(String permission) {
        Player player = this.playerRef.get();
        if (player != null) {
            return player.hasPermission(permission);
        }

        this.refreshOfflinePlayerRef();
        OfflinePlayer offlinePlayer = this.offlinePlayerRef.get();

        return offlinePlayer.isOp();
    }

    @Override
    public int getPing() {
        return this.getPlayer().map(NmsUtils::getPing).orElseGet(0);
    }

    @Override
    public void sendMessage(String message) {
        if (TextUtils.isEmpty(message)) {
            return;
        }

        this.getPlayer().peek(player -> player.sendMessage(message));
    }

    @Override
    public void kick(String reason) {
        this.getPlayer().peek(player -> player.kickPlayer(reason));
    }

    @Override
    public void teleport(Position position) {
        this.getPlayer().peek(player -> player.teleport(LocationUtils.adapt(position)));
    }

    @Override
    public void refresh() {
        this.metaServer.getPlayer(this.uuid).peek(player -> {
            this.playerRef = new WeakReference<>(player);
            this.offlinePlayerRef = new WeakReference<>(player);
        }).onEmpty(() -> {
            this.playerRef = new WeakReference<>(null);
        });
    }

    @Override
    public Position getPosition() {
        return this.getPlayer()
                .map(Player::getLocation)
                .map(LocationUtils::adapt)
                .orElseGet(Position.ZERO);
    }

}
