package pl.kiosel.villages.gui.crafting;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.core.gui.CustomizableGui;
import pl.kiosel.core.gui.GuiUtils;
import pl.kiosel.dependencies.com.cryptomorin.xseries.XMaterial;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.enums.Lang;

public class GUICrafting extends CustomizableGui {

	private final AdvancedVillages plugin;
	private final Player player;

	public GUICrafting(AdvancedVillages plugin, Player player) {
		super(plugin, "crafting");
		this.plugin = plugin;
		this.player = player;

		setRows(3);
		setTitle("Crafting");
		openGui();
	}

	private void openGui() {
		ItemStack glass1 = GuiUtils.getBorderItem(XMaterial.WHITE_STAINED_GLASS_PANE);
		ItemStack glass2 = GuiUtils.getBorderItem(XMaterial.BLACK_STAINED_GLASS_PANE);

		setDefaultItem(glass1);

		mirrorFill("mirrorfill_1", 0, 0, true, true, glass2);
		mirrorFill("mirrorfill_2", 0, 1, true, true, glass2);
		mirrorFill("mirrorfill_3", 1, 0, false, true, glass2);

		setButton(1, 2, plugin.getApi().createVillageBlock(), (event) ->
				this.guiManager.showGUI(player, new GUICreateCrafting(player, this, plugin.getCraftingManager().getVillage())));
		setButton(1, 4, plugin.getApi().createDestroyer(), (event) ->
				this.guiManager.showGUI(player, new GUICreateCrafting(player, this, plugin.getCraftingManager().getDestroyer())));
		setButton(1, 6, plugin.getApi().createHearth(), (event) ->
				this.guiManager.showGUI(player, new GUICreateCrafting(player, this, plugin.getCraftingManager().getHearth())));

		setButton(2, 8, GuiUtils.createButtonItem(XMaterial.SPECTRAL_ARROW,
				plugin.getMessages().text(Lang.EXIT)), (event) -> player.closeInventory());
	}
}
