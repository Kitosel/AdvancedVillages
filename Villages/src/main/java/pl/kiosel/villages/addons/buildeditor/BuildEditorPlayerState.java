package pl.kiosel.villages.addons.buildeditor;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

final class BuildEditorPlayerState {

    private final ItemStack[] storageContents;
    private final ItemStack[] armorContents;
    private final ItemStack[] extraContents;
    private final ItemStack cursorItem;
    private final int heldItemSlot;
    private final GameMode gameMode;
    private final boolean allowFlight;
    private final boolean flying;
    private final int level;
    private final float experience;
    private final int totalExperience;

    private BuildEditorPlayerState(Player player) {
        PlayerInventory inventory = player.getInventory();
        this.storageContents = cloneItems(inventory.getStorageContents());
        this.armorContents = cloneItems(inventory.getArmorContents());
        this.extraContents = cloneItems(inventory.getExtraContents());
        this.cursorItem = cloneItem(player.getItemOnCursor());
        this.heldItemSlot = inventory.getHeldItemSlot();
        this.gameMode = player.getGameMode();
        this.allowFlight = player.getAllowFlight();
        this.flying = player.isFlying();
        this.level = player.getLevel();
        this.experience = player.getExp();
        this.totalExperience = player.getTotalExperience();
    }

    static BuildEditorPlayerState capture(Player player) {
        return new BuildEditorPlayerState(player);
    }

    void prepareForEditing(Player player) {
        player.closeInventory();
        clearInventory(player);
        resetExperience(player);
        player.setGameMode(GameMode.CREATIVE);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.updateInventory();
    }

    void restore(Player player) {
        player.closeInventory();
        clearInventory(player);

        PlayerInventory inventory = player.getInventory();
        inventory.setStorageContents(cloneItems(storageContents));
        inventory.setArmorContents(cloneItems(armorContents));
        inventory.setExtraContents(cloneItems(extraContents));
        inventory.setHeldItemSlot(heldItemSlot);
        player.setItemOnCursor(cloneItem(cursorItem));

        resetExperience(player);
        player.setTotalExperience(totalExperience);
        player.setLevel(level);
        player.setExp(experience);

        player.setFlying(false);
        player.setGameMode(gameMode);
        player.setAllowFlight(allowFlight);
        if (allowFlight) {
            player.setFlying(flying);
        }
        player.updateInventory();
    }

    private static void clearInventory(Player player) {
        PlayerInventory inventory = player.getInventory();
        inventory.clear();
        inventory.setArmorContents(new ItemStack[inventory.getArmorContents().length]);
        inventory.setExtraContents(new ItemStack[inventory.getExtraContents().length]);
        player.setItemOnCursor(null);
    }

    private static void resetExperience(Player player) {
        player.setTotalExperience(0);
        player.setLevel(0);
        player.setExp(0.0F);
    }

    private static ItemStack[] cloneItems(ItemStack[] source) {
        ItemStack[] copy = new ItemStack[source.length];
        for (int index = 0; index < source.length; index++) {
            copy[index] = cloneItem(source[index]);
        }
        return copy;
    }

    private static ItemStack cloneItem(ItemStack item) {
        return item == null ? null : item.clone();
    }
}
