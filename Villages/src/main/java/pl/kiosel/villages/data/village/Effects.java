package pl.kiosel.villages.data.village;

import lombok.Getter;
import pl.kiosel.rosacore.compatibility.ZPotionEffectType;

public enum Effects {

	REGENERATION(ZPotionEffectType.REGENERATION),
	SPEED(ZPotionEffectType.SPEED),
	JUMP_BOOST(ZPotionEffectType.JUMP_BOOST),
	HASTE(ZPotionEffectType.HASTE);

	@Getter private final ZPotionEffectType potion;

	Effects(ZPotionEffectType potion) {
		this.potion = potion;
	}
}