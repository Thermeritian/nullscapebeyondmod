package net.thmrite.nullscapebeyond.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.entity.BeaconEntity;
import net.thmrite.nullscapebeyond.shaders.emissive.client.EmissiveGeoLayer;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import javax.annotation.Nullable;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public class BeaconRenderer extends GeoEntityRenderer<BeaconEntity> {
    public BeaconRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(
                ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "beacon")));
        this.shadowRadius = 0.0F;
        addRenderLayer(new EmissiveGeoLayer<>(this));   // <- this line
    }

    @Override
    public RenderType getRenderType(BeaconEntity animatable, ResourceLocation texture,
                                    @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucentEmissive(texture);
    }
}