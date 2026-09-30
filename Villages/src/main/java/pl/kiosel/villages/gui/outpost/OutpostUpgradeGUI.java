package pl.kiosel.villages.gui.outpost;

import org.bukkit.entity.Player;
import pl.kiosel.rosacore.gui.Gui;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.gui.GUIS;
import pl.kiosel.villages.data.outpost.Outpost;
import pl.kiosel.villages.gui.OutpostMenu;
import pl.kiosel.villages.gui.VillageGUIManager;

public class OutpostUpgradeGUI extends OutpostMenu {

	public OutpostUpgradeGUI(AdvancedVillages plugin, VillageGUIManager menus, Outpost outpost, Player viewer, Gui parent) {
		super(plugin, menus, outpost, viewer, GUIS.OUTPOST, parent);
		addBackButton();
	}
}
