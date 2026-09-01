package pl.kiosel.villages.data.village;

import org.jetbrains.annotations.NotNull;
import pl.kiosel.villages.data.rank.Rank;
import pl.kiosel.villages.data.village.top.VillageComparator;

public class VillageRank extends Rank<Village> implements Comparable<VillageRank> {

    VillageRank(Village village) {
        super(village);
    }

    public Village getVillage() {
        return this.entity;
    }

    @Override
    public int getPoints() {
        return this.entity.getMembers().stream()
                .mapToInt(user -> user.getRank().getPoints())
                .sum();
    }

    public int getAveragePoints() {
        return this.calculateAverage(this.getPoints());
    }

    @Override
    public int getKills() {
        return this.entity.getMembers().stream()
                .mapToInt(user -> user.getRank().getKills())
                .sum();
    }

    public int getAverageKills() {
        return this.calculateAverage(this.getKills());
    }

    @Override
    public int getDeaths() {
        return this.entity.getMembers().stream()
                .mapToInt(user -> user.getRank().getDeaths())
                .sum();
    }

    public int getAverageDeaths() {
        return this.calculateAverage(this.getDeaths());
    }

    @Override
    public int getAssists() {
        return this.entity.getMembers().stream()
                .mapToInt(user -> user.getRank().getAssists())
                .sum();
    }

    public int getAverageAssists() {
        return this.calculateAverage(this.getAssists());
    }

    @Override
    public float getKDR() {
        return this.getDeaths() == 0
                ? this.getKills()
                : 1.0F * this.getKills() / this.getDeaths();
    }

    public float getAverageKDR() {
        return (float) this.entity.getMembers().stream()
                .mapToDouble(user -> user.getRank().getKDR())
                .average()
                .orElse(0.0D);
    }

    @Override
    public float getKDA() {
        return this.getDeaths() == 0
                ? this.getKills() + this.getAssists()
                : 1.0F * (this.getKills() + this.getAssists()) / this.getDeaths();
    }

    public float getAverageKDA() {
        return (float) this.entity.getMembers().stream()
                .mapToDouble(user -> user.getRank().getKDA())
                .average()
                .orElse(0.0D);
    }

    @Override
    public int compareTo(@NotNull VillageRank rank) {
        return VillageComparator.AVG_POINTS_COMPARATOR.compare(this, rank);
    }

    private int calculateAverage(int value) {
		int memberCount = this.entity.getMembers().size();
		return memberCount == 0 ? 0 : value / memberCount;
    }

    @Override
    public String toString() {
        return Integer.toString(this.getAveragePoints());
    }

}
