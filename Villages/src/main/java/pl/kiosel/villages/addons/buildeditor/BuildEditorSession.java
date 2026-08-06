package pl.kiosel.villages.addons.buildeditor;

import org.bukkit.Location;

import java.util.UUID;

final class BuildEditorSession {

    private final UUID playerId;
    private final int level;
    private final Location origin;
    private final Location returnLocation;
    private final BuildEditorBounds bounds;
    private final BuildEditorBounds cleanupBounds;
    private final BuildEditorPlayerState playerState;
	private final boolean newLevel;

    BuildEditorSession(UUID playerId, int level, Location origin, Location returnLocation,
                       BuildEditorBounds bounds, BuildEditorBounds cleanupBounds,
                       BuildEditorPlayerState playerState, boolean newLevel) {
        this.playerId = playerId;
        this.level = level;
        this.origin = origin.clone();
        this.returnLocation = returnLocation.clone();
        this.bounds = bounds;
        this.cleanupBounds = cleanupBounds;
        this.playerState = playerState;
		this.newLevel = newLevel;
    }

    UUID getPlayerId() {
        return playerId;
    }

    int getLevel() {
        return level;
    }

    Location getOrigin() {
        return origin.clone();
    }

    Location getReturnLocation() {
        return returnLocation.clone();
    }

    BuildEditorBounds getBounds() {
        return bounds;
    }

    BuildEditorBounds getCleanupBounds() {
        return cleanupBounds;
    }

    BuildEditorPlayerState getPlayerState() {
        return playerState;
    }

	boolean isNewLevel() {
		return newLevel;
	}
}
