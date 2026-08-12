package pl.kiosel.villages.data.rank;

import lombok.Getter;

import java.util.Locale;

/** Small, deterministic calculator used by the live ranking manager. */
public final class RankSystem {

	private final Type type;
	private final int eloKFactor;
	private final int staticWinnerGain;
	private final int staticLoserLoss;
	private final double percentTransfer;

	public RankSystem(Type type, int eloKFactor, int staticWinnerGain,
	                  int staticLoserLoss, double percentTransfer) {
		this.type = type;
		this.eloKFactor = Math.max(1, eloKFactor);
		this.staticWinnerGain = Math.max(0, staticWinnerGain);
		this.staticLoserLoss = Math.max(0, staticLoserLoss);
		this.percentTransfer = Math.max(0.0D, percentTransfer);
	}

	public RankResult calculate(int winnerPoints, int loserPoints) {
		switch (this.type) {
			case STATIC:
				return new RankResult(this.staticWinnerGain, this.staticLoserLoss);
			case PERCENT:
				int transfer = this.percentTransfer <= 0.0D
						? 0
						: Math.max(1, safeRound(loserPoints * this.percentTransfer / 100.0D));
				return new RankResult(transfer, transfer);
			case ELO:
			default:
				double expectedWinner = 1.0D /
						(1.0D + Math.pow(10.0D, (loserPoints - winnerPoints) / 400.0D));
				int change = Math.max(1, safeRound(this.eloKFactor * (1.0D - expectedWinner)));
				return new RankResult(change, change);
		}
	}

	private static int safeRound(double value) {
		return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, Math.round(value)));
	}

	public enum Type {
		ELO,
		PERCENT,
		STATIC;

		public static Type parse(String value) {
			if (value == null) {
				return ELO;
			}
			try {
				return valueOf(value.trim().toUpperCase(Locale.ROOT));
			} catch (IllegalArgumentException ignored) {
				return ELO;
			}
		}
	}

	@Getter
	public static final class RankResult {
		private final int winnerGain;
		private final int loserLoss;

		private RankResult(int winnerGain, int loserLoss) {
			this.winnerGain = winnerGain;
			this.loserLoss = loserLoss;
		}
	}
}
