package pl.kiosel.villages.data.rank;

import lombok.Getter;

import java.util.Locale;

public final class RankSystem {

	private final RankType rankType;
	private final int eloKFactor;
	private final int staticWinnerGain;
	private final int staticLoserLoss;
	private final double percentTransfer;

	public RankSystem(RankType rankType, int eloKFactor, int staticWinnerGain,
					  int staticLoserLoss, double percentTransfer) {
		this.rankType = rankType;
		this.eloKFactor = Math.max(1, eloKFactor);
		this.staticWinnerGain = Math.max(0, staticWinnerGain);
		this.staticLoserLoss = Math.max(0, staticLoserLoss);
		this.percentTransfer = Math.max(0.0D, percentTransfer);
	}

	public RankResult calculate(int winnerPoints, int loserPoints) {
		switch (this.rankType) {
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

	public enum RankType {
		ELO,
		PERCENT,
		STATIC;

		public static RankType parse(String value) {
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
