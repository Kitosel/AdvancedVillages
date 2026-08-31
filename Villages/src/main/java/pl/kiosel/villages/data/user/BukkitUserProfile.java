package pl.kiosel.villages.data.user;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.Bukkit;
import pl.kiosel.rosacore.location.LocationUtils;
import pl.kiosel.rosacore.location.Position;
import pl.kiosel.rosacore.utils.TextUtils;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.UUID;

public class BukkitUserProfile implements UserProfile {

    private final UUID uuid;
    private WeakReference<OfflinePlayer> offlinePlayerRef;
    private WeakReference<Player> playerRef;

    public BukkitUserProfile(UUID uuid) {
        this.uuid = uuid;

		this.offlinePlayerRef = new WeakReference<>(Bukkit.getOfflinePlayer(uuid));
		this.playerRef = new WeakReference<>(Bukkit.getPlayer(uuid));
    }

    private Optional<Player> getPlayer() {
        Player player = this.playerRef.get();

        if (player == null) {
            this.refresh();
            player = this.playerRef.get();
        }

        return Optional.ofNullable(player);
    }

    private void refreshOfflinePlayerRef() {
        if (this.offlinePlayerRef.get() == null) {
			this.offlinePlayerRef = new WeakReference<>(Bukkit.getOfflinePlayer(this.uuid));
        }
    }

    @Override
    public boolean isOnline() {
        return this.getPlayer().map(Player::isOnline).orElse(false);
    }

    @Override
    public boolean isVanished() {
        return this.getPlayer()
                .map(player -> player.getMetadata("vanished"))
                .map(metadata -> metadata.stream().anyMatch(MetadataValue::asBoolean))
                .orElse(false);
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
		return this.getPlayer().map(Player::getPing).orElse(0);
    }

    @Override
    public void sendMessage(String message) {
        if (TextUtils.isEmpty(message)) {
            return;
        }

        this.getPlayer().ifPresent(player -> player.sendMessage(message));
    }

    @Override
    public void kick(String reason) {
        this.getPlayer().ifPresent(player -> player.kickPlayer(reason));
    }

    @Override
    public void teleport(Position position) {
        this.getPlayer().ifPresent(player -> player.teleport(LocationUtils.adapt(position)));
    }

    @Override
    public void refresh() {
		Player player = Bukkit.getPlayer(this.uuid);
        if (player != null) {
            this.playerRef = new WeakReference<>(player);
            this.offlinePlayerRef = new WeakReference<>(player);
        } else {
            this.playerRef = new WeakReference<>(null);
        }
    }

    @Override
    public Position getPosition() {
        return this.getPlayer()
                .map(Player::getLocation)
                .map(LocationUtils::adapt)
                .orElse(Position.ZERO);
    }

}
