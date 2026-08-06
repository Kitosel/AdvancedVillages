package pl.kiosel.villages.addons.tablist.placeholders;

public class BasicPlaceholders<T> extends Placeholders<T, BasicPlaceholders<T>> {

    @Override
    public BasicPlaceholders<T> create() {
        return new BasicPlaceholders<>();
    }

}
