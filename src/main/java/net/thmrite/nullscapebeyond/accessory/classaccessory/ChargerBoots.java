package net.thmrite.nullscapebeyond.accessory.classaccessory;

import java.util.function.Consumer;

import net.thmrite.nullscapebeyond.accessory.ClassAccessory;
import org.jetbrains.annotations.Nullable;

import io.wispforest.accessories.api.AccessoriesCapability;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.thmrite.nullscapebeyond.client.renderer.ChargerBootsArmorRenderer;
import net.thmrite.nullscapebeyond.registry.ModItems;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ChargerBoots extends ClassAccessory implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ChargerBoots(Properties properties) {
        super(properties, "feet");
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private ChargerBootsArmorRenderer renderer;

            @Override
            public <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(@Nullable T livingEntity, ItemStack itemStack,
                                                                                 @Nullable EquipmentSlot equipmentSlot,
                                                                                 @Nullable HumanoidModel<T> original) {
                if (this.renderer == null)
                    this.renderer = new ChargerBootsArmorRenderer();
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public static boolean isEquipped(LivingEntity entity) {
        AccessoriesCapability cap = AccessoriesCapability.get(entity);
        return cap != null && cap.isEquipped(ModItems.CHARGER_BOOTS.get());
    }
}
