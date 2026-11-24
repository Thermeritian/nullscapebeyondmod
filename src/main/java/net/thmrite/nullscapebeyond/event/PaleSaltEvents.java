package net.thmrite.nullscapebeyond.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.registry.ModBlocks;
import net.thmrite.nullscapebeyond.registry.ModItems;

/**
 * Pale sand + a FULL nulqua cauldron = pale salt. The nulqua is not used up.
 * Right-click: one pale sand. Sneak + right-click: the whole stack. Either hand works.
 * Done with an event (not a cauldron interaction) because vanilla skips block interactions while sneaking with an item.
 */
@EventBusSubscriber(modid = NullscapeBeyond.MODID)
public final class PaleSaltEvents {
    private static final ResourceLocation NULQUA_CAULDRON = ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "nulqua_cauldron");

    private PaleSaltEvents() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(ModBlocks.PALESAND.get().asItem())) return;
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!state.is(BuiltInRegistries.BLOCK.get(NULQUA_CAULDRON))
                || state.getValue(LayeredCauldronBlock.LEVEL) != LayeredCauldronBlock.MAX_FILL_LEVEL) return;

        Player player = event.getEntity();
        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                player.setItemInHand(event.getHand(), new ItemStack(ModItems.PALE_SALT.get(), stack.getCount()));
            } else {
                player.setItemInHand(event.getHand(), ItemUtils.createFilledResult(stack, player, new ItemStack(ModItems.PALE_SALT.get())));
            }
            level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.8F, 1.2F);
        }
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
        event.setCanceled(true);
    }
}
