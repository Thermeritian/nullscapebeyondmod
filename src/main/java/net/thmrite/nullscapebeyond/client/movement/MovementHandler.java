package net.thmrite.nullscapebeyond.client.movement;

import java.util.List;

import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.thmrite.nullscapebeyond.NullscapeBeyond;

/**
 * Central loop: updates the state, then runs every registered ability.
 * Runs on PlayerTickEvent.Pre so velocity changes apply within the same tick.
 */
@EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
public final class MovementHandler {
    public static final MovementState STATE = new MovementState();

    /** Add new abilities here. Order matters when two abilities touch the same velocity. */
    private static final List<MovementAbility> ABILITIES = List.of(
            new DoubleJumpAbility()
    );

    private MovementHandler() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LocalPlayer player)) return;

        STATE.update(player);

        boolean suppressed = player.isSpectator()
                || player.getAbilities().flying
                || player.isFallFlying();

        if (!suppressed) {
            for (MovementAbility ability : ABILITIES) {
                ability.tick(player, STATE);
            }
        }

        STATE.endTick(player);
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        STATE.reset();
    }

    /** Fires when the local player is replaced (respawn, dimension change). */
    @SubscribeEvent
    public static void onPlayerClone(ClientPlayerNetworkEvent.Clone event) {
        STATE.reset();
    }
}