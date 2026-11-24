package net.thmrite.nullscapebeyond.event;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.registry.ModBlocks;
import net.thmrite.nullscapebeyond.registry.ModItems;

/**
 * Right-click nullflame fire with a glass bottle: one bottle of nullflame. Sneak + right-click: every glass bottle
 * in the hand. The fire is NOT put out. Either hand works (the event fires for each hand).
 */
@EventBusSubscriber(modid = NullscapeBeyond.MODID)
public final class NullflameBottleEvents {
    private NullflameBottleEvents() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(Items.GLASS_BOTTLE)) return;
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        if (!level.getBlockState(pos).is(ModBlocks.NULLFLAME.get())) return;

        Player player = event.getEntity();
        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                player.setItemInHand(event.getHand(), new ItemStack(ModItems.NULLFLAME_BOTTLE.get(), stack.getCount()));
            } else {
                player.setItemInHand(event.getHand(), ItemUtils.createFilledResult(stack, player, new ItemStack(ModItems.NULLFLAME_BOTTLE.get())));
            }
            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
        event.setCanceled(true);
    }
}
