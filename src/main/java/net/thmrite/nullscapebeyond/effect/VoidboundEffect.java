package net.thmrite.nullscapebeyond.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.registry.ModSounds;

public class VoidboundEffect extends MobEffect {
    public VoidboundEffect() {
        super(MobEffectCategory.HARMFUL, 0x4B1E8C);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, id("speed"), 0.35, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, id("damage"), 3.0, AttributeModifier.Operation.ADD_VALUE);
        addAttributeModifier(Attributes.ARMOR, id("armor"), 4.0, AttributeModifier.Operation.ADD_VALUE);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "effect.voidbound." + path);
    }

    // Infinite effects receive entity.tickCount here.
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide && entity.getRandom().nextInt(5) == 0) {
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    ModSounds.VOIDBOUND_IDLE.get(), entity.getSoundSource(), 1.0F,
                    0.9F + entity.getRandom().nextFloat() * 0.2F);
        }
        return true;
    }
}