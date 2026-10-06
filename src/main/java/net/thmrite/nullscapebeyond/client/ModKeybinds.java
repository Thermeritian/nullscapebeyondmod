package net.thmrite.nullscapebeyond.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;

@EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
public final class ModKeybinds {
    public static final String CATEGORY = "key.categories." + NullscapeBeyond.MODID;

    public static final KeyMapping ABILITY = new KeyMapping(
            "key.nscapebeyond.ability", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY);
    public static final KeyMapping ALT_ABILITY = new KeyMapping(
            "key.nscapebeyond.alt_ability", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY);

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(ABILITY);
        event.register(ALT_ABILITY);
    }

    private ModKeybinds() {}
}
