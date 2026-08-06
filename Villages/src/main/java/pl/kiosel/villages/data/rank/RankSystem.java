package pl.kiosel.villages.data.rank;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import pl.kiosel.villages.addons.tablist.utils.NumberRange;

import java.util.Map;
import java.util.function.BiFunction;

public final class RankSystem {

    private final Map<Type, RankingAlgorithm> map;

    private RankSystem(Map<Type, RankingAlgorithm> map) {
        this.map = map;
    }

    public RankResult calculate(Type type, int attackerPoints, int victimPoints) {
        return this.map.get(type).apply(attackerPoints, victimPoints);
    }

	public static Map<NumberRange, Integer> eloConstants;

    public static RankSystem create() {
        ImmutableMap<Type, RankingAlgorithm> build = new ImmutableMap.Builder<Type, RankingAlgorithm>()
                .put(Type.ELO, (attackerPoints, victimPoints) -> {
                    int attackerElo = NumberRange.inRange(attackerPoints, eloConstants).orElseGet(0);
                    int victimElo = NumberRange.inRange(victimPoints, eloConstants).orElseGet(0);

                    double attackerE = 1.0D / (1.0D + Math.pow(10.0D, (victimPoints - attackerPoints) / 400.0D));
                    double victimE = 1.0D / (1.0D + Math.pow(10.0D, (attackerPoints - victimPoints) / 400.0D));

                    attackerElo = (int) Math.round(attackerElo * (1 - attackerE));
                    victimElo = (int) Math.round(victimElo * (0 - victimE) * -1);

                    return new RankResult(attackerElo, victimElo);
                })
                .put(Type.PERCENT, (attackerPoints, victimPoints) -> new RankResult((int) (victimPoints * (1.0 / 100.0))))
                .put(Type.STATIC, (attackerPoints, victimPoints) -> new RankResult(15, 10))
                .build();

        return new RankSystem(Maps.newEnumMap(build));
    }

    public enum Type {

        ELO,
        PERCENT,
        STATIC

    }

    public static class RankResult {

        private final int attackerPoints;
        private final int victimPoints;

        public RankResult(int attackerPoints, int victimPoints) {
            this.attackerPoints = attackerPoints;
            this.victimPoints = victimPoints;
        }

        public RankResult(int samePoints) {
            this.attackerPoints = samePoints;
            this.victimPoints = samePoints;
        }

        public int getAttackerPoints() {
            return this.attackerPoints;
        }

        public int getVictimPoints() {
            return this.victimPoints;
        }

    }

    public interface RankingAlgorithm extends BiFunction<Integer, Integer, RankResult> {
    }

}
