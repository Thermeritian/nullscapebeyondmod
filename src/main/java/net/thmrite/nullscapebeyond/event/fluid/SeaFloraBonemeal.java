package net.thmrite.nullscapebeyond.event.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.fluid.SeaFloraFluid;
import net.thmrite.nullscapebeyond.registry.ModBlocks;


@EventBusSubscriber(modid = NullscapeBeyond.MODID)
public final class SeaFloraBonemeal {
    public static final TagKey<Block> GROUND = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "sea_flora_ground"));

    private SeaFloraBonemeal() {
    }

    @SubscribeEvent
    public static void onBonemeal(BonemealEvent event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        if (!event.getState().is(GROUND)) return;
        BlockPos water = pos.above();
        if (!SeaFloraFluid.isSourceBlock(level, water)) return;

        if (level instanceof ServerLevel server) {
            grow(server, water, server.getRandom());
            server.sendParticles(ParticleTypes.HAPPY_VILLAGER, water.getX() + 0.5, water.getY() + 0.5, water.getZ() + 0.5, 15, 1.5, 0.5, 1.5, 0.0);
        }
        event.setSuccessful(true);
    }

    private static void grow(ServerLevel level, BlockPos origin, RandomSource random) {
        for (int i = 0; i < 128; i++) {
            BlockPos p = origin;
            boolean blocked = false;
            for (int j = 0; j < i / 16; j++) {
                p = p.offset(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
                if (level.getBlockState(p).isCollisionShapeFullBlock(level, p)) {
                    blocked = true;
                    break;
                }
            }
            if (blocked || !SeaFloraFluid.isSourceBlock(level, p)) continue;

            BlockState plant = random.nextInt(4) == 0
                    ? ModBlocks.RIFTWEED.get().defaultBlockState().setValue(BlockStateProperties.AGE_25, random.nextInt(25))
                    : ModBlocks.NILREED.get().defaultBlockState();
            if (plant.canSurvive(level, p)) {
                level.setBlock(p, plant, 3);
            }
        }
    }
}
