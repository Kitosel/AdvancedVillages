package pl.kiosel.villages.data.user;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.Nullable;
import panda.std.Option;

public class UserCache {

    private final User user;

    private Option<Scoreboard> scoreboard = Option.none();

    @Setter
	@Getter
	private BukkitTask teleportation;
    @Setter
	@Getter
	private long notificationTime;
    @Getter
	private boolean spy;

    public UserCache(User user) {
        this.user = user;
    }

	public synchronized Option<Scoreboard> getScoreboard() {
        return this.scoreboard;
    }

    public synchronized void setScoreboard(@Nullable Scoreboard scoreboard) {
        this.scoreboard = Option.of(scoreboard);
    }

	public boolean toggleSpy() {
        this.spy = !this.spy;
        return this.spy;
    }

}
