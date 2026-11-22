package net.thmrite.nullscapebeyond.client.fluid;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.client.fluid.WaterUnderwaterLoop;
import net.thmrite.nullscapebeyond.fluid.WaterFluidType;

@EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
public final class WaterUnderwaterAudio {
    private static boolean wasUnder;
    private static int cooldown;

    private WaterUnderwaterAudio() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            wasUnder = false;
            return;
        }
        boolean under = player.getEyeInFluidType() instanceof WaterFluidType;

        if (under && !wasUnder) {
            mc.level.playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.AMBIENT_UNDERWATER_ENTER, SoundSource.AMBIENT, 1.0F, 1.0F, false);
            mc.getSoundManager().play(new WaterUnderwaterLoop(player));
        } else if (!under && wasUnder) {
            mc.level.playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.AMBIENT_UNDERWATER_EXIT, SoundSource.AMBIENT, 1.0F, 1.0F, false);
        }
        wasUnder = under;

        if (cooldown > 0) {
            cooldown--;
        } else if (under) {
            float f = mc.level.random.nextFloat();
            SoundEvent extra = f < 0.0001F ? SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS_ULTRA_RARE
                    : f < 0.001F ? SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS_RARE
                    : f < 0.01F ? SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS : null;
            if (extra != null) {
                mc.level.playLocalSound(player.getX(), player.getY(), player.getZ(), extra, SoundSource.AMBIENT, 1.0F, 1.0F, false);
                cooldown = 100;
            }
        }
    }
}
