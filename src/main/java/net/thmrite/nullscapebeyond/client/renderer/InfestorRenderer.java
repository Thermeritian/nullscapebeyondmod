package net.thmrite.nullscapebeyond.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.entity.InfestorEntity;
import net.thmrite.nullscapebeyond.shaders.emissive.client.EmissiveGeoLayer;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class InfestorRenderer extends GeoEntityRenderer<InfestorEntity> {
    public InfestorRenderer(EntityRendererProvider.Context context) {
        // "infestor" -> geo/entity/infestor.geo.json, animations/entity/infestor.animation.json, textures/entity/infestor.png
        super(context, new DefaultedEntityGeoModel<>(ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "infestor")));
        this.shadowRadius = 0.7F;
        addRenderLayer(new EmissiveGeoLayer<>(this));
    }
}