package pl.kiosel.villages.data.rank;

import lombok.Getter;
import pl.kiosel.villages.data.MutableEntity;
import pl.kiosel.villages.data.VEntity;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Rank<T extends MutableEntity> {

    @Getter
    protected final T entity;
    protected final Map<String, Integer> position = new ConcurrentHashMap<>();

    protected Rank(T entity) {
        this.entity = entity;
    }

	public VEntity.UnitType getType() {
        return this.entity.getType();
    }

    public String getIdentityName() {
        return this.entity.getName();
    }

    public String getIdentityKey() {
        return this.entity.getIdentityKey();
    }

    /**
     * @param top the id of the top - you can use {@link DefaultTops} to get ids of default built-in tops
     *
     * @return position in which entity is for the given top, return 0 if entity is not in the top
     */
    public int getPosition(String top) {
        return this.position.getOrDefault(top.toLowerCase(Locale.ROOT), 0);
    }

    /**
     * You should not use this method since this value will be overwritten in the next top recalculation
     * It's only for internal use (or when you added your own top from your plugin)
     */
    public void setPosition(String top, int position) {
        this.position.put(top.toLowerCase(Locale.ROOT), position);
    }

    public abstract int getPoints();

    public abstract int getKills();

    public abstract int getDeaths();

    public abstract int getAssists();

    public abstract float getKDR();

    public abstract float getKDA();

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (o == this) {
            return true;
        }

        if (o.getClass() != this.getClass()) {
            return false;
        }

        Rank<?> rank = (Rank<?>) o;
        if (rank.getType() != this.getType()) {
            return false;
        }

        return this.getIdentityKey().equals(rank.getIdentityKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getType(), this.getIdentityKey());
    }

    @Override
    public String toString() {
        return Integer.toString(this.getPoints());
    }

    public static int compareName(Rank<?> o1, Rank<?> o2) {
        String firstName = o1.getIdentityName();
        String secondName = o2.getIdentityName();
        if (Objects.equals(firstName, secondName)) {
            return o1.getIdentityKey().compareTo(o2.getIdentityKey());
        }

        if (firstName == null) {
            return -1;
        }

        if (secondName == null) {
            return 1;
        }

        int result = firstName.compareTo(secondName);
        return result != 0 ? result : o1.getIdentityKey().compareTo(o2.getIdentityKey());
    }

}
