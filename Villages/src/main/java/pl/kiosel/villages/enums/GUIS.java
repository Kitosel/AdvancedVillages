package pl.kiosel.villages.enums;

import lombok.Getter;
import pl.kiosel.villages.config.GuiConfig;

public enum GUIS {

    VILLAGE(GuiConfig.gui_village, 36),
    BANK(GuiConfig.gui_bank, 36),
    STORE(GuiConfig.gui_store, 36),
    RESIDENT(GuiConfig.gui_resident, 36),
    REMOVE(GuiConfig.gui_remove, 27),
    UPGRADE(GuiConfig.gui_upgrade, 36),
    SETTINGS(GuiConfig.gui_settings, 36),
	EFFECTS(GuiConfig.gui_effects, 36),
	STORAGE(GuiConfig.gui_storage, 54),
	TAG("Name your village", 0),
    DELETE_MEMBER(GuiConfig.gui_remove_member, 18),
	MEMBER_SETTINGS(GuiConfig.gui_member_settings, 18);

    @Getter private final String name;
    @Getter private final int size;

    GUIS(String name, int size) {
        this.name = name;
        this.size = size;
    }
}