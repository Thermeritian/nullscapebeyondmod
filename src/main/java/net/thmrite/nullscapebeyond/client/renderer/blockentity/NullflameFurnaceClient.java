package net.thmrite.nullscapebeyond.client.renderer.blockentity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.BlastFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.SmokerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
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

        private Gui() {
        }

        @SubscribeEvent
        public static void onRenderBackground(ContainerScreenEvent.Render.Background event) {
            if (!(event.getContainerScreen() instanceof AbstractFurnaceScreen<?> screen)) return;
            AbstractFurnaceMenu menu = screen.getMenu();
            if (!menu.isLit() || !targetIsNullflame()) return;

            ResourceLocation background = screen instanceof BlastFurnaceScreen ? BG_BLAST
                    : screen instanceof SmokerScreen ? BG_SMOKER : BG_FURNACE;
            GuiGraphics graphics = event.getGuiGraphics();
            int x = screen.getGuiLeft() + 56;
            int y = screen.getGuiTop() + 36;
            float lit = Math.min(14, menu.getLitProgress());

            graphics.blit(background, x, y, 56, 36, 14, 14);
            graphics.enableScissor(x, (int) (y + 14 - lit), x + 14, y + 14);
            graphics.blit(ICON, x, y, 14, 14, 0.0F, 0.0F, 16, 16, 16, 16);
            graphics.disableScissor();
        }

        /**
         * The menu has no link to its block entity on the client, so use the block the player is looking at
         * (it is still the furnace they just opened: the crosshair does not move while a screen is open).
         */
        private static boolean targetIsNullflame() {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || !(mc.hitResult instanceof BlockHitResult hit)) return false;
            BlockEntity be = mc.level.getBlockEntity(hit.getBlockPos());
            return be instanceof NullflameCombustion combustion && combustion.nscapebeyond$isNullflame();
        }
    }
}
