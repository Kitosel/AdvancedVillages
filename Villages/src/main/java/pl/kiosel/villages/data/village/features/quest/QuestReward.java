package pl.kiosel.villages.data.village.features.quest;

import lombok.Getter;

@Getter
public final class QuestReward {

	private final int bank;
	private final int experience;
	private final int points;

	public QuestReward(int bank, int experience, int points) {
		this.bank = Math.max(0, bank);
		this.experience = Math.max(0, experience);
		this.points = Math.max(0, points);
	}

}
