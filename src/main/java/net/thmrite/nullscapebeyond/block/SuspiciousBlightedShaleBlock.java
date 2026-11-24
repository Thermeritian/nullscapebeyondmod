package net.thmrite.nullscapebeyond.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.thmrite.nullscapebeyond.NullscapeBeyond;


public class SuspiciousBlightedShaleBlock extends BrushableBlock {
    public static final ResourceKey<LootTable> LOOT = ResourceKey.create(Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "archaeology/blighted_shale"));

    public SuspiciousBlightedShaleBlock(Block turnsInto, Properties properties,
                                        SoundEvent brushSound, SoundEvent brushCompletedSound) {
        super(turnsInto, brushSound, brushCompletedSound, properties);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof BrushableBlockEntity brushable) {
            brushable.setLootTable(LOOT, level.getRandom().nextLong());
        }
    }
}
