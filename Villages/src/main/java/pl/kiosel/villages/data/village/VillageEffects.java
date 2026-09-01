package pl.kiosel.villages.data.village;

import java.util.Objects;

final class VillageEffects {

	private final Runnable changeListener;
	private volatile boolean regeneration;
	private volatile boolean speed;
	private volatile boolean jump;
	private volatile boolean haste;
	private volatile boolean regenerationActive;
	private volatile boolean speedActive;
	private volatile boolean jumpActive;
	private volatile boolean hasteActive;

	VillageEffects(Runnable changeListener) {
		this.changeListener = Objects.requireNonNull(changeListener, "changeListener");
	}

	boolean isRegeneration() {
		return this.regeneration;
	}

	void setRegeneration(boolean regeneration) {
		if (this.regeneration == regeneration) return;
		this.regeneration = regeneration;
		this.changeListener.run();
	}

	boolean isSpeed() {
		return this.speed;
	}

	void setSpeed(boolean speed) {
		if (this.speed == speed) return;
		this.speed = speed;
		this.changeListener.run();
	}

	boolean isJump() {
		return this.jump;
	}

	void setJump(boolean jump) {
		if (this.jump == jump) return;
		this.jump = jump;
		this.changeListener.run();
	}

	boolean isHaste() {
		return this.haste;
	}

	void setHaste(boolean haste) {
		if (this.haste == haste) return;
		this.haste = haste;
		this.changeListener.run();
	}

	boolean isRegenerationActive() {
		return this.regenerationActive;
	}

	void setRegenerationActive(boolean regenerationActive) {
		if (this.regenerationActive == regenerationActive) return;
		this.regenerationActive = regenerationActive;
		this.changeListener.run();
	}

	boolean isSpeedActive() {
		return this.speedActive;
	}

	void setSpeedActive(boolean speedActive) {
		if (this.speedActive == speedActive) return;
		this.speedActive = speedActive;
		this.changeListener.run();
	}

	boolean isJumpActive() {
		return this.jumpActive;
	}

	void setJumpActive(boolean jumpActive) {
		if (this.jumpActive == jumpActive) return;
		this.jumpActive = jumpActive;
		this.changeListener.run();
	}

	boolean isHasteActive() {
		return this.hasteActive;
	}

	void setHasteActive(boolean hasteActive) {
		if (this.hasteActive == hasteActive) return;
		this.hasteActive = hasteActive;
		this.changeListener.run();
	}

	String purchasedToString() {
		return this.regeneration + ";" + this.speed + ";" + this.jump + ";" + this.haste + ";";
	}

	String activeToString() {
		return this.regenerationActive + ";" + this.speedActive + ";" + this.jumpActive + ";" + this.hasteActive + ";";
	}
}
