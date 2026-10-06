package net.thmrite.nullscapebeyond.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.accessory.classaccessory.ChargerBoots;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;


public final class ChargerBootsArmorRenderer extends GeoArmorRenderer<ChargerBoots> {
    public ChargerBootsArmorRenderer() {
        super(new DefaultedItemGeoModel<>(
                ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "accessory/charger_boots")));    }
}
