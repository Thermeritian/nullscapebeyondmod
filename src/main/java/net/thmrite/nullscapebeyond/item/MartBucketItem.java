package net.thmrite.nullscapebeyond.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.thmrite.nullscapebeyond.entity.MartEntity;
import net.thmrite.nullscapebeyond.registry.ModEntities;
import net.thmrite.nullscapebeyond.registry.ModItems;

/** "Mart-in-a-bucket": stores water level, health, owner and custom name in item components. */
public class MartBucketItem extends Item {
    public static final String TAG_WATER = "Water";
    public static final String TAG_HEALTH = "Health";
    public static final String TAG_OWNER = "Owner";

    public MartBucketItem(Properties properties) {
        super(properties);
    }

    public static ItemStack fromMart(MartEntity mart) {
        ItemStack stack = new ItemStack(ModItems.MART_BUCKET.get());
        CompoundTag tag = new CompoundTag();
        tag.putInt(TAG_WATER, mart.getWater());
        tag.putFloat(TAG_HEALTH, mart.getHealth());
        if (mart.getOwnerUUID() != null) tag.putUUID(TAG_OWNER, mart.getOwnerUUID());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        if (mart.hasCustomName()) stack.set(DataComponents.CUSTOM_NAME, mart.getCustomName());
        return stack;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;

        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());

        MartEntity mart = ModEntities.MART.get().create(server);
        if (mart == null) return InteractionResult.FAIL;

        mart.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, context.getRotation(), 0f);
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        mart.applyBucketData(tag);
        if (stack.has(DataComponents.CUSTOM_NAME)) mart.setCustomName(stack.get(DataComponents.CUSTOM_NAME));
        server.addFreshEntity(mart);

        Player player = context.getPlayer();
        level.gameEvent(player, GameEvent.ENTITY_PLACE, pos);
        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY_FISH, SoundSource.NEUTRAL, 1.0f, 1.0f);
        if (player != null && !player.getAbilities().instabuild) {
            player.setItemInHand(context.getHand(), new ItemStack(Items.BUCKET));
        }
        return InteractionResult.CONSUME;
    }
}