package pl.kiosel.villages.data.village;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import pl.kiosel.core.utils.LocationUtils;
import pl.kiosel.villages.data.AbstractMutableEntity;

public class Region extends AbstractMutableEntity {

    private String name;
    @Getter
	private Village village;

    @Getter
	private World world;
    @Getter
	private Location center;
    @Getter
	private int enlargementLevel;
    @Getter
	private int size;

    @Getter
	private Location firstCorner;
    @Getter
	private Location secondCorner;

    public Region(String name, Location center) {
        this.name = name;
        this.world = center.getWorld();
        this.center = center;
    }

    public Region(Village village, Location center, int defaultSize) {
        this(village.getName(), center);

        this.village = village;
        this.size = defaultSize;

        this.update();
    }

    public synchronized void update() {
		super.markChanged();

        if (this.center == null) {
            return;
        }

        if (this.size < 1) {
            return;
        }

        if (this.world == null) {
            this.world = Bukkit.getWorlds().get(0);
        }

        if (this.world != null) {
            int lx = this.center.getBlockX() + this.size;
            int lz = this.center.getBlockZ() + this.size;

            int px = this.center.getBlockX() - this.size;
            int pz = this.center.getBlockZ() - this.size;

            Vector l = new Vector(lx, LocationUtils.getMinHeight(this.world), lz);
            Vector p = new Vector(px, this.world.getMaxHeight(), pz);

            this.firstCorner = l.toLocation(this.world);
            this.secondCorner = p.toLocation(this.world);
        }
    }

    public boolean isIn(Location location) {
        if (location == null || this.firstCorner == null || this.secondCorner == null) {
            return false;
        }

        if (this.world == null) {
            return false;
        }

        if (!this.world.equals(location.getWorld())) {
            return false;
        }

        if (location.getBlockX() > this.getLowerX() && location.getBlockX() < this.getUpperX()) {
            if (location.getBlockY() > this.getLowerY() && location.getBlockY() < this.getUpperY()) {
                return location.getBlockZ() > this.getLowerZ() && location.getBlockZ() < this.getUpperZ();
            }
        }

        return false;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
		super.markChanged();
	}

	public void setVillage(Village village) {
        this.village = village;
		super.markChanged();
    }

    void setCenter(Location location) {
        this.center = location;
        this.world = location.getWorld();
        this.update();
    }

	void setEnlargementLevel(int enlargementLevel) {
        this.enlargementLevel = enlargementLevel;
		super.markChanged();
    }

	public void setSize(int size) {
        this.size = size;
        this.update();
    }

    public int getUpperX() {
        return compareCoordinates(true, this.firstCorner.getBlockX(), this.secondCorner.getBlockX());
    }

    public int getUpperY() {
        return compareCoordinates(true, this.firstCorner.getBlockY(), this.secondCorner.getBlockY());
    }

    public int getUpperZ() {
        return compareCoordinates(true, this.firstCorner.getBlockZ(), this.secondCorner.getBlockZ());
    }

    public int getLowerX() {
        return compareCoordinates(false, this.firstCorner.getBlockX(), this.secondCorner.getBlockX());
    }

    public int getLowerY() {
        return compareCoordinates(false, this.firstCorner.getBlockY(), this.secondCorner.getBlockY());
    }

    public int getLowerZ() {
        return compareCoordinates(false, this.firstCorner.getBlockZ(), this.secondCorner.getBlockZ());
    }

    public Location getUpperCorner() {
        return new Location(this.world, this.getUpperX(), this.getUpperY(), this.getUpperZ());
    }

    public Location getLowerCorner() {
        return new Location(this.world, this.getLowerX(), this.getLowerY(), this.getLowerZ());
    }

	private static int compareCoordinates(boolean upper, int a, int b) {
        if (upper) {
            return Math.max(b, a);
        } else {
            return Math.min(a, b);
        }
    }

    @Override
    public UnitType getType() {
        return UnitType.REGION;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
