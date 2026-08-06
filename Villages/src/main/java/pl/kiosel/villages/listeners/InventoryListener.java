package pl.kiosel.villages.listeners;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import pl.kiosel.core.dependencies.de.tr7zw.nbtapi.NBT;
import pl.kiosel.core.dependencies.de.tr7zw.nbtapi.iface.ReadableItemNBT;
import pl.kiosel.core.hooks.EconomyManager;
import pl.kiosel.core.locale.Locale;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.enums.GUIS;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.enums.Permission;
import pl.kiosel.villages.enums.Upgrade;
import pl.kiosel.villages.events.VillageMemberRemoveEvent;
import pl.kiosel.villages.events.VillageRemoveEvent;
import pl.kiosel.villages.events.VillageUpgradeEvent;
import pl.kiosel.villages.manager.PermissionManager;
import pl.kiosel.villages.manager.TeleportManager;
import pl.kiosel.villages.settings.Settings;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public class InventoryListener implements Listener {

    private final AdvancedVillages plugin;
	private final PermissionManager permissionManager;
	private final Locale locale;

    public InventoryListener(AdvancedVillages plugin) {
        this.plugin = plugin;
		this.permissionManager = plugin.getPermissionManager();
		this.locale = plugin.getLocale();
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if(event.getClickedInventory() == null) return;
		/*
			MAIN INVENTORY
		 */
        if(event.getView().getTitle().equalsIgnoreCase(GUIS.VILLAGE.getName())) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            if (!(event.getWhoClicked() instanceof Player)) return;

            Player player = (Player) event.getWhoClicked();
			Village village = plugin.getUserManager().findByUuid(player.getUniqueId()).get().getPresentVillage();
			if (village == null) return;

            switch (event.getCurrentItem().getType()) {
				case NOTE_BLOCK:
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 0.1f, 2);
                    plugin.getVillageGui().openGui(village, player, GUIS.SETTINGS);
                    break;
                case PLAYER_HEAD:
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 0.1f, 2);
                    plugin.getVillageGui().openGui(village, player, GUIS.RESIDENT);
                    break;
                case SUNFLOWER:
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 0.1f, 2);
                    plugin.getVillageGui().openGui(village, player, GUIS.BANK);
                    break;
				case CHEST:
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 0.1f, 2);
					plugin.getVillageGui().openGui(village, player, GUIS.STORAGE);
					break;
				case NETHER_STAR:
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 0.1f, 2);
					plugin.getVillageGui().openGui(village, player, GUIS.STORE);
					break;
                case SPLASH_POTION:
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 0.1f, 2);
                    plugin.getVillageGui().openGui(village, player, GUIS.EFFECTS);
                    break;
                case DIAMOND:
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 0.1f, 2);
                    plugin.getVillageGui().openGui(village, player, GUIS.UPGRADE);
                    break;
				case ARROW:
					player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 0.1f, 2);
                    player.closeInventory();
                    break;
            }
        }
		/*
			EFFECTS INVENTORY
		 */
		if(event.getView().getTitle().equalsIgnoreCase(GUIS.EFFECTS.getName())) {
			event.setCancelled(true);
			if (event.getCurrentItem() == null) return;
			if (!(event.getWhoClicked() instanceof Player)) return;

			Player player = (Player) event.getWhoClicked();
			Village village = plugin.getUserManager().findByUuid(player.getUniqueId()).get().getPresentVillage();
			if (village == null) return;
			switch (event.getRawSlot()) {
				case 4:
					plugin.getVillageGui().openGui(village, player, GUIS.VILLAGE);
					player.playSound(player.getLocation(), Sound.UI_HUD_BUBBLE_POP, 0.5f, 2);
					break;
				case 19:
					if (isPermission(player, Permission.EFFECTS_TOGGLE)) return;
					if (!village.isRegeneration()) {
						locale.getMessage(Lang.EFFECT_NOT_BUY.getPath()).sendPrefixedMessage(player);
						player.closeInventory();
						return;
					}
					village.setRegenerationActive(!village.isRegenerationActive());
					player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.2f, 2);
					plugin.getVillageGui().openGui(village, player, GUIS.EFFECTS);
					break;
				case 21:
					if (isPermission(player, Permission.EFFECTS_TOGGLE)) return;
					if (!village.isSpeed()) {
						locale.getMessage(Lang.EFFECT_NOT_BUY.getPath()).sendPrefixedMessage(player);
						player.closeInventory();
						return;
					}
					village.setSpeedActive(!village.isSpeedActive());
					player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.2f, 2);
					plugin.getVillageGui().openGui(village, player, GUIS.EFFECTS);
					break;
				case 23:
					if (isPermission(player, Permission.EFFECTS_TOGGLE)) return;
					if (!village.isJump()) {
						locale.getMessage(Lang.EFFECT_NOT_BUY.getPath()).sendPrefixedMessage(player);
						player.closeInventory();
						return;
					}
					village.setJumpActive(!village.isJumpActive());
					player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.2f, 2);
					plugin.getVillageGui().openGui(village, player, GUIS.EFFECTS);
					break;
				case 25:
					if (isPermission(player, Permission.EFFECTS_TOGGLE)) return;
					if (!village.isHaste()) {
						locale.getMessage(Lang.EFFECT_NOT_BUY.getPath()).sendPrefixedMessage(player);
						player.closeInventory();
						return;
					}
					village.setHasteActive(!village.isHasteActive());
					player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.2f, 2);
					plugin.getVillageGui().openGui(village, player, GUIS.EFFECTS);
					break;
				case 28:
					if (isPermission(player, Permission.EFFECTS_BUY)) return;
					if (!village.isRegeneration()) {
						if (!hasMoneyForEffect(player, Settings.EFFECTS_REGENERATION_COST.getDouble())) return;
						village.setRegeneration(true);
						plugin.getVillageGui().openGui(village, player, GUIS.EFFECTS);
						player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10, 2);
					} else {
						player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, 10, 0);
					}
					break;
				case 30:
					if (isPermission(player, Permission.EFFECTS_BUY)) return;
					if (!village.isSpeed()) {
						if (!hasMoneyForEffect(player, Settings.EFFECTS_SPEED_COST.getDouble())) return;
						village.setSpeed(true);
						plugin.getVillageGui().openGui(village, player, GUIS.EFFECTS);
						player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10, 2);
					} else {
						player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, 10, 0);
					}
					break;
				case 32:
					if (isPermission(player, Permission.EFFECTS_BUY)) return;
					if (!village.isJump()) {
						if (!hasMoneyForEffect(player, Settings.EFFECTS_JUMP_BOOST_COST.getDouble())) return;
						village.setJump(true);
						plugin.getVillageGui().openGui(village, player, GUIS.EFFECTS);
						player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10, 2);
					} else {
						player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, 10, 0);
					}
					break;
				case 34:
					if (isPermission(player, Permission.EFFECTS_BUY)) return;
					if (!village.isHaste()) {
						if (!hasMoneyForEffect(player, Settings.EFFECTS_HASTE_COST.getDouble())) return;
						village.setHaste(true);
						plugin.getVillageGui().openGui(village, player, GUIS.EFFECTS);
						player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10, 2);
					} else {
						player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, 10, 0);
					}
					break;
			}
		}
		/*
			RESIDENT INVENTORY
		 */
		if(event.getView().getTitle().equalsIgnoreCase(GUIS.RESIDENT.getName())) {
			event.setCancelled(true);
			if (event.getCurrentItem() == null) return;
			if (!(event.getWhoClicked() instanceof Player)) return;

			Player player = (Player) event.getWhoClicked();
			Village village = plugin.getUserManager().findByUuid(player.getUniqueId()).get().getPresentVillage();
			if (village == null) return;
			if (event.getRawSlot() == 4) {
				plugin.getVillageGui().openGui(village, player, GUIS.VILLAGE);
				player.playSound(player.getLocation(), Sound.UI_HUD_BUBBLE_POP, 0.5f, 2);
				return;
			}
			if (isPermission(player, Permission.OWNER)) return;

			String player1 = NBT.get(event.getCurrentItem(), (Function<ReadableItemNBT, String>) nbt -> nbt.getString("player"));
			UUID uuid = NBT.get(event.getCurrentItem(), (Function<ReadableItemNBT, UUID>) nbt -> nbt.getUUID("uuid"));

			if (player1.equalsIgnoreCase(village.getOwner().getName())
					|| player1.equalsIgnoreCase(player.getName())) {
				locale.getMessage(Lang.CANT_EDIT.getPath()).sendPrefixedMessage(player);
				player.closeInventory();
				return;
			}
			plugin.getVillageGui().openMemberSettings(player, uuid);
		}
		/*
			MEMBER SETINGS INVENTORY
		 */
		if(event.getView().getTitle().equalsIgnoreCase(GUIS.MEMBER_SETTINGS.getName())) {
			event.setCancelled(true);
			if (event.getCurrentItem() == null) return;
			if (!(event.getWhoClicked() instanceof Player)) return;

			Player player = (Player) event.getWhoClicked();
			User user = plugin.getUserManager().findByUuid(player.getUniqueId()).get();
			Village village = user.getPresentVillage();
			if (village == null) return;

			UUID uuid = NBT.get(Objects.requireNonNull(
					event.getInventory().getItem(4)), (Function<ReadableItemNBT, UUID>) nbt -> nbt.getUUID("uuid"));
			User user2 = plugin.getUserManager().findByUuid(uuid).get();
			if (user2==null) {
				player.sendMessage("null");
				player.closeInventory();
				return;
			}
			switch (event.getRawSlot()) {
				case 8:
					plugin.getVillageGui().openGui(village, player, GUIS.VILLAGE);
					player.playSound(player.getLocation(), Sound.UI_HUD_BUBBLE_POP, 0.5f, 2);
					break;
				case 9:
					setPermission(user2, player, Permission.SETTINGS);
					break;
				case 10:
					setPermission(user2, player, Permission.INVITE);
					break;
				case 11:
					setPermission(user2, player, Permission.STORE);
					break;
				case 12:
					setPermission(user2, player, Permission.EFFECTS_BUY);
					break;
				case 13:
					setPermission(user2, player, Permission.EFFECTS_TOGGLE);
					break;
				case 14:
					setPermission(user2, player, Permission.UPGRADE);
					break;
				case 15:
					setPermission(user2, player, Permission.BANK_ADD);
					break;
				case 16:
					setPermission(user2, player, Permission.BANK_REMOVE);
					break;
				case 17:
					plugin.getVillageGui().openMemberRemove(player, uuid);
					break;
			}
		}
		/*
			BANK INVENTORY
		 */
		if(event.getView().getTitle().equalsIgnoreCase(GUIS.BANK.getName())) {
			event.setCancelled(true);
			if (event.getCurrentItem() == null) return;
			if (!(event.getWhoClicked() instanceof Player)) return;

			Player player = (Player) event.getWhoClicked();
			Village village = plugin.getUserManager().findByUuid(player.getUniqueId()).get().getPresentVillage();
			if (village == null) return;
			int money;
			switch (event.getRawSlot()) {
				case 4:
					plugin.getVillageGui().openGui(village, player, GUIS.VILLAGE);
					player.playSound(player.getLocation(), Sound.UI_HUD_BUBBLE_POP, 0.5f, 2);
					break;
				case 21:
					if (isPermission(player, Permission.BANK_ADD)) return;

					money = 5;
					addBank(village, player, money);
					break;
				case 22:
					if (isPermission(player, Permission.BANK_ADD)) return;

					money = 10;
					addBank(village, player, money);
					break;
				case 23:
					if (isPermission(player, Permission.BANK_ADD)) return;

					money = 100;
					addBank(village, player, money);
					break;
				case 30:
					if (isPermission(player, Permission.BANK_REMOVE)) return;

					money = 5;
					removeBank(village, player, money);
					break;
				case 31:
					if (isPermission(player, Permission.BANK_REMOVE)) return;

					money = 10;
					removeBank(village, player, money);
				case 32:
					if (isPermission(player, Permission.BANK_REMOVE)) return;

					money = 100;
					removeBank(village, player, money);
					break;
			}
		}
		/*
			STORE INVENTORY
		 */
		if(event.getView().getTitle().equalsIgnoreCase(GUIS.STORE.getName())) {
			event.setCancelled(true);
			if (event.getCurrentItem() == null) return;
			if (!(event.getWhoClicked() instanceof Player)) return;

			Player player = (Player) event.getWhoClicked();
			Village village = plugin.getUserManager().findByUuid(player.getUniqueId()).get().getPresentVillage();
			if (village == null) return;

			if (isPermission(player, Permission.STORE)) return;

			int money;
			switch (event.getRawSlot()) {
				case 4:
					plugin.getVillageGui().openGui(village, player, GUIS.VILLAGE);
					player.playSound(player.getLocation(), Sound.UI_HUD_BUBBLE_POP, 0.5f, 2);
					break;
				case 19:
					money = 240;
					if (EconomyManager.hasBalance(player, money)) {
						if (player.getInventory().firstEmpty() != -1) {
							EconomyManager.withdrawBalance(player, money);
							player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2, 2);
							locale.getMessage(Lang.MONEY_REMOVE.getPath()).processPlaceholder("money", money).sendPrefixedMessage(player);
							player.getInventory().addItem(plugin.getApi().createVillageHearth());
							player.closeInventory();
						} else {
							player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 2, 0);
							locale.getMessage(Lang.FULL_EQ.getPath()).sendPrefixedMessage(player);
							player.closeInventory();
						}
					} else {
						locale.getMessage(Lang.NO_MONEY.getPath()).sendPrefixedMessage(player);
					}
					break;
				case 21:
					money = 120;
					if (EconomyManager.hasBalance(player, money)) {
						if (player.getInventory().firstEmpty() != -1) {
							EconomyManager.withdrawBalance(player, money);
							player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2, 2);
							locale.getMessage(Lang.MONEY_REMOVE.getPath()).processPlaceholder("money", money).sendPrefixedMessage(player);
							player.getInventory().addItem(plugin.getApi().createDestroyerHearth());
							player.closeInventory();
						} else {
							player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 2, 0);
							locale.getMessage(Lang.FULL_EQ.getPath()).sendPrefixedMessage(player);
							player.closeInventory();
						}
					} else {
						locale.getMessage(Lang.NO_MONEY.getPath()).sendPrefixedMessage(player);
					}
					break;
			}
		}
		/*
			UPGRADE INVENTORY
		 */
		if(event.getView().getTitle().equalsIgnoreCase(GUIS.UPGRADE.getName())) {
			event.setCancelled(true);
			if (event.getCurrentItem() == null) return;
			if (!(event.getWhoClicked() instanceof Player)) return;

			Player player = (Player) event.getWhoClicked();
			Village village = plugin.getUserManager().findByUuid(player.getUniqueId()).get().getPresentVillage();
			if (village == null) return;

			if (isPermission(player, Permission.UPGRADE)) return;

			switch (event.getRawSlot()) {
				case 4:
					plugin.getVillageGui().openGui(village, player, GUIS.VILLAGE);
					player.playSound(player.getLocation(), Sound.UI_HUD_BUBBLE_POP, 0.5f, 2);
					break;
				case 13:
					switch (event.getCurrentItem().getType()) {
						case COAL:
						case GOLD_INGOT:
						case IRON_INGOT:
						case DIAMOND:
							VillageUpgradeEvent villageUpgradeEvent = new VillageUpgradeEvent(
									village, player,
									Upgrade.getByLevel(village.getLevel().getLevel()),
									Upgrade.getByLevel(village.getLevel().getLevel() + 1),
									village.getLevel().getCostEconomy());
							plugin.getServer().getPluginManager().callEvent(villageUpgradeEvent);
							if (villageUpgradeEvent.isCancelled()) return;
							if (!plugin.getUpgradeManager().canUpgrade(player, plugin.getLevelManager().getLevel(village.getLevel().getLevel() + 1))) return;

							locale.getMessage(Lang.VILLAGE_UPGRADE.getPath()).sendPrefixedMessage(player);
							plugin.getUpgradeManager().upgradeVillage(village);
							player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 1);
							player.closeInventory();
							break;
						case NETHERITE_INGOT:
							player.sendMessage("Max level");
							player.closeInventory();
							break;
					}
					break;
			}
		}
		/*
			SETTINGS INVENTORY
		 */
        if(event.getView().getTitle().equalsIgnoreCase(GUIS.SETTINGS.getName())) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            if (!(event.getWhoClicked() instanceof Player)) return;

            Player player = (Player) event.getWhoClicked();
			User user = plugin.getUserManager().findByUuid(player.getUniqueId()).get();
            Village village = user.getPresentVillage();
            if (village == null) return;

			if (event.getRawSlot()==4) {
				plugin.getVillageGui().openGui(village, player, GUIS.VILLAGE);
				player.playSound(player.getLocation(), Sound.UI_HUD_BUBBLE_POP, 0.5f, 2);
				return;
			}
			if (isPermission(player, Permission.SETTINGS)) return;

            switch (event.getRawSlot()) {
                case 19:
					TeleportManager teleportManager = plugin.getTeleportManager();
					player.closeInventory();
					if (!teleportManager.isTeleportTask(player)) {
						teleportManager.setTeleportTask(player);
						teleportManager.sendHoverSet(player);
					} else {
						teleportManager.removeTeleportTask(player);
						locale.getMessage(Lang.TELEPORT_SET_CANCEL.getPath()).sendPrefixedMessage(player);
					}
                    break;
                case 21:
					if (!village.isTag()) {
						plugin.getVillageGui().openGui(village, player, GUIS.TAG);
					} else {
						player.closeInventory();
						locale.getMessage(Lang.TAG_VILLAGE.getPath())
								.processPlaceholder("%TAG%", village.getTag()).sendPrefixedMessage(player);
					}
                    break;
				case 23:
					village.setTnt(!village.isTnt());
					player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.2f, 2);
					plugin.getVillageGui().openGui(village, player, GUIS.SETTINGS);
					break;
                case 25:
                    village.togglePvP();
					player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.2f, 2);
                    plugin.getVillageGui().openGui(village, player, GUIS.SETTINGS);
                    break;
                case 31:
					if (village.isOwner(user)) {
						plugin.getVillageGui().openGui(village, player, GUIS.REMOVE);
					} else {
						player.closeInventory();
						locale.getMessage(Lang.VILLAGE_NO_PERMISSION.getPath()).sendPrefixedMessage(player);
						player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_COW_BELL, 2, 0);
					}
                    break;
            }
        }
		/*
			REMOVE MEMBER INVENTORY
		 */
		if(event.getView().getTitle().equalsIgnoreCase(GUIS.DELETE_MEMBER.getName())) {
			event.setCancelled(true);
			if (event.getCurrentItem() == null) return;
			if (!(event.getWhoClicked() instanceof Player)) return;

			Player player = (Player) event.getWhoClicked();
			Village village = plugin.getUserManager().findByUuid(player.getUniqueId()).get().getPresentVillage();
			if (village == null) return;

			UUID uuid = NBT.get(Objects.requireNonNull(
					event.getInventory().getItem(13)), (Function<ReadableItemNBT, UUID>) nbt -> nbt.getUUID("uuid"));
			User user2 = plugin.getUserManager().findByUuid(uuid).get();
			if (user2==null) {
				player.sendMessage("null");
				player.closeInventory();
				return;
			}

			switch (event.getRawSlot()) {
				case 12:
					VillageMemberRemoveEvent villageMemberRemoveEvent = new VillageMemberRemoveEvent(village, player);
					plugin.getServer().getPluginManager().callEvent(villageMemberRemoveEvent);
					if (villageMemberRemoveEvent.isCancelled()) return;
					user2.getPresentVillage().removeMember(user2);
					user2.removeVillage();
					locale.getMessage(Lang.VILLAGE_REMOVE.getPath()).sendPrefixedMessage(player);
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_FLUTE, 2, 0);
					player.closeInventory();
					break;
				case 14:
					player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_HIT, 2, 0);
					player.closeInventory();
					break;
			}
		}
		/*
			REMOVE VILLAGE INVENTORY
		 */
        if(event.getView().getTitle().equalsIgnoreCase(GUIS.REMOVE.getName())) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            if (!(event.getWhoClicked() instanceof Player)) return;

            Player player = (Player) event.getWhoClicked();
            Village village = plugin.getUserManager().findByUuid(player.getUniqueId()).get().getPresentVillage();
            if (village == null) return;

            switch (event.getRawSlot()) {
                case 12:
					VillageRemoveEvent villageRemoveEvent = new VillageRemoveEvent(village, player);
					plugin.getServer().getPluginManager().callEvent(villageRemoveEvent);
					if (villageRemoveEvent.isCancelled()) return;
					player.closeInventory();
					EconomyManager.deposit(player, village.getBank());
					plugin.getVillageRemoveManager().removeVillage(village, true);
					locale.getMessage(Lang.VILLAGE_REMOVE.getPath()).sendPrefixedMessage(player);
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_FLUTE, 2, 0);
					break;
                case 14:
                    player.closeInventory();
					player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_HIT, 2, 0);
                    break;
            }
        }
    }

	private void addBank(Village village, Player player, int money) {
		if (EconomyManager.hasBalance(player, money)) {
			village.addBank(money);
			EconomyManager.withdrawBalance(player, money);
			plugin.getVillageGui().openGui(village, player, GUIS.BANK);
			locale.getMessage(Lang.BANK_ADD.getPath()).processPlaceholder("money", money).sendPrefixedMessage(player);
			locale.getMessage(Lang.MONEY_REMOVE.getPath()).processPlaceholder("money", money).sendPrefixedMessage(player);
		} else {
			locale.getMessage(Lang.NO_MONEY.getPath()).sendPrefixedMessage(player);
		}
	}

	private void removeBank(Village village, Player player, int money) {
		if (village.getBank()>=money) {
			village.removeBank(money);
			EconomyManager.deposit(player, money);
			plugin.getVillageGui().openGui(village, player, GUIS.BANK);
			locale.getMessage(Lang.BANK_REMOVE.getPath()).processPlaceholder("money", money).sendPrefixedMessage(player);
			locale.getMessage(Lang.MONEY_ADD.getPath()).processPlaceholder("money", money).sendPrefixedMessage(player);
		} else {
			locale.getMessage(Lang.BANK_NO_MONEY.getPath()).sendPrefixedMessage(player);
		}
	}

	private boolean hasMoneyForEffect(Player player, double cost) {
		if (!EconomyManager.hasBalance(player, cost)) {
			locale.getMessage(Lang.NO_MONEY.getPath()).sendPrefixedMessage(player);
			return false;
		}
		EconomyManager.withdrawBalance(player, cost);
		return true;
	}

	private void setPermission(User member, Player player, Permission permission) {
		if (permissionManager.hasPermission(member, permission)) {
			plugin.getPermissionManager().removePermission(member, permission);
		} else {
			plugin.getPermissionManager().addPermission(member, permission);
		}
		plugin.getVillageGui().openMemberSettings(player, member.getUUID());
	}

	private boolean isPermission(Player player, Permission permission) {
		if (!permissionManager.hasPermission(player.getUniqueId(), permission)) {
			locale.getMessage(Lang.VILLAGE_NO_PERMISSION.getPath()).sendPrefixedMessage(player);
			return true;
		}
		return false;
	}
}