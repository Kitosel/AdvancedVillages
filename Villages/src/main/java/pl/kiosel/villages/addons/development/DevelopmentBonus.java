package pl.kiosel.villages.addons.development;

import lombok.Getter;

import java.util.Locale;

@Getter
public enum DevelopmentBonus {

	MAX_MEMBERS("max-members"),
	QUEST_BANK_PERCENT("quest-bank-percent"),
	WAR_SCORE_PERCENT("war-score-percent"),
	ATTACK_PROTECTION_PERCENT("attack-protection-percent"),
	TELEPORT_DELAY_REDUCTION_PERCENT("teleport-delay-reduction-percent"),
	EFFECT_COST_DISCOUNT_PERCENT("effect-cost-discount-percent");

	private final String configKey;

	DevelopmentBonus(String configKey) {
		this.configKey = configKey;
	}

	public static DevelopmentBonus fromConfigKey(String key) {
		if (key == null) return null;
		String normalized = key.trim().toLowerCase(Locale.ROOT);
		for (DevelopmentBonus bonus : values()) {
			if (bonus.configKey.equals(normalized)) return bonus;
		}
		return null;
	}
}
