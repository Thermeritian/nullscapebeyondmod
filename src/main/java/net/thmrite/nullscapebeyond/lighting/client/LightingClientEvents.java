package net.thmrite.nullscapebeyond.lighting.client;

import java.io.IOException;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;

/** Event wiring for the coloured lighting library (client only). */
public final class LightingClientEvents {
    private LightingClientEvents() {}

    @EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
    public static final class ModBus {
        @SubscribeEvent
        static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
            event.registerShader(
                    new ShaderInstance(event.getResourceProvider(),
                            ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "colored_light"),
                            DefaultVertexFormat.POSITION),
                    LightRenderer::setShader);
        }
    }

    @EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
    public static final class GameBus {
        @SubscribeEvent
        static void onClientTick(ClientTickEvent.Post event) {
            DynamicLightEngine.INSTANCE.tick(Minecraft.getInstance());
        }

        @SubscribeEvent
        static void onRenderStage(RenderLevelStageEvent event) {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
                LightRenderer.render(event);
            }
        }

        @SubscribeEvent
        static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
            DynamicLightEngine.INSTANCE.reset();
        }
    }
}