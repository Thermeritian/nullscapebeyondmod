package net.thmrite.nullscapebeyond.client.movement;

import java.util.List;

import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.client.classcontrol.*;

@EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
public final class MovementHandler {
    public static final MovementState STATE = new MovementState();

    private static final List<MovementController> CONTROLLERS = List.of(
            new GroundControlController(),
            new AirControlController()
    );

    private static final List<MovementAbility> ABILITIES = List.of(
            new DoubleJumpAbility()
    );

    private MovementHandler() {}

    private static boolean isSuppressed(LocalPlayer player) {
        return player.isSpectator()
                || player.getAbilities().flying
                || player.isFallFlying();
    }

    private static boolean classDrivesMovement() {
        return ChargerController.drivesMovement() || DiverController.drivesMovement();
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LocalPlayer player)) return;

        STATE.update(player);

        if (!isSuppressed(player)) {
            if (!classDrivesMovement()) {
                for (MovementController controller : CONTROLLERS) {
                    controller.tick(player, STATE);
                }
            }
            for (MovementAbility ability : ABILITIES) {
                ability.tick(player, STATE);
            }
        }

        STATE.endTick(player);
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!(event.getEntity() instanceof LocalPlayer player) || isSuppressed(player) || classDrivesMovement()) return;

        for (MovementController controller : CONTROLLERS) {
            controller.onInput(player, STATE, event.getInput());
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        STATE.reset();
    }

    @SubscribeEvent
    public static void onPlayerClone(ClientPlayerNetworkEvent.Clone event) {
        STATE.reset();
    }
}