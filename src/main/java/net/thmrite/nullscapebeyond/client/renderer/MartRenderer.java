package net.thmrite.nullscapebeyond.client.renderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.entity.MartEntity;
import net.thmrite.nullscapebeyond.shaders.emissive.client.EmissiveGeoLayer;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Loads geo/entity/mart.geo.json, animations/entity/mart.animation.json, textures/entity/mart.png.
 * Size (water level + merging) comes from the SCALE attribute, which GeckoLib applies natively.
 */
public class MartRenderer extends GeoEntityRenderer<MartEntity> {
    public MartRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "mart")));
        this.shadowRadius = 0.4f;
    }

    @Override
    public RenderType getRenderType(MartEntity animatable, ResourceLocation texture,
                                    MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}