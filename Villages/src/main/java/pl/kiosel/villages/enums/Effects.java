package pl.kiosel.villages.enums;

import lombok.Getter;
import pl.kiosel.dependencies.com.cryptomorin.xseries.XPotion;

public enum Effects {

	REGENERATION(XPotion.REGENERATION),
	SPEED(XPotion.SPEED),
	JUMP_BOOST(XPotion.JUMP_BOOST),
	HASTE(XPotion.HASTE);

	@Getter private final XPotion potion;

	Effects(XPotion potion) {
		this.potion = potion;
	}
}