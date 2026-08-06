package pl.kiosel.villages.addons.antylogout.listener;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.addons.antylogout.CombatConfig;
import pl.kiosel.villages.addons.antylogout.CombatManager;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.settings.Settings;

import java.util.UUID;

public class CombatListener implements Listener {

	private final AdvancedVillages plugin;
	private final CombatManager combatManager;
	private final CombatConfig combatConfig;

	public CombatListener(AdvancedVillages plugin, CombatManager combatManager, CombatConfig combatConfig) {
		this.plugin = plugin;
		this.combatManager = combatManager;
		this.combatConfig = combatConfig;
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (!Settings.ADDONS_ANTYLOGOUT_ENABLE.getBoolean()) return;

		Entity victim = event.getEntity();
		Entity attacker = event.getDamager();
		if (this.combatConfig.isCombatFromProjectiles() && attacker instanceof Projectile) {
			Projectile projectile = (Projectile) attacker;
			if (projectile.getShooter() instanceof Player) {
				attacker = (Entity) projectile.getShooter();
			} else if (this.combatConfig.isCombatFromMobs() && projectile.getShooter() instanceof LivingEntity) {
				attacker = (Entity) projectile.getShooter();
			}
		}
		this.handleCombat(attacker, victim);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (!this.combatManager.getCombat(player.getUniqueId()).isPresent()) {
			return;
		}
		broadcastCombatQuit(player);
		if (!this.hasBypassPermission(player)) {
			player.setHealth(0.0);
		}
		this.combatManager.removeCombat(player.getUniqueId());
	}

	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String command = event.getMessage().split(" ")[0];
		if (!this.combatConfig.isCommandsBlockedDuringCombat() || this.hasBypassPermission(player) || this.combatConfig.getCombatCommandWhitelist().contains(command) || !this.combatManager.getCombat(player.getUniqueId()).isPresent()) {
			return;
		}
		event.setCancelled(true);
		plugin.getLocale().getMessage(Lang.ANTILOGOUT_BLOCK_COMMAND.getPath()).processPlaceholder("command", command).sendPrefixedMessage(player);
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		this.handleCombatRemoval(event.getEntity(), true);
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		this.handleCombatRemoval(event.getEntity(), false);
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		this.handleCombatRemoval(event.getEntity(), false);
	}

	private void handleCombat(Entity attacker, Entity victim) {
		if (!(attacker instanceof Player) && !(victim instanceof Player)) {
			return;
		}
		if (attacker == victim) {
			return;
		}
		this.sendCombatStartMessage(attacker, victim);
		if (this.combatConfig.isCombatFromMobs()) {
			if (attacker instanceof Player) {
				this.startCombat(attacker, victim);
			}
			if (victim instanceof Player) {
				this.startCombat(victim, attacker);
			}
			return;
		}
		if (attacker instanceof Player && victim instanceof Player) {
			this.startCombat(attacker, victim);
			this.startCombat(victim, attacker);
		}
	}

	private void startCombat(Entity attacker, Entity victim) {
		UUID attackerUUID = attacker.getUniqueId();
		UUID victimUUID = victim.getUniqueId();
		this.combatManager.addCombat(attackerUUID, victimUUID, this.combatConfig.getCombatDuration());
	}

	private void sendCombatStartMessage(Entity attacker, Entity victim) {
		if (!this.combatConfig.isCombatStartNotificationsEnabled()) {
			return;
		}

		boolean attackerIsPlayer = attacker instanceof Player;
		boolean victimIsPlayer = victim instanceof Player;
		if (!this.combatConfig.isCombatFromMobs() && !(attackerIsPlayer && victimIsPlayer)) {
			return;
		}

		if (attackerIsPlayer && !this.combatManager.getCombat(attacker.getUniqueId()).isPresent()) {
			this.combatConfig.getCombatStartMessageAttacker().send((Player) attacker, "victim", getEntityName(victim));
		}
		if (victimIsPlayer && !this.combatManager.getCombat(victim.getUniqueId()).isPresent()) {
			this.combatConfig.getCombatStartMessageVictim().send((Player) victim, "attacker", getEntityName(attacker));
		}
	}

	private void handleCombatRemoval(Entity entity, boolean isPlayer) {
		if (entity == null) {
			return;
		}
		UUID entityId = entity.getUniqueId();
		if (isPlayer) {
			this.combatManager.removeCombat(entityId);
		}
		if (!this.combatConfig.isRemoveCombatOnOpponentDeath()) {
			return;
		}
		this.combatManager.findPlayerByOpponent(entityId).ifPresent(playerId -> {
			this.combatManager.removeCombat(playerId);
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
			if (offlinePlayer.isOnline()) {
				Player player = (Player) offlinePlayer;
				this.combatConfig.getRemoveCombatMessage().forEach(message -> message.send(player, "opponent", entity.getName()));
			}
		});
	}

	private void broadcastCombatQuit(Player quiter) {
		if (this.combatConfig.isBroadcast())
			for (Player player : Bukkit.getOnlinePlayers()) {
				plugin.getLocale().getMessage(Lang.ANTILOGOUT_QUIT_BROADCAST.getPath()).processPlaceholder("player", quiter.getName()).sendMessage(player);
			}
	}

	private boolean hasBypassPermission(Player player) {
		if (!combatConfig.isBypass()) return false;
		return player.hasPermission(this.combatConfig.getCombatBypassPermission());
	}

	public String getEntityName(Entity entity) {
		if (entity instanceof Player) {
			return entity.getName();
		}
		if (entity instanceof LivingEntity) {
			LivingEntity livingEntity = (LivingEntity)entity;
			return (livingEntity.getCustomName() != null ? livingEntity.getCustomName() : livingEntity.getType().name().toLowerCase()).trim();
		}
		return "Unknown";
	}
}
