package net.thmrite.nullscapebeyond;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.thmrite.nullscapebeyond.client.ClientConfig;
import net.thmrite.nullscapebeyond.client.config.ModConfigScreen;

@Mod(value = NullscapeBeyond.MODID, dist = Dist.CLIENT)
public class NullscapeBeyondClient {
    public NullscapeBeyondClient(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (modContainer, parent) -> new ModConfigScreen(parent));
    }
}