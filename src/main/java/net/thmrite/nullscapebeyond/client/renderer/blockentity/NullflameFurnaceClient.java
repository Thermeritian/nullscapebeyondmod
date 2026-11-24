package net.thmrite.nullscapebeyond.client.renderer.blockentity;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.BlastFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.SmokerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.block.entity.NullflameCombustion;

public final class NullflameFurnaceClient {
    private NullflameFurnaceClient() {
    }

    @EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
    public static final class Renderers {
        private Renderers() {
        }

        @SubscribeEvent
        static void register(EntityRenderersEvent.RegisterRenderers event) {
            event.<AbstractFurnaceBlockEntity>registerBlockEntityRenderer(BlockEntityType.FURNACE, ctx -> new NullflameFurnaceRenderer());
            event.<AbstractFurnaceBlockEntity>registerBlockEntityRenderer(BlockEntityType.BLAST_FURNACE, ctx -> new NullflameFurnaceRenderer());
            event.<AbstractFurnaceBlockEntity>registerBlockEntityRenderer(BlockEntityType.SMOKER, ctx -> new NullflameFurnaceRenderer());
        }
    }

    @EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
    public static final class Gui {
        private static final ResourceLocation ICON = ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "textures/gui/container/nullflame.png");
        private static final ResourceLocation BG_FURNACE = ResourceLocation.withDefaultNamespace("textures/gui/container/furnace.png");
        private static final ResourceLocation BG_BLAST = ResourceLocation.withDefaultNamespace("textures/gui/container/blast_furnace.png");
        private static final ResourceLocation BG_SMOKER = ResourceLocation.withDefaultNamespace("textures/gui/container/smoker.png");

        private static BlockPos lastClicked;

        private Gui() {
        }

        @SubscribeEvent
        public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
            if (event.getLevel().isClientSide) {
                lastClicked = event.getPos();
            }
        }

        @SubscribeEvent
        public static void onRenderForeground(ContainerScreenEvent.Render.Foreground event) {
            if (!(event.getContainerScreen() instanceof AbstractFurnaceScreen<?> screen)) return;
            AbstractFurnaceMenu menu = screen.getMenu();
            if (!menu.isLit() || !furnaceIsNullflame()) return;

            ResourceLocation background = screen instanceof BlastFurnaceScreen ? BG_BLAST
                    : screen instanceof SmokerScreen ? BG_SMOKER : BG_FURNACE;
            GuiGraphics graphics = event.getGuiGraphics();
            int x = 56;
            int y = 36;
            int rows = (int) Math.min(14, menu.getLitProgress() + 1);   // same height vanilla draws

            // 1) wipe vanilla's lit flame by redrawing the empty flame slot from the screen's own background
            graphics.blit(background, x, y, 56, 36, 14, 14);
            // 2) the nullflame icon (16x16 scaled to the 14x14 slot), cut from the top as the fuel runs out
            graphics.enableScissor(x, y + 14 - rows, x + 14, y + 14);
            graphics.blit(ICON, x, y, 14, 14, 0.0F, 0.0F, 16, 16, 16, 16);
            graphics.disableScissor();
        }

        private static boolean furnaceIsNullflame() {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return false;
            BlockPos pos = lastClicked;
            if (pos == null || !(mc.level.getBlockEntity(pos) instanceof AbstractFurnaceBlockEntity)) {
                pos = mc.hitResult instanceof BlockHitResult hit ? hit.getBlockPos() : null;   // fallback: what the crosshair is on
            }
            return pos != null && mc.level.getBlockEntity(pos) instanceof NullflameCombustion combustion && combustion.nscapebeyond$isNullflame();
        }
    }
}
