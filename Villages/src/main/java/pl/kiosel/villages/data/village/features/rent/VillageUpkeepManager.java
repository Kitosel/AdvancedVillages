package pl.kiosel.villages.data.village.features.rent;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.village.features.logs.VillageLogType;
import pl.kiosel.villages.config.Lang;
import pl.kiosel.villages.data.village.Village;
import pl.kiosel.villages.storage.UpkeepStorage;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class VillageUpkeepManager {
	private final AdvancedVillages plugin;
	private final RentConfiguration configuration;
	private final UpkeepStorage storage;
	private final Map<UUID, VillageUpkeepState> states = new ConcurrentHashMap<>();
	private volatile RentSettings settings;
	private volatile BukkitTask task;

	public VillageUpkeepManager(AdvancedVillages plugin, RentConfiguration configuration) {
		this.plugin = plugin;
		this.configuration = configuration;
		this.storage = new UpkeepStorage(plugin);
		this.settings = configuration.snapshot();
	}

	public void load() {
		if (!this.plugin.isDev()) return;
		this.states.clear();
		this.storage.load(state -> {
			if (this.plugin.getVillageManager().findByUuid(state.getVillageId()).isPresent()) {
				this.states.put(state.getVillageId(), state);
			}
		});
		Instant now = Instant.now();
		for (Village village : this.plugin.getVillageManager().getVillages()) {
			this.states.computeIfAbsent(village.getUUID(), ignored ->
					new VillageUpkeepState(village.getUUID(), now.plus(this.settings.getInterval()), 0));
		}
	}

	public synchronized void start() {
		this.shutdown();
		if (!this.isEnabled()) return;
		this.task = Bukkit.getScheduler().runTaskTimer(this.plugin, this::processDuePayments,
				20L * 30L, 20L * 60L);
	}

	public synchronized void shutdown() {
		if (this.task != null) {
			this.task.cancel();
			this.task = null;
		}
	}

	public void reload() {
		this.configuration.reload();
		this.settings = this.configuration.snapshot();
		if (this.plugin.isDataReady()) this.start();
	}

	public boolean isEnabled() {
		return this.plugin.isDev() && this.settings.isEnabled();
	}

	public int calculateCost(Village village) {
		if (village == null) return 0;
		RentSettings current = this.settings;
		long level = village.getLevel() == null ? 1L : Math.max(1, village.getLevel().getLevel());
		long members = village.getMembers().size();
		long radius = village.getRegion().map(region -> (long) region.getSize())
				.orElseGet(() -> village.getLevel() == null ? 1L : (long) village.getLevel().getSize());
		radius = Math.min(1_000_000L, Math.max(0L, radius));
		long side = Math.max(1L, radius * 2L + 1L);
		long area = side * side;
		long regionUnits = (area + current.getRegionBlockUnit() - 1L) / current.getRegionBlockUnit();
		long result = current.getBaseCost()
				+ level * current.getCostPerLevel()
				+ members * current.getCostPerMember()
				+ regionUnits * current.getCostPerRegionUnit();
		return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, result));
	}

	public Instant getNextPayment(Village village) {
		return this.state(village).getNextPayment();
	}

	public int getMissedPayments(Village village) {
		return this.state(village).getMissedPayments();
	}

	public Duration getRemaining(Village village) {
		Duration remaining = Duration.between(Instant.now(), this.getNextPayment(village));
		return remaining.isNegative() ? Duration.ZERO : remaining;
	}

	public void save(boolean ignoreUnchanged) {
		if (!this.plugin.isDev()) return;
		for (VillageUpkeepState state : this.states.values()) {
			if (ignoreUnchanged && !state.wasChanged()) continue;
			try {
				this.storage.save(state);
			} catch (RuntimeException exception) {
				this.plugin.getRosaLogger().log(Level.SEVERE,
						"Could not save upkeep state for village " + state.getVillageId(), exception);
			}
		}
	}

	public void delete(Village village) {
		if (!this.plugin.isDev() || village == null) return;
		this.states.remove(village.getUUID());
		this.storage.delete(village.getUUID());
	}

	private void processDuePayments() {
		if (!this.isEnabled()) return;
		Instant now = Instant.now();
		for (Village village : this.plugin.getVillageManager().getVillages()) {
			VillageUpkeepState state = this.state(village);
			if (!state.getNextPayment().isAfter(now)) this.charge(village, state, now);
		}
	}

	private void charge(Village village, VillageUpkeepState state, Instant now) {
		if (!this.isEnabled()) return;
		int cost = this.calculateCost(village);
		int missed = state.getMissedPayments();
		if (village.getBank() >= cost) {
			village.removeBank(cost);
			missed = 0;
			this.plugin.getLogManager().recordSystem(village, VillageLogType.UPKEEP_PAID,
					"amount", cost);
			village.broadcast(this.plugin.getVillageMessages().prefixedText(Lang.UPKEEP_PAID,
					"cost", cost));
		} else {
			missed++;
			this.plugin.getLogManager().recordSystem(village, VillageLogType.UPKEEP_MISSED,
					"amount", cost, "missing", Math.max(0, cost - village.getBank()), "missed", missed);
			village.broadcast(this.plugin.getVillageMessages().prefixedText(Lang.UPKEEP_MISSED,
					"cost", cost, "missing", Math.max(0, cost - village.getBank()), "missed", missed));
			if (this.settings.getLifePenalty() > 0 && missed >= this.settings.getMissedBeforePenalty()) {
				int lost = Math.min(village.getLives(), this.settings.getLifePenalty());
				village.updateLives(value -> value - lost);
				missed = 0;
				this.plugin.getLogManager().recordSystem(village, VillageLogType.UPKEEP_PENALTY,
						"lives", lost);
				village.broadcast(this.plugin.getVillageMessages().prefixedText(Lang.UPKEEP_PENALTY,
						"lives", lost));
			}
		}
		state.update(now.plus(this.settings.getInterval()), missed);
	}

	private VillageUpkeepState state(Village village) {
		if (village == null) throw new IllegalArgumentException("village cannot be null");
		return this.states.computeIfAbsent(village.getUUID(), ignored ->
				new VillageUpkeepState(village.getUUID(), Instant.now().plus(this.settings.getInterval()), 0));
	}
}
