package net.thmrite.nullscapebeyond.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.entity.ChargerPlatformEntity;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;


public class ChargerPlatformModel extends DefaultedEntityGeoModel<ChargerPlatformEntity> {
    public ChargerPlatformModel() {
        super(ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "charger_platform"));
    }
}
