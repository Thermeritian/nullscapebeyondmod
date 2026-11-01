package net.thmrite.nullscapebeyond.accessory;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import io.wispforest.accessories.api.AccessoryItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.thmrite.nullscapebeyond.client.renderer.accessory.AccessoryArmorRenderer;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

import net.minecraft.resources.ResourceLocation;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.cache.GeckoLibCache;

public abstract class GeoAccessoryItem extends AccessoryItem implements GeoItem, TooltipAccessory {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final String slot;
    private final String model;

    protected GeoAccessoryItem(Properties properties, String slot, String model) {
        super(properties);
        this.slot = slot;
        this.model = model;
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public String tooltipSlot() {
        return slot;
    }

    public String modelName() {
        return model;
    }

    protected String idleAnimation() {
        return "idle";
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        String idle = idleAnimation();
        if (idle == null) return;
        controllers.add(new AnimationController<>(this, "main", 0, state -> {
            if (!hasAnimation(idle)) return PlayState.STOP;
            try {
                return state.setAndContinue(RawAnimation.begin().thenLoop(idle));
            } catch (RuntimeException e) {
                return PlayState.STOP;
            }
        }));
    }

    protected boolean hasAnimation(String name) {
        BakedAnimations animations = GeckoLibCache.getBakedAnimations().get(animationFile());
        return animations != null && animations.getAnimation(name) != null;
    }

    protected ResourceLocation animationFile() {
        return ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "animations/" + model + ".animation.json");
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private AccessoryArmorRenderer<GeoAccessoryItem> renderer;

            @Override
            public <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(@Nullable T livingEntity, ItemStack itemStack,
                                                                                 @Nullable EquipmentSlot equipmentSlot,
                                                                                 @Nullable HumanoidModel<T> original) {
                if (this.renderer == null) this.renderer = new AccessoryArmorRenderer<>(modelName());
                return this.renderer;
            }
        });
    }
}
