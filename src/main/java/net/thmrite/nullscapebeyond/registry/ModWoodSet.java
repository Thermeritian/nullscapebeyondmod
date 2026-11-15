package net.thmrite.nullscapebeyond.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.thmrite.nullscapebeyond.block.StrippableLogBlock;

public final class ModWoodSet {
    public static final List<ModWoodSet> ALL = new ArrayList<>();

    public final String name;
    public final WoodType woodType;

    public final DeferredBlock<RotatedPillarBlock> strippedLog;
    public final DeferredBlock<RotatedPillarBlock> strippedWood;
    public final DeferredBlock<StrippableLogBlock> log;
    public final DeferredBlock<StrippableLogBlock> wood;
    public final DeferredBlock<Block> planks;
    public final DeferredBlock<StairBlock> stairs;
    public final DeferredBlock<SlabBlock> slab;
    public final DeferredBlock<FenceBlock> fence;
    public final DeferredBlock<FenceGateBlock> fenceGate;
    public final DeferredBlock<DoorBlock> door;
    public final DeferredBlock<TrapDoorBlock> trapdoor;
    public final DeferredBlock<ButtonBlock> button;
    public final DeferredBlock<PressurePlateBlock> pressurePlate;
    public final DeferredBlock<StandingSignBlock> sign;
    public final DeferredBlock<WallSignBlock> wallSign;
    public final DeferredBlock<CeilingHangingSignBlock> hangingSign;
    public final DeferredBlock<WallHangingSignBlock> wallHangingSign;
    public final DeferredBlock<LeavesBlock> leaves;
    public final DeferredBlock<SaplingBlock> sapling;

    private final List<Supplier<? extends ItemLike>> items = new ArrayList<>();

    public static ModWoodSet create(String name, MapColor planksColor, MapColor barkColor, WoodType woodType, TreeGrower grower) {
        ModWoodSet set = new ModWoodSet(name, planksColor, barkColor, woodType, grower);
        ALL.add(set);
        return set;
    }

    private ModWoodSet(String name, MapColor planksColor, MapColor barkColor, WoodType woodType, TreeGrower grower) {
        this.name = name;
        this.woodType = woodType;
        BlockSetType setType = woodType.setType();

        strippedLog = ModBlocks.reg("stripped_" + name + "_log",
                () -> new RotatedPillarBlock(Properties.ofFullCopy(Blocks.STRIPPED_OAK_LOG).mapColor(barkColor)), items);
        strippedWood = ModBlocks.reg("stripped_" + name + "_wood",
                () -> new RotatedPillarBlock(Properties.ofFullCopy(Blocks.STRIPPED_OAK_WOOD).mapColor(barkColor)), items);
        log = ModBlocks.reg(name + "_log",
                () -> new StrippableLogBlock(Properties.ofFullCopy(Blocks.OAK_LOG).mapColor(barkColor), strippedLog), items);
        wood = ModBlocks.reg(name + "_wood",
                () -> new StrippableLogBlock(Properties.ofFullCopy(Blocks.OAK_WOOD).mapColor(barkColor), strippedWood), items);

        planks = ModBlocks.reg(name + "_planks",
                () -> new Block(Properties.ofFullCopy(Blocks.OAK_PLANKS).mapColor(planksColor)), items);
        stairs = ModBlocks.reg(name + "_stairs",
                () -> new StairBlock(planks.get().defaultBlockState(), Properties.ofFullCopy(planks.get())), items);
        slab = ModBlocks.reg(name + "_slab",
                () -> new SlabBlock(Properties.ofFullCopy(planks.get())), items);
        fence = ModBlocks.reg(name + "_fence",
                () -> new FenceBlock(Properties.ofFullCopy(planks.get())), items);
        fenceGate = ModBlocks.reg(name + "_fence_gate",
                () -> new FenceGateBlock(woodType, Properties.ofFullCopy(planks.get())), items);
        door = ModBlocks.reg(name + "_door",
                () -> new DoorBlock(setType, Properties.ofFullCopy(Blocks.OAK_DOOR).mapColor(planksColor)),
                b -> new DoubleHighBlockItem(b, new Item.Properties()), items);
        trapdoor = ModBlocks.reg(name + "_trapdoor",
                () -> new TrapDoorBlock(setType, Properties.ofFullCopy(Blocks.OAK_TRAPDOOR).mapColor(planksColor)), items);
        button = ModBlocks.reg(name + "_button",
                () -> new ButtonBlock(setType, 30, Properties.ofFullCopy(Blocks.OAK_BUTTON)), items);
        pressurePlate = ModBlocks.reg(name + "_pressure_plate",
                () -> new PressurePlateBlock(setType, Properties.ofFullCopy(Blocks.OAK_PRESSURE_PLATE).mapColor(planksColor)), items);

        sign = ModBlocks.regNoItem(name + "_sign",
                () -> new StandingSignBlock(woodType, Properties.ofFullCopy(Blocks.OAK_SIGN).mapColor(planksColor)));
        wallSign = ModBlocks.regNoItem(name + "_wall_sign",
                () -> new WallSignBlock(woodType, Properties.ofFullCopy(Blocks.OAK_WALL_SIGN).mapColor(planksColor).dropsLike(sign.get())));
        hangingSign = ModBlocks.regNoItem(name + "_hanging_sign",
                () -> new CeilingHangingSignBlock(woodType, Properties.ofFullCopy(Blocks.OAK_HANGING_SIGN).mapColor(planksColor)));
        wallHangingSign = ModBlocks.regNoItem(name + "_wall_hanging_sign",
                () -> new WallHangingSignBlock(woodType, Properties.ofFullCopy(Blocks.OAK_WALL_HANGING_SIGN).mapColor(planksColor).dropsLike(hangingSign.get())));
        items.add(ModItems.ITEMS.register(name + "_sign",
                () -> new SignItem(new Item.Properties().stacksTo(16), sign.get(), wallSign.get())));
        items.add(ModItems.ITEMS.register(name + "_hanging_sign",
                () -> new HangingSignItem(hangingSign.get(), wallHangingSign.get(), new Item.Properties().stacksTo(16))));

        leaves = ModBlocks.reg(name + "_leaves",
                () -> new LeavesBlock(Properties.ofFullCopy(Blocks.OAK_LEAVES)), items);
        sapling = ModBlocks.reg(name + "_sapling",
                () -> new SaplingBlock(grower, Properties.ofFullCopy(Blocks.OAK_SAPLING)), items);
    }

    public void acceptItems(Consumer<ItemLike> out) {
        items.forEach(s -> out.accept(s.get()));
    }

    public void registerFlammables() {
        FireBlock fire = (FireBlock) Blocks.FIRE;
        for (Supplier<? extends Block> b : List.<Supplier<? extends Block>>of(planks, stairs, slab, fence, fenceGate)) {
            fire.setFlammable(b.get(), 5, 20);
        }
        for (Supplier<? extends Block> b : List.<Supplier<? extends Block>>of(log, wood, strippedLog, strippedWood)) {
            fire.setFlammable(b.get(), 5, 5);
        }
        fire.setFlammable(leaves.get(), 30, 60);
    }

    public void addSignBlocks(BlockEntityTypeAddBlocksEvent event) {
        event.modify(BlockEntityType.SIGN, sign.get(), wallSign.get());
        event.modify(BlockEntityType.HANGING_SIGN, hangingSign.get(), wallHangingSign.get());
    }
}
