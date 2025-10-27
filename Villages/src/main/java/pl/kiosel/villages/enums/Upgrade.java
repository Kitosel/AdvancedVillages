package pl.kiosel.villages.enums;

public enum Upgrade {

    IRON(1),
    GOLD(2),
    EMERALD(3),
    DIAMOND(4),
	NETHERITE(5),
	WORLDEDIT(73),
    RESET(99);

    private final int level;

    Upgrade(int level) {
        this.level = level;
    }

    public int getLevel() { return level; }

    public static Upgrade getByLevel(int x) {
        switch (x) {
            case 1:
                return IRON;
            case 2:
                return GOLD;
            case 3:
                return EMERALD;
            case 4:
                return DIAMOND;
            case 5:
                return NETHERITE;
            default:
                return RESET;
        }
    }
}