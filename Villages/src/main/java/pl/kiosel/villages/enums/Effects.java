package pl.kiosel.villages.enums;

import lombok.Getter;
import org.bukkit.potion.PotionEffectType;

public enum Effects {

	REGENERATION(PotionEffectType.REGENERATION),
	SPEED(PotionEffectType.SPEED),
	JUMP_BOOST(PotionEffectType.JUMP_BOOST),
	HASTE(PotionEffectType.HASTE);

	@Getter private final PotionEffectType potionEffectType;

	Effects(PotionEffectType potionEffectType) {
		this.potionEffectType = potionEffectType;
	}
}