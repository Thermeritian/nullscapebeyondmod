package net.thmrite.nullscapebeyond.client.renderer.accessory;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;


public class AccessoryGeoModel<T extends GeoAnimatable> extends GeoModel<T> {
    private final ResourceLocation model, texture, animation;

    public AccessoryGeoModel(String name) {
        this.model = rl("geo/item/accessory/" + name + ".geo.json");
        this.texture = rl("textures/item/accessory/" + name + ".png");
        this.animation = rl("animations/" + name + ".animation.json");
    }

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, path);
    }

    public ResourceLocation getModelResource(T animatable) {
        return model;
    }

    public ResourceLocation getTextureResource(T animatable) {
        return texture;
    }

    public ResourceLocation getAnimationResource(T animatable) {
        return animation;
    }


    public ResourceLocation getModelResource(T animatable, @Nullable GeoRenderer<T> renderer) {
        return model;
    }

    public ResourceLocation getTextureResource(T animatable, @Nullable GeoRenderer<T> renderer) {
        return texture;
    }

    public ResourceLocation getAnimationResource(T animatable, @Nullable GeoRenderer<T> renderer) {
        return animation;
    }
}
