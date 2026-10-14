package net.thmrite.nullscapebeyond.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.entity.BeaconEntity;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BeaconRenderer extends GeoEntityRenderer<BeaconEntity> {
    public BeaconRenderer(EntityRendererProvider.Context context) {
        // geo/entity/beacon.geo.json, animations/entity/beacon.animation.json, textures/entity/beacon.png
        super(context, new DefaultedEntityGeoModel<>(
                ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "beacon")));
        this.shadowRadius = 0.0F;
    }
}