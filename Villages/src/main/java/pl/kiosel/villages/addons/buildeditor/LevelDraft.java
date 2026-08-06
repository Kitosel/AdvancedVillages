package pl.kiosel.villages.addons.buildeditor;

import pl.kiosel.dependencies.com.cryptomorin.xseries.XMaterial;
import pl.kiosel.villages.data.village.level.Level;

import java.util.LinkedHashMap;
import java.util.Map;

final class LevelDraft {

	private final Map<XMaterial, Integer> materials = new LinkedHashMap<>();
	private int experience;
	private int economy;
	private int size = 1;

	static LevelDraft from(Level level) {
		LevelDraft draft = new LevelDraft();
		draft.materials.putAll(level.getMaterials());
		draft.experience = level.getCostExperience();
		draft.economy = level.getCostEconomy();
		draft.size = level.getSize();
		return draft;
	}

	Map<XMaterial, Integer> getMaterials() {
		return materials;
	}

	int getExperience() {
		return experience;
	}

	void setExperience(int experience) {
		this.experience = experience;
	}

	int getEconomy() {
		return economy;
	}

	void setEconomy(int economy) {
		this.economy = economy;
	}

	int getSize() {
		return size;
	}

	void setSize(int size) {
		this.size = size;
	}
}
