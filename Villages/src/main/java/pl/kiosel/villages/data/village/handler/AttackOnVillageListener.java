package pl.kiosel.villages.data.village.handler;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.rosacore.compatibility.ZSound;
import pl.kiosel.rosacore.location.Cuboid;
import pl.kiosel.rosacore.material.ItemTag;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.features.diplomacy.DiplomacyAttackResult;
import pl.kiosel.villages.api.events.VillageListener;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.config.Settings;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.manager.VillageUtils;

import java.util.Objects;

public class AttackOnVillageListener extends VillageListener {

	private final AdvancedVillages plugin;
	private final VillageUtils villageUtils;

	public AttackOnVillageListener(AdvancedVillages plugin) {
		super(plugin);
		this.plugin = plugin;
		this.villageUtils = plugin.getVillageUtils();
	}

	private boolean isInEnabledWorld(Location loc) {
		if (loc == null || loc.getWorld() == null) return true;
		return villageUtils.isBlacklisted(loc.getWorld());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBreakTurret(BlockBreakEvent event) {
		Block block = event.getBlock();
		Location location = block.getLocation();

		if (isInEnabledWorld(location)) return;

		Player player = event.getPlayer();
		Village village = villageUtils.getVillageAt(location);
		if (village == null) return;

		Location vloc = village.getLocation().orElseThrow();
		Location max = villageUtils.getTurretMax(vloc);
		Location min = villageUtils.getTurretMin(vloc);
		Cuboid cuboid = new Cuboid(min, max);

		if (!cuboid.contains(location)) return;

		User user = getUser(player);
		ItemStack hand = player.getInventory().getItemInMainHand();

		if (!isSameType(block.getType(), Material.NOTE_BLOCK)) {
			event.setCancelled(true);
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
			return;
		}

		if (!village.isMember(user)) {
			event.setCancelled(true);
			if (ItemTag.has(hand, "villageDestroyer")) {
				event.setCancelled(true);
				if (user.getVillage().isEmpty()) {
					return;
				}
				Village attackerVillage = user.getPresentVillage();
				DiplomacyAttackResult diplomacyResult = plugin.getDiplomacyManager().canAttack(attackerVillage, village);
				if (diplomacyResult != DiplomacyAttackResult.ALLOWED) {
					if (diplomacyResult == DiplomacyAttackResult.ALLIED) {
						plugin.getVillageMessages().sendPrefixed(player, Lang.DIPLOMACY_ALLIANCE_CANNOT_ATTACK);
					} else if (diplomacyResult == DiplomacyAttackResult.ATTACKER_HAS_NO_VILLAGE) {
						plugin.getVillageMessages().sendPrefixed(player, Lang.DIPLOMACY_WAR_ATTACKER_NO_VILLAGE);
					} else if (diplomacyResult == DiplomacyAttackResult.WAR_PREPARING) {
						plugin.getVillageMessages().sendPrefixed(player, Lang.DIPLOMACY_WAR_PREPARING,
								"time", plugin.getVillageMessages().formatDuration(
										plugin.getDiplomacyManager().getPreparationRemaining(attackerVillage, village)));
					} else {
						plugin.getVillageMessages().sendPrefixed(player, Lang.DIPLOMACY_WAR_REQUIRED);
					}
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 0.0f);
					return;
				}
				if (!Settings.VILLAGE_ATTACK_WHEN_OFFLINE.getBoolean()
						&& village.getOnlineMembers().isEmpty()) {
					plugin.getVillageMessages().get(Lang.VILLAGE_PROTECTED_OFFLINE).sendPrefixed(player);
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 0.0f);
					return;
				}
				if (!village.canBeAttacked()) {
					plugin.getVillageMessages().get(Lang.VILLAGE_PROTECTED).sendPrefixed(player);
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 0.0f);
					return;
				}
				if (attackerVillage != null) {
					plugin.getDiplomacyManager().recordVillageLifeLost(attackerVillage, village);
				}
				Objects.requireNonNull(location.getWorld()).dropItem(location.add(0.5, 1, 0.5), plugin.getApi().createHearthPart(), item -> {
					item.setGlowing(true);
					item.setUnlimitedLifetime(true);
					item.setVelocity(location.getDirection().multiply(0).setY(0.5));
				});
				ZSound.ENTITY_GENERIC_EXPLODE.play(village.getCenter().orElseThrow(), 1f, 1f);
				plugin.getDebug().debug("Player " + player.getName() + " destroy central block of village " + village.getName());
				villageUtils.attackOnVillage(village, 1, player);
			}
			return;
		}

		event.setCancelled(true);
		ZSound.ENTITY_VILLAGER_NO.play(player, 1f, 1f);
	}
}
