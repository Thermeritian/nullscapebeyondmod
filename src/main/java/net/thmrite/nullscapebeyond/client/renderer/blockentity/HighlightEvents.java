package net.thmrite.nullscapebeyond.client.renderer.blockentity;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.block.LevelObjectBlock;

@EventBusSubscriber(modid = NullscapeBeyond.MODID)
public class HighlightEvents {
    @SubscribeEvent
    public static void onBlockHighlight(RenderHighlightEvent.Block event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.gameMode == null) return;
        if (mc.gameMode.getPlayerMode() != GameType.SURVIVAL) return;

        BlockState state = mc.level.getBlockState(event.getTarget().getBlockPos());
        if (state.getBlock() instanceof LevelObjectBlock) {
            event.setCanceled(true);
        }
    }
}