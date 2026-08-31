package pl.kiosel.villages.data.user;

import org.jetbrains.annotations.NotNull;
import pl.kiosel.villages.data.rank.Rank;
import pl.kiosel.villages.data.user.top.UserComparator;

import java.util.function.IntUnaryOperator;

public class UserRank extends Rank<User> implements Comparable<UserRank> {

    private int points;
    private int kills;
    private int deaths;
    private int assists;
    private int logouts;

    UserRank(User user, int rankStart) {
        super(user);
        this.points = rankStart;
    }

    public User getUser() {
        return this.entity;
    }

    @Override
    public int getPoints() {
        return this.points;
    }

    public void setPoints(int points) {
        this.points = Math.max(0, points);
        this.entity.markChanged();
    }

    public void updatePoints(IntUnaryOperator update) {
        this.setPoints(update.applyAsInt(this.points));
    }

    @Override
    public int getKills() {
        return this.kills;
    }

    public void setKills(int kills) {
        this.kills = Math.max(0, kills);
        this.entity.markChanged();
    }

    public void updateKills(IntUnaryOperator update) {
        this.setKills(update.applyAsInt(this.kills));
    }

    @Override
    public int getDeaths() {
        return this.deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = Math.max(0, deaths);
        this.entity.markChanged();
    }

    public void updateDeaths(IntUnaryOperator update) {
        this.setDeaths(update.applyAsInt(this.deaths));
    }

    @Override
    public int getAssists() {
        return this.assists;
    }

    public void setAssists(int assists) {
        this.assists = Math.max(0, assists);
        this.entity.markChanged();
    }

    public void updateAssists(IntUnaryOperator update) {
        this.setAssists(update.applyAsInt(this.assists));
    }

    @Override
    public int getLogouts() {
        return this.logouts;
    }

    public void setLogouts(int logouts) {
        this.logouts = Math.max(0, logouts);
        this.entity.markChanged();
    }

    public void updateLogouts(IntUnaryOperator update) {
        this.setLogouts(update.applyAsInt(this.logouts));
    }

    @Override
    public float getKDR() {
        return this.deaths == 0 ? this.kills : 1.0F * this.kills / this.deaths;
    }

    @Override
    public float getKDA() {
        return this.deaths == 0 ? (this.kills + this.assists) : 1.0F * (this.kills + this.assists) / this.deaths;
    }

    @Override
    public int compareTo(@NotNull UserRank rank) {
        return UserComparator.POINTS_COMPARATOR.compare(this, rank);
    }

}
