package net.thmrite.nullscapebeyond.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.thmrite.nullscapebeyond.lighting.client.DynamicLightEngine;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {

    /** Lets the lighting engine know a block changed (it may block light or emit it). */
    @Inject(method = "sendBlockUpdated", at = @At("HEAD"))
    private void nsb$onBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags, CallbackInfo ci) {
        DynamicLightEngine.INSTANCE.onBlockChanged(pos);
    }
}