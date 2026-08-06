package pl.kiosel.villages.addons.antylogout;

import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Getter
public class Combat {

	private final UUID opponent;
	private final long duration;
	private final Instant startTime;

	public Combat(UUID opponent, long duration) {
		this.opponent = opponent;
		this.duration = duration;
		this.startTime = Instant.now();
	}

	public long getElapsedSeconds(Instant now) {
		return Duration.between(this.startTime, now).getSeconds();
	}

	public long getTimeLeft(Instant now) {
		long elapsedSeconds = this.getElapsedSeconds(now);
		return this.duration - elapsedSeconds;
	}

	public long getTimeLeft() {
		return this.getTimeLeft(Instant.now());
	}

	public boolean hasElapsed(Instant now) {
		long elapsedSeconds = this.getElapsedSeconds(now);
		return elapsedSeconds >= this.duration;
	}

}
