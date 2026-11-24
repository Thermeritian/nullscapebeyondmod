package net.thmrite.nullscapebeyond.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.thmrite.nullscapebeyond.registry.ModBlocks;

/** Flint and steel for nullflame: lights nullflame fire on a block face, and lights nullflame campfires. */
public class FlintAndStillclayItem extends Item {
    public FlintAndStillclayItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clicked);
        ItemStack stack = context.getItemInHand();

        // nullflame campfire
        if (clickedState.is(ModBlocks.NULLFLAME_CAMPFIRE.get())
                && !clickedState.getValue(BlockStateProperties.LIT) && !clickedState.getValue(BlockStateProperties.WATERLOGGED)) {
            level.playSound(player, clicked, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
            level.setBlock(clicked, clickedState.setValue(BlockStateProperties.LIT, true), 11);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, clicked);
            damage(stack, player, context);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // nullflame fire next to the clicked face
        BlockPos firePos = clicked.relative(context.getClickedFace());
        BlockState fire = ModBlocks.NULLFLAME.get().defaultBlockState();
        if (level.getBlockState(firePos).isAir() && fire.canSurvive(level, firePos)) {
            level.playSound(player, firePos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
            level.setBlock(firePos, fire, 11);
            level.gameEvent(player, GameEvent.BLOCK_PLACE, firePos);
            if (player instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, firePos, stack);
            }
            damage(stack, player, context);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.FAIL;
    }

    private static void damage(ItemStack stack, Player player, UseOnContext context) {
        if (player != null) {
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
        }
    }

    /** Used as a crafting ingredient (nullflame bottle) it is not consumed: it loses 1 durability instead. */
    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        ItemStack damaged = stack.copy();
        damaged.setDamageValue(damaged.getDamageValue() + 1);
        return damaged.getDamageValue() >= damaged.getMaxDamage() ? ItemStack.EMPTY : damaged;
    }
}
