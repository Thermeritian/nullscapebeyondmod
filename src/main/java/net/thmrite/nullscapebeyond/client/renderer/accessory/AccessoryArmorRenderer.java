package net.thmrite.nullscapebeyond.client.renderer.accessory;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.thmrite.nullscapebeyond.client.renderer.accessory.AccessoryGeoModel;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;


public class AccessoryArmorRenderer<T extends Item & GeoItem> extends GeoArmorRenderer<T> {
    public AccessoryArmorRenderer(String modelName) {
        super(new AccessoryGeoModel<>(modelName));
    }

    @Override
    public void applyBoneVisibilityBySlot(EquipmentSlot currentSlot) {
        setAllVisible(true);
    }
}
