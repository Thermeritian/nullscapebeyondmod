package net.thmrite.nullscapebeyond.accessory;


public class NormalAccessory extends GeoAccessoryItem {
    public NormalAccessory(Properties properties, String slot, String model) {
        super(properties, slot, model);
    }

    @Override
    public int tooltipColor() {
        return 0x55AAFF; // Normal accessories: blue
    }

    @Override
    public boolean hasFlavorText() {
        return false;
    }
}
