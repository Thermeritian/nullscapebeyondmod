package net.thmrite.nullscapebeyond.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.thmrite.nullscapebeyond.block.entity.NullflameCombustion;
import net.thmrite.nullscapebeyond.event.blockentity.NullflameFuel;
import net.thmrite.nullscapebeyond.item.NullflameBottleItem;


@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin implements NullflameCombustion {
    @Shadow
    int litTime;

    @Unique
    private boolean nscapebeyond$nullflame;
    @Unique
    private Item nscapebeyond$fuelItemBefore = Items.AIR;
    @Unique
    private int nscapebeyond$fuelCountBefore;

    @Inject(method = "serverTick", at = @At("HEAD"))
    private static void nscapebeyond$head(Level level, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci) {
        ((NullflameCombustion) blockEntity).nscapebeyond$before();
    }

    @Inject(method = "serverTick", at = @At("TAIL"))
    private static void nscapebeyond$tail(Level level, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci) {
        ((NullflameCombustion) blockEntity).nscapebeyond$after(level, pos);
    }

    @Override
    public boolean nscapebeyond$isNullflame() {
        return this.nscapebeyond$nullflame;
    }

    @Override
    public void nscapebeyond$before() {
        ItemStack fuel = ((AbstractFurnaceBlockEntity) (Object) this).getItem(1);
        this.nscapebeyond$fuelItemBefore = fuel.getItem();
        this.nscapebeyond$fuelCountBefore = fuel.getCount();
    }

    @Override
    public void nscapebeyond$after(Level level, BlockPos pos) {
        AbstractFurnaceBlockEntity self = (AbstractFurnaceBlockEntity) (Object) this;
        ItemStack fuel = self.getItem(1);
        boolean fuelWasConsumed = fuel.getItem() != this.nscapebeyond$fuelItemBefore || fuel.getCount() < this.nscapebeyond$fuelCountBefore;

        boolean before = this.nscapebeyond$nullflame;
        if (this.litTime <= 0) {
            this.nscapebeyond$nullflame = false;                        // fire is out
        } else if (fuelWasConsumed) {
            this.nscapebeyond$nullflame = this.nscapebeyond$fuelItemBefore instanceof NullflameFuel;
        }
        // one bottle burned = one empty glass bottle back (the item has no crafting remainder, see NullflameBottleItem)
        if (fuelWasConsumed && this.nscapebeyond$fuelItemBefore instanceof NullflameBottleItem) {
            Block.popResource(level, pos.above(), new ItemStack(Items.GLASS_BOTTLE));
        }
        if (before != this.nscapebeyond$nullflame) {
            self.setChanged();
            BlockState state = level.getBlockState(pos);
            level.sendBlockUpdated(pos, state, state, 3);              // pushes getUpdatePacket to the clients
        }
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void nscapebeyond$save(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        tag.putBoolean("NullflameCombustion", this.nscapebeyond$nullflame);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void nscapebeyond$load(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        this.nscapebeyond$nullflame = tag.getBoolean("NullflameCombustion");
    }

    // Vanilla furnaces do not sync their block entity; these two (added to the class, overriding BlockEntity's) do.
    // The return type must be EXACTLY Packet<ClientGamePacketListener>: with a narrower return type the compiler would
    // need a bridge method, which does not exist in a mixin, and the method would silently not override anything.
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create((BlockEntity) (Object) this);
    }

    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("NullflameCombustion", this.nscapebeyond$nullflame);
        return tag;
    }
}
