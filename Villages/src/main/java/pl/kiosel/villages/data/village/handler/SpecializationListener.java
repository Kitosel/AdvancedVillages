package pl.kiosel.villages.data.village.handler;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.api.events.VillageListener;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.VillageSpecialization;
import pl.kiosel.villages.manager.SpecializationManager;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class SpecializationListener extends VillageListener {

	private static final Set<String> CROPS = Set.of(
			"WHEAT", "CARROTS", "POTATOES", "BEETROOTS", "NETHER_WART", "COCOA", "SWEET_BERRY_BUSH");

	private final SpecializationManager manager;

	public SpecializationListener(AdvancedVillages plugin) {
		super(plugin);
		this.manager = plugin.getSpecializationManager();
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onAttack(EntityDamageByEntityEvent event) {
		Player attacker = getAttacker(event.getDamager());
		User user = getUser(attacker);
		if (!this.manager.has(user, VillageSpecialization.WARRIOR)) return;
		event.setDamage(event.getDamage()
				* (1.0D + this.manager.getBonus(VillageSpecialization.WARRIOR) / 100.0D));
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onDefend(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		Player player = (Player) event.getEntity();
		User user = getUser(player);
		if (!this.manager.has(user, VillageSpecialization.DEFENDER)
				|| Objects.requireNonNull(user.getPresentVillage()).getRegion().filter(region -> region.isIn(player.getLocation())).isEmpty()) {
			return;
		}
		event.setDamage(event.getDamage()
				* (1.0D - this.manager.getBonus(VillageSpecialization.DEFENDER) / 100.0D));
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onHeal(EntityRegainHealthEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		User user = getUser((Player) event.getEntity());
		if (!this.manager.has(user, VillageSpecialization.HEALER)) return;
		event.setAmount(event.getAmount()
				* (1.0D + this.manager.getBonus(VillageSpecialization.HEALER) / 100.0D));
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBreak(BlockBreakEvent event) {
		User user = getUser(event.getPlayer());
		if (!this.manager.has(user, VillageSpecialization.MINER) || !isOre(event.getBlock().getType())) return;
		int experience = event.getExpToDrop();
		if (experience > 0) {
			event.setExpToDrop((int) Math.ceil(experience
					* (1.0D + this.manager.getBonus(VillageSpecialization.MINER) / 100.0D)));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDrops(BlockDropItemEvent event) {
		User user = getUser(event.getPlayer());
		if (!this.manager.has(user, VillageSpecialization.FARMER)
				|| !isMatureCrop(event.getBlockState().getType(), event.getBlockState())) return;
		if (ThreadLocalRandom.current().nextDouble(100.0D)
				>= this.manager.getBonus(VillageSpecialization.FARMER)) return;

		for (Item item : event.getItems()) {
			ItemStack extra = item.getItemStack().clone();
			if (!extra.getType().isAir() && extra.getAmount() > 0) {
				event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), extra);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemDamage(PlayerItemDamageEvent event) {
		User user = getUser(event.getPlayer());
		if (!this.manager.has(user, VillageSpecialization.FORESTER)
				|| !event.getItem().getType().name().endsWith("_AXE")) return;
		if (ThreadLocalRandom.current().nextDouble(100.0D)
				< this.manager.getBonus(VillageSpecialization.FORESTER)) {
			event.setCancelled(true);
		}
	}

	private static boolean isMatureCrop(Material material, BlockState state) {
		if (!CROPS.contains(material.name()) || !(state.getBlockData() instanceof Ageable)) return false;
		Ageable ageable = (Ageable) state.getBlockData();
		return ageable.getAge() >= ageable.getMaximumAge();
	}

	private static boolean isOre(Material material) {
		return material != null && (material.name().endsWith("_ORE") || material == Material.ANCIENT_DEBRIS);
	}
}
