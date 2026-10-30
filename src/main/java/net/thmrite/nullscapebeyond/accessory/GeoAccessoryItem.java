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
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Common base of every accessory with a GeckoLib model worn on the player.
 * Where the model attaches is decided by its armor-named bones (see AccessoryArmorRenderer), not by the slot.
 * Assets (model = the short name given to the constructor, e.g. "orb"):
 *   geo/item/accessory/<model>.geo.json, textures/item/accessory/<model>.png, animations/<model>.animation.json
 */
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

    /** Name of the looping animation to play, or null for none. Must match the name inside the animation json. */
    protected String idleAnimation() {
        return "idle";
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        String idle = idleAnimation();
        if (idle != null) {
            controllers.add(new AnimationController<>(this, "main", 0,
                    state -> state.setAndContinue(RawAnimation.begin().thenLoop(idle))));
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    /** GeckoLib only accepts armor renderers that come from a GeoRenderProvider. */
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
