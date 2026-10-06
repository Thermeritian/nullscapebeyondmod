package net.thmrite.nullscapebeyond.client.movement;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.thmrite.nullscapebeyond.attribute.ModAttributes;

public final class AirControlController implements MovementController {

    /** Vanilla's airborne acceleration for this player (mirrors Player#getFlyingSpeed). */
    public static float baseAirAcceleration(LocalPlayer player) {
        return player.isSprinting() ? 0.025999999F : 0.02F;
    }

    @Override
    public void tick(LocalPlayer player, MovementState state) {
        if (state.grounded || player.isInLava() || player.isPassenger()) return;

        double multiplier = player.getAttributeValue(ModAttributes.AIR_CONTROL);
        if (Math.abs(multiplier - 1.0) < 1.0E-6) return;

        // xxa / zza hold the input vanilla actually used last tick (already scaled by
        // sneaking, item use and the 0.98 factor), so we stay consistent with it.
        double strafe = player.xxa;
        double forward = player.zza;
        double lengthSqr = strafe * strafe + forward * forward;
        if (lengthSqr < 1.0E-7) return;

        // Same math as vanilla's input -> world-space conversion.
        if (lengthSqr > 1.0) {
            double inv = 1.0 / Math.sqrt(lengthSqr);
            strafe *= inv;
            forward *= inv;
        }

        double extra = (multiplier - 1.0) * baseAirAcceleration(player);
        strafe *= extra;
        forward *= extra;

        float yawRad = player.getYRot() * Mth.DEG_TO_RAD;
        double sin = Mth.sin(yawRad);
        double cos = Mth.cos(yawRad);

        Vec3 v = player.getDeltaMovement();
        player.setDeltaMovement(
                v.x + strafe * cos - forward * sin,
                v.y,
                v.z + forward * cos + strafe * sin);
    }
}