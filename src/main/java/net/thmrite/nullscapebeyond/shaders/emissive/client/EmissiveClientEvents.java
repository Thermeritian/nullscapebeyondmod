package net.thmrite.nullscapebeyond.shaders.emissive.client;

import java.io.IOException;
import java.util.Map;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;

@EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
public final class EmissiveClientEvents {
    private EmissiveClientEvents() {}

    @SubscribeEvent
    static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(NullscapeBeyond.MODID, "emissive_neon"),
                        DefaultVertexFormat.NEW_ENTITY),
                EmissiveRenderTypes::setShader);
    }

    @SubscribeEvent
    static void onModifyBaking(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();
        models.replaceAll((location, model) -> {
            boolean ours = location.id().getNamespace().equals(NullscapeBeyond.MODID);
            boolean isItem = "inventory".equals(location.variant());
            return ours && !isItem && !(model instanceof EmissiveBakedModel) ? new EmissiveBakedModel(model) : model;
        });
    }

    @SubscribeEvent
    static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) manager -> EmissiveTextures.clear());
    }
}