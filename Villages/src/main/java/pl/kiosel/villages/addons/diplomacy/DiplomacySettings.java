package pl.kiosel.villages.addons.diplomacy;

import lombok.Getter;

import java.time.Duration;

@Getter
public final class DiplomacySettings {

	private final boolean enabled;
	private final boolean alliancesEnabled;
	private final int maximumAlliances;
	private final Duration requestExpiration;
	private final boolean preventAlliedFriendlyFire;
	private final boolean preventAlliedVillageAttacks;
	private final boolean warsEnabled;
	private final boolean requireWarToAttack;
	private final Duration preparationTime;
	private final Duration warDuration;
	private final Duration warCooldown;
	private final int maximumWars;
	private final int minimumMembers;
	private final int minimumOnlineMembers;
	private final int declarationCost;
	private final int killScore;
	private final int villageLifeScore;
	private final int winnerBankReward;

	public DiplomacySettings(boolean enabled, boolean alliancesEnabled, int maximumAlliances,
	                         Duration requestExpiration, boolean preventAlliedFriendlyFire,
	                         boolean preventAlliedVillageAttacks, boolean warsEnabled,
	                         boolean requireWarToAttack, Duration preparationTime,
	                         Duration warDuration, Duration warCooldown, int maximumWars,
	                         int minimumMembers, int minimumOnlineMembers, int declarationCost,
	                         int killScore, int villageLifeScore, int winnerBankReward) {
		this.enabled = enabled;
		this.alliancesEnabled = alliancesEnabled;
		this.maximumAlliances = Math.max(0, maximumAlliances);
		this.requestExpiration = positive(requestExpiration, Duration.ofMinutes(10));
		this.preventAlliedFriendlyFire = preventAlliedFriendlyFire;
		this.preventAlliedVillageAttacks = preventAlliedVillageAttacks;
		this.warsEnabled = warsEnabled;
		this.requireWarToAttack = requireWarToAttack;
		this.preparationTime = nonNegative(preparationTime, Duration.ofMinutes(10));
		this.warDuration = positive(warDuration, Duration.ofHours(24));
		this.warCooldown = nonNegative(warCooldown, Duration.ofHours(12));
		this.maximumWars = Math.max(0, maximumWars);
		this.minimumMembers = Math.max(1, minimumMembers);
		this.minimumOnlineMembers = Math.max(0, minimumOnlineMembers);
		this.declarationCost = Math.max(0, declarationCost);
		this.killScore = Math.max(0, killScore);
		this.villageLifeScore = Math.max(0, villageLifeScore);
		this.winnerBankReward = Math.max(0, winnerBankReward);
	}

	private static Duration positive(Duration value, Duration fallback) {
		return value == null || value.isZero() || value.isNegative() ? fallback : value;
	}

	private static Duration nonNegative(Duration value, Duration fallback) {
		return value == null || value.isNegative() ? fallback : value;
	}
}
