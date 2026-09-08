package pl.kiosel.villages.manager;

import pl.kiosel.rosacore.config.RosaConfig;
import pl.kiosel.rosacore.utils.NumberUtils;
import pl.kiosel.rosacore.utils.TimeUtils;
import pl.kiosel.villages.AdvancedVillages;
import pl.kiosel.villages.data.user.User;
import pl.kiosel.villages.data.user.VillageSpecialization;

import java.time.Duration;
import java.util.*;

public final class SpecializationManager {

	private final AdvancedVillages plugin;
	private final RosaConfig file;
	private volatile boolean enabled;
	private volatile Duration changeCooldown = Duration.ofHours(1);
	private volatile Map<VillageSpecialization, Double> bonuses = Collections.emptyMap();
	private volatile List<VillageSpecialization> available = Collections.emptyList();

	public SpecializationManager(AdvancedVillages plugin) {
		this.plugin = plugin;
		this.file = plugin.getSpecFile();
		this.reload();
	}

	public synchronized void reload() {
		if (!this.plugin.isDev()) return;
		this.enabled = this.file.getBoolean("enabled", true);
		this.changeCooldown = TimeUtils.duration(
				this.file.getString("change-cooldown", "1h"), Duration.ofHours(1), true);

		EnumMap<VillageSpecialization, Double> loadedBonuses = new EnumMap<>(VillageSpecialization.class);
		List<VillageSpecialization> loadedAvailable = new ArrayList<>();
		for (VillageSpecialization specialization : VillageSpecialization.values()) {
			String path = "specializations." + specialization.getId();
			if (!this.file.getBoolean(path + ".enabled", true)) continue;
			loadedAvailable.add(specialization);
			loadedBonuses.put(specialization, NumberUtils.clamp(
					this.file.getDouble(path + ".bonus-percent", specialization.getDefaultBonus()),
					0.0D, 100.0D));
		}
		this.bonuses = Collections.unmodifiableMap(loadedBonuses);
		this.available = Collections.unmodifiableList(loadedAvailable);
	}

	public boolean isEnabled() {
		return this.plugin.isDev() && this.enabled;
	}

	public List<VillageSpecialization> getAvailable() {
		return this.isEnabled() ? this.available : Collections.emptyList();
	}

	public Optional<VillageSpecialization> getActive(User user) {
		if (!this.isEnabled() || user == null || !user.hasVillage()) return Optional.empty();
		return user.getSpecialization().filter(this.bonuses::containsKey);
	}

	public boolean has(User user, VillageSpecialization specialization) {
		return specialization != null && this.getActive(user).filter(specialization::equals).isPresent();
	}

	public double getBonus(VillageSpecialization specialization) {
		if (!this.isEnabled() || specialization == null) return 0.0D;
		return this.bonuses.getOrDefault(specialization, 0.0D);
	}

	public Duration getRemainingCooldown(User user) {
		if (!this.isEnabled() || user == null || user.getSpecialization().isEmpty() || this.changeCooldown.isZero()) {
			return Duration.ZERO;
		}
		long remaining = this.changeCooldown.toMillis()
				- Math.max(0L, System.currentTimeMillis() - user.getSpecializationChangedAt());
		return remaining <= 0L ? Duration.ZERO : Duration.ofMillis(remaining);
	}

	public boolean select(User user, VillageSpecialization specialization) {
		if (!this.isEnabled() || user == null || !user.hasVillage() || !this.bonuses.containsKey(specialization)) {
			return false;
		}
		if (user.getSpecialization().filter(specialization::equals).isPresent()
				|| !this.getRemainingCooldown(user).isZero()) {
			return false;
		}
		user.setSpecialization(specialization, System.currentTimeMillis());
		return true;
	}

	public int applyStoreDiscount(User user, int price) {
		if (price <= 0 || !this.has(user, VillageSpecialization.MERCHANT)) return Math.max(0, price);
		double multiplier = 1.0D - this.getBonus(VillageSpecialization.MERCHANT) / 100.0D;
		return Math.max(0, (int) Math.ceil(price * multiplier));
	}

	public String getDisplayName(VillageSpecialization specialization) {
		if (!this.isEnabled() || specialization == null) return "-";
		String fallback = specialization.getId().substring(0, 1).toUpperCase()
				+ specialization.getId().substring(1);
		return this.plugin.getVillageMessages().textOrDefault(
				"specializations.names." + specialization.getId(), fallback);
	}

	public void deserialize(User user, String specialization, long changedAt) {
		if (user == null) return;
		VillageSpecialization.fromId(specialization)
				.ifPresentOrElse(value -> user.setSpecialization(value, Math.max(0L, changedAt)), () -> {
					if (specialization != null && !specialization.isBlank()) {
						this.plugin.getRosaLogger().warning("Unknown saved specialization '"
								+ specialization + "' for " + user.getName());
					}
				});
	}
}
