package net.thmrite.nullscapebeyond.client.movement;

import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.thmrite.nullscapebeyond.attribute.ModAttributes;

public final class GroundControlController implements MovementController {
    /** Vanilla scales raw input by this factor every tick. */
    private static final double INPUT_SCALE = 0.98;

    /** Multiplier from vanilla modifiers (sprint, speed/slowness effects) on the movement speed. */
    public static double speedFactor(LocalPlayer player) {
        double base = player.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue();
        return base > 1.0E-6 ? player.getSpeed() / base : 1.0;
    }

    /** Run cap in blocks/tick, vanilla modifiers included (e.g. x1.3 while sprinting). */
    public static double effectiveMaxSpeedPerTick(LocalPlayer player) {
        return player.getAttributeValue(ModAttributes.GROUND_MAX_SPEED) / 20.0 * speedFactor(player);
    }


    @Override
    public void tick(LocalPlayer player, MovementState state) {

    }

    @Override
    public void onInput(LocalPlayer player, MovementState state, Input input) {
        if (!onSolidGround(player)) return;

        // Input exactly as vanilla will use it this tick (item use slows it, then x0.98).
        double strafe = input.leftImpulse * INPUT_SCALE;
        double forward = input.forwardImpulse * INPUT_SCALE;
        if (player.isUsingItem() && !player.isPassenger()) {
            strafe *= 0.2;
            forward *= 0.2;
        }
        double lengthSqr = strafe * strafe + forward * forward;
        if (lengthSqr < 1.0E-7) return; // no input: vanilla friction handles it

        double length = Math.sqrt(lengthSqr);
        if (length > 1.0) { // vanilla normalizes inputs longer than 1
            strafe /= length;
            forward /= length;
            length = 1.0;
        }

        float yawRad = player.getYRot() * Mth.DEG_TO_RAD;
        double sin = Mth.sin(yawRad);
        double cos = Mth.cos(yawRad);
        double wx = strafe * cos - forward * sin; // world-space input, length = 'length'
        double wz = forward * cos + strafe * sin;
        double dx = wx / length;                  // unit direction
        double dz = wz / length;
        double strength = Math.min(1.0, length / INPUT_SCALE); // 1 running, 0.3 sneaking, 0.2 using item

        // Block friction exactly as vanilla reads it.
        Level level = player.level();
        BlockPos below = player.getOnPos();
        BlockState ground = level.getBlockState(below);
        float blockFriction = ground.getFriction(level, below, player);
        double drag = blockFriction * 0.91;        // applied by vanilla after moving
        if (drag < 0.05 || drag > 0.999) return;
        double vanillaRate = 1.0 - drag;

        // The acceleration vanilla is about to add (we cancel it, then replace it with ours).
        double vanillaAccel = player.getSpeed() * (0.21600002 / (blockFriction * blockFriction * blockFriction));
        double vanillaAx = vanillaAccel * wx;
        double vanillaAz = vanillaAccel * wz;

        // Undo last tick's drag to get last tick's displacement-equivalent velocity.
        Vec3 velocity = player.getDeltaMovement();
        double ux = velocity.x / drag;
        double uz = velocity.z / drag;

        double along = ux * dx + uz * dz;
        double perpX = ux - along * dx;
        double perpZ = uz - along * dz;

        double cap = effectiveMaxSpeedPerTick(player) * strength;
        double rateAccel = Mth.clamp(vanillaRate * player.getAttributeValue(ModAttributes.GROUND_ACCELERATION), 0.0, 1.0);
        double rateTurn = Mth.clamp(vanillaRate * player.getAttributeValue(ModAttributes.GROUND_TURN_SPEED), 0.0, 1.0);

        double newAlong;
        if (along > cap) {
            // Faster than running allows (external momentum): no running force, vanilla-like decay.
            newAlong = Math.max(cap, drag * along);
        } else if (along < 0.0) {
            // Moving against the input: cancel at the turn rate, then start accelerating.
            newAlong = (1.0 - rateTurn) * along + rateAccel * cap;
        } else {
            newAlong = (1.0 - rateAccel) * along + rateAccel * cap;
        }
        double keepPerp = 1.0 - rateTurn;

        double targetX = newAlong * dx + keepPerp * perpX;
        double targetZ = newAlong * dz + keepPerp * perpZ;

        player.setDeltaMovement(targetX - vanillaAx, velocity.y, targetZ - vanillaAz);
    }

    private static boolean onSolidGround(LocalPlayer player) {
        return player.onGround()
                && !player.isInWater()
                && !player.isInLava()
                && !player.isSwimming()
                && !player.onClimbable()
                && !player.isPassenger();
    }
}