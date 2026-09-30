package pl.kiosel.villages.gui.outpost;

import org.bukkit.entity.Player;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.config.gui.GUIS;
import pl.kiosel.villages.data.outpost.Outpost;
import pl.kiosel.villages.gui.OutpostMenu;
import pl.kiosel.villages.gui.VillageGUIManager;

public class OutpostGUI extends OutpostMenu {

	public OutpostGUI(AdvancedVillages plugin, VillageGUIManager menus, Outpost outpost, Player viewer) {
		super(plugin, menus, outpost, viewer, GUIS.OUTPOST, null);
	}
}
