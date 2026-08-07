package pl.kiosel.villages.addons.spawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import pl.kiosel.core.chat.AdventureUtils;
import pl.kiosel.core.commands.SimpleCommand;
import pl.kiosel.core.dependencies.net.kyori.adventure.title.Title;
import pl.kiosel.core.utils.TabUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.enums.CommandLang;
import pl.kiosel.villages.enums.Lang;
import pl.kiosel.villages.settings.Settings;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CommandSpawn extends SimpleCommand {

	private final AdvancedVillages plugin;
	private final SpawnManager spawnManager;

	public CommandSpawn(AdvancedVillages plugin) {
		super(plugin, plugin.getCommandLang().getSpawnCommandName(), plugin.getCommandLang().getSpawnCommandAliases(),
				plugin.getCommandLang().getSpawnCommandPermission());
		this.plugin = plugin;
		this.spawnManager = plugin.getSpawnManager();
	}

	@Override
	public boolean onExecute(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			this.plugin.getLocale().getMessage(Lang.COMMAND_CONSOLE.getPath()).sendPrefixedMessage(sender);
			return true;
		}
		if (!Settings.ADDONS_SPAWN_ENABLE.getBoolean()) {
			this.plugin.getLocale().getMessage(Lang.COMMAND_ENABLED.getPath()).sendPrefixedMessage(sender);
			return true;
		}

		Player player = (Player) sender;
		if (args.length == 0) {
			return teleportToSpawn(player);
		}

		String setArgument = this.plugin.getCommandLang().getCommand(CommandLang.SET);
		if (args.length == 1 && args[0].equalsIgnoreCase(setArgument)) {
			if (!player.hasPermission(this.plugin.getCommandLang().getSpawnCommandSetPermission())) {
				this.plugin.getLocale().getMessage(Lang.COMMAND_NO_PERMISSION.getPath()).sendPrefixedMessage(sender);
				return true;
			}
			this.spawnManager.setSpawn(player.getLocation());
			this.plugin.getLocale().getMessage(Lang.SPAWN_SET.getPath()).sendPrefixedMessage(sender);
			return true;
		}

		return false;
	}

	private boolean teleportToSpawn(Player player) {
		Optional<Location> spawn = this.spawnManager.getSpawn();
		if (spawn.isEmpty() || this.spawnManager.isSpawnSet()) {
			this.plugin.getLocale().getMessage(Lang.SPAWN_NOT_SET.getPath()).sendPrefixedMessage(player);
			return false;
		}

		UUID playerId = player.getUniqueId();
		if (this.spawnManager.isTeleporting(playerId)
				|| this.plugin.getTeleportManager().isTeleporting(player)) {
			return false;
		}

		long remainingCooldown = this.spawnManager.getRemainingCooldownSeconds(playerId);
		if (remainingCooldown > 0L) {
			this.plugin.getLocale().getMessage(Lang.TELEPORT_COOLDOWN.getPath())
					.processPlaceholder("time", remainingCooldown)
					.sendPrefixedMessage(player);
			return false;
		}

		int price = this.spawnManager.getCost();
		if (price > 0 && (!plugin.getEconomy().hasBalance(player, price)
				|| !plugin.getEconomy().withdrawBalance(player, price))) {
			double more_money = price - plugin.getEconomy().getBalance(player);
			this.plugin.getLocale().getMessage(Lang.NO_MONEY.getPath()).processPlaceholder("money", more_money).sendPrefixedMessage(player);
			return false;
		}

		if (price > 0) {
			this.plugin.getLocale().getMessage(Lang.MONEY_REMOVE.getPath())
					.processPlaceholder("money", price)
					.sendPrefixedMessage(player);
		}

		long delaySeconds = this.spawnManager.getTeleportDelay();
		this.spawnManager.beginTeleport(player, price);
		if (this.spawnManager.isMessage()) {
			this.plugin.getLocale().getMessage(Lang.SPAWN_TELEPORT.getPath())
					.processPlaceholder("seconds", delaySeconds)
					.sendPrefixedMessage(player);
		}

		BukkitTask task;
		if (delaySeconds > 0L && this.spawnManager.isMessageTitle()
				&& this.spawnManager.isMessageTitleAnimated()) {
			task = createAnimatedTeleportTask(player, spawn.get(), delaySeconds);
		} else {
			if (delaySeconds > 0L && this.spawnManager.isMessageTitle()) {
				sendTitle(player, delaySeconds, (int) Math.min(Integer.MAX_VALUE, delaySeconds * 20L));
			}
			task = Bukkit.getScheduler().runTaskLater(
					this.plugin,
					() -> finishTeleport(player, spawn.get()),
					delaySeconds * 20L
			);
		}
		this.spawnManager.trackTeleportTask(playerId, task);
		return true;
	}

	private BukkitTask createAnimatedTeleportTask(Player player, Location destination, long delaySeconds) {
		return new BukkitRunnable() {
			private long secondsLeft = delaySeconds;

			@Override
			public void run() {
				if (!spawnManager.isTeleporting(player.getUniqueId())) {
					cancel();
					return;
				}
				if (this.secondsLeft <= 0L) {
					cancel();
					finishTeleport(player, destination);
					return;
				}

				sendTitle(player, this.secondsLeft, 20);
				this.secondsLeft--;
			}
		}.runTaskTimer(this.plugin, 0L, 20L);
	}

	private void finishTeleport(Player player, Location destination) {
		if (!this.spawnManager.isTeleporting(player.getUniqueId())) {
			return;
		}

		int chargedCost = this.spawnManager.completeTeleport(player);
		if (!player.isOnline() || !player.teleport(destination)) {
			int refunded = this.spawnManager.refundFailedTeleport(player, chargedCost);
			if (refunded > 0 && player.isOnline()) {
				this.plugin.getLocale().getMessage(Lang.MONEY_ADD.getPath())
						.processPlaceholder("money", refunded)
						.sendPrefixedMessage(player);
			}
		}
	}

	private void sendTitle(Player player, long secondsLeft, int stayTicks) {
		Title title = AdventureUtils.createTitle(
				this.plugin.getLocale().getMessage(Lang.SPAWN_TITLE.getPath())
						.processPlaceholder("seconds", secondsLeft).getMessage(),
				this.plugin.getLocale().getMessage(Lang.SPAWN_SUBTITLE.getPath())
						.processPlaceholder("seconds", secondsLeft).getMessage(),
				10, stayTicks, 10
		);
		AdventureUtils.sendTitle(title, player);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (!Settings.ADDONS_SPAWN_ENABLE.getBoolean() || args.length != 1
				|| !sender.hasPermission(this.plugin.getCommandLang().getSpawnCommandSetPermission())) {
			return TabUtils.returnEmpty();
		}
		return TabUtils.returnWith(args[0], Collections.singletonList(this.plugin.getCommandLang().getCommand(CommandLang.SET)));
	}
}
