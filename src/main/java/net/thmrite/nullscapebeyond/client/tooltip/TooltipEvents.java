package net.thmrite.nullscapebeyond.client.tooltip;

import com.mojang.datafixers.util.Either;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.accessory.TooltipAccessory;
import net.thmrite.nullscapebeyond.registry.AccessoryTooltipData;

public final class TooltipEvents {
    /** Game bus: swap the whole vanilla tooltip for our component, and recolour the frame. */
    @EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
    public static final class Game {
        @SubscribeEvent
        public static void gather(RenderTooltipEvent.GatherComponents event) {
            if (!(event.getItemStack().getItem() instanceof TooltipAccessory)) return;
            var elements = event.getTooltipElements();
            elements.clear(); // also drops the Accessories mod's own lines; we show the slot ourselves
            elements.add(Either.<FormattedText, TooltipComponent>right(new AccessoryTooltipData(event.getItemStack())));
        }

        @SubscribeEvent
        public static void tick(ClientTickEvent.Post event) {
            PreviewDummy.tick();
        }

        @SubscribeEvent
        public static void colors(RenderTooltipEvent.Color event) {
            if (!(event.getItemStack().getItem() instanceof TooltipAccessory)) return;
            event.setBackgroundStart(0xF0140A24);
            event.setBackgroundEnd(0xF0140A24);
            event.setBorderStart(0xFF6A35C0);
            event.setBorderEnd(0xFF3A1C7A);
        }
    }

    /** Mod bus: tell the game how to draw AccessoryTooltipData. */
    @EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
    public static final class Mod {
        @SubscribeEvent
        public static void factories(RegisterClientTooltipComponentFactoriesEvent event) {
            event.register(AccessoryTooltipData.class, AccessoryTooltipComponent::new);
        }
    }

    private TooltipEvents() {}
}
