package net.thmrite.nullscapebeyond.client.classcontrol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.accessory.classaccessory.ChargerBoots;
import net.thmrite.nullscapebeyond.attribute.ClassAttributes;
import net.thmrite.nullscapebeyond.client.ModKeybinds;
import net.thmrite.nullscapebeyond.server.classnetwork.ChargerEventPayload;
import net.thmrite.nullscapebeyond.registry.ModSounds;
import net.thmrite.nullscapebeyond.registry.ModTags;

/**
 * Client-side Charger logic. Player movement is client-authoritative in vanilla, so the charge,
 * platform, water skipping, quickdrop and bonk run here; the server is only told about events
 * (sound, knock/bash, bonk damage, platform entity).
 *
 * Controls: hold ABILITY (or ALT) on the ground to charge; keep ALT held to get the air platform when the
 * charge carries you off a ledge; press either key in mid-air (not charging) to quickdrop, and keep it held
 * to start charging on landing. At full speed you skim across water without a platform. Weak blocks
 * (tag nscapebeyond:charger_passable, e.g. leaves) never stop a charge. Charging while blocking with a
 * shield turns entity contact into a shield bash.
 */
@EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
public final class ChargerController {
    // Read-only state for the HUD / debug screen
    public static boolean hasClass, charging, platformActive, waterSkipping, bonked;
    public static float speed, stamina, maxStamina = 1f, heading, lastCommandedSpeed;
    public static int cooldown, bashFreeze;

    private static boolean staminaInit, prevAbility, prevAlt;
    private static int platformTicks, regenDelay, bonkTicks;
    private static double groundY;
    private static float bashImpact;
    private static boolean sentPlatform, quickdropping;
    private static final Map<Integer, Integer> knockCooldowns = new HashMap<>();

    /** True while the stamina bar should replace the XP bar. */
    public static boolean isActive() {
        return hasClass && (charging || cooldown > 0 || stamina < maxStamina - 0.01f);
    }

    @SubscribeEvent
    public static void onPlayerTickPre(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LocalPlayer p)) return;

        hasClass = ChargerBoots.isEquipped(p);
        if (!hasClass) {
            resetState();
            return;
        }

        maxStamina = (float) ClassAttributes.STAMINA_MAX.of(p);
        if (!staminaInit) {
            stamina = maxStamina;
            staminaInit = true;
        }
        stamina = Math.min(stamina, maxStamina);

        knockCooldowns.replaceAll((id, t) -> t - 1);
        knockCooldowns.values().removeIf(t -> t <= 0);
        if (cooldown > 0) cooldown--;
        if (bonked && ++bonkTicks > 5 && p.onGround()) bonked = false;

        boolean ability = ModKeybinds.ABILITY.isDown();
        boolean alt = ModKeybinds.ALT_ABILITY.isDown();
        boolean pressed = (ability && !prevAbility) || (alt && !prevAlt);
        prevAbility = ability;
        prevAlt = alt;
        boolean held = ability || alt;

        // Hit-stop after a shield bash: hang in place, then take the recoil.
        if (bashFreeze > 0) {
            p.setDeltaMovement(Vec3.ZERO);
            p.resetFallDistance();
            if (--bashFreeze == 0) applyBonk(p, bashImpact);
            return;
        }

        // Quickdrop tech: keep the key held through a quickdrop and the charge starts as you land.
        if (quickdropping && !charging) {
            if (p.onGround()) {
                quickdropping = false;
                if (held && !bonked && cooldown == 0 && canUse(p) && stamina >= maxStamina * 0.1f) startCharge(p);
            } else if (!canUse(p)) {
                quickdropping = false;
            }
        }

        if (!charging && pressed && !bonked && canUse(p)) {
            if (!p.onGround()) {
                quickdrop(p);
            } else if (cooldown == 0 && stamina >= maxStamina * 0.1f) {
                startCharge(p);
            }
        }

        if (charging) {
            tickCharge(p, held, alt);
        } else if (regenDelay > 0) {
            regenDelay--;
        } else {
            stamina = Math.min(maxStamina, stamina + (float) ClassAttributes.STAMINA_REGEN.of(p));
        }
    }

    /** After vanilla movement has run: detect wall (bonk) and entity (knock / shield bash) contact. */
    @SubscribeEvent
    public static void onPlayerTickPost(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof LocalPlayer p) || !charging) return;

        if (p.horizontalCollision && lastCommandedSpeed >= ClassAttributes.BONK_MIN_SPEED.of(p) && isWall(p)) {
            bonk(p);
        } else {
            knock(p);
        }
    }

    /**
     * Runs inside the player's aiStep, right before vanilla moves the player:
     * no steering or jumping while charging, bonked or frozen, and weak blocks get bypassed.
     */
    @SubscribeEvent
    public static void onInput(MovementInputUpdateEvent event) {
        if (!charging && !bonked && bashFreeze <= 0) return;
        var in = event.getInput();
        in.up = in.down = in.left = in.right = false;
        in.shiftKeyDown = false;
        in.forwardImpulse = 0;
        in.leftImpulse = 0;
        in.jumping = false;

        if (charging && event.getEntity() instanceof LocalPlayer p) passWeakBlocks(p);
    }

    // ------------------------------------------------------------------------------------

    /** Basic eligibility, ignoring water (water is handled by the skipping logic). */
    private static boolean canUseCore(LocalPlayer p) {
        return !p.isPassenger() && !p.isFallFlying() && !p.isSpectator() && !p.getAbilities().flying;
    }

    private static boolean canUse(LocalPlayer p) {
        return canUseCore(p) && !p.isInWater();
    }

    private static void startCharge(LocalPlayer p) {
        charging = true;
        heading = p.getYRot();
        Vec3 dm = p.getDeltaMovement();
        speed = (float) Math.hypot(dm.x, dm.z);
        platformTicks = (int) ClassAttributes.PLATFORM_DURATION.of(p);
        groundY = p.getY();
        playLocal(p, ModSounds.CHARGER_CHARGE.get());
        send(ChargerEventPayload.Kind.START, 0, speed);
    }

    private static void tickCharge(LocalPlayer p, boolean held, boolean alt) {
        stamina -= (float) ClassAttributes.CHARGE_STAMINA_COST.of(p);
        if (!held || stamina <= 0 || !canUseCore(p)) {
            stopCharge(p, true);
            return;
        }

        // Heading chases the camera yaw at a capped turn rate
        float diff = Mth.wrapDegrees(p.getYRot() - heading);
        float maxTurn = (float) ClassAttributes.CHARGE_TURN_SPEED.of(p);
        heading += Mth.clamp(diff, -maxTurn, maxTurn);

        // Speed eases toward the target
        float target = (float) ClassAttributes.CHARGE_SPEED.of(p);
        float accel = (float) Mth.clamp(ClassAttributes.CHARGE_ACCELERATION.of(p), 0.0, 1.0);
        speed += (target - speed) * accel;

        double rad = heading * Mth.DEG_TO_RAD;
        double dy = p.getDeltaMovement().y;

        // While on the platform dy is 0, so vanilla never reports onGround even when standing on a real block of
        // the same height. Probe for solid ground just below the feet instead.
        boolean grounded = p.onGround() || !p.level().noCollision(p, p.getBoundingBox().move(0, -0.05, 0));

        // Water skipping: at (nearly) full speed the water surface acts as ground.
        double surface = grounded ? Double.NaN : waterSurface(p);
        boolean skipping = !Double.isNaN(surface)
                && speed >= target * (float) ClassAttributes.WATER_SKIP_RATIO.of(p);
        boolean platformReady = alt && platformTicks > 0;

        // Too slow to skim and no platform to fall back on: the charge ends in the water.
        if (p.isInWater() && !skipping && !platformReady) {
            stopCharge(p, true);
            return;
        }

        platformActive = false;
        waterSkipping = false;
        if (skipping) {
            waterSkipping = true;
            groundY = surface;                                  // a platform can take over if speed drops
            platformTicks = (int) ClassAttributes.PLATFORM_DURATION.of(p);
            if (Math.abs(p.getY() - surface) > 1.0E-3) p.setPos(p.getX(), surface, p.getZ());
            p.setOnGround(true);
            p.resetFallDistance();
            dy = 0;
            for (int i = 0; i < 2; i++) {
                p.level().addParticle(ParticleTypes.SPLASH,
                        p.getX() + p.getRandom().nextGaussian() * 0.25, surface + 0.05,
                        p.getZ() + p.getRandom().nextGaussian() * 0.25, 0, 0.05, 0);
            }
        } else if (grounded) {
            groundY = p.getY();
            platformTicks = (int) ClassAttributes.PLATFORM_DURATION.of(p);
        } else if (platformReady) {
            // The platform behaves like ground at the height where the player left it:
            // snap back up to that height (leaving a ledge drops you a hair), count as grounded so vanilla
            // step-up works on small edges, and cancel fall damage.
            if (p.getY() < groundY) p.setPos(p.getX(), groundY, p.getZ());
            p.setOnGround(true);
            p.resetFallDistance();
            dy = 0;
            platformTicks--;
            platformActive = true;
        }

        if (platformActive != sentPlatform) {
            sentPlatform = platformActive;
            send(platformActive ? ChargerEventPayload.Kind.PLATFORM_START : ChargerEventPayload.Kind.PLATFORM_END,
                    0, (float) groundY);
        }

        p.setDeltaMovement(-Mth.sin((float) rad) * speed, dy, Mth.cos((float) rad) * speed);
        lastCommandedSpeed = speed;
    }

    /**
     * Height of the top of a water surface the player is at or just above (NaN if none): the surface cell must have
     * no water above it, and sit between 0.6 below and 0.4 above the feet.
     */
    private static double waterSurface(LocalPlayer p) {
        Level level = p.level();
        BlockPos feet = BlockPos.containing(p.getX(), p.getY(), p.getZ());
        for (BlockPos pos : new BlockPos[] {feet, feet.below()}) {
            FluidState fluid = level.getFluidState(pos);
            if (!fluid.is(FluidTags.WATER) || !level.getFluidState(pos.above()).isEmpty()) continue;
            double surface = pos.getY() + fluid.getHeight(level, pos);
            double rel = surface - p.getY();
            if (rel >= -0.6 && rel <= 0.4) return surface;
        }
        return Double.NaN;
    }

    private static void stopCharge(LocalPlayer p, boolean brake) {
        charging = false;
        platformActive = false;
        waterSkipping = false;
        if (sentPlatform) {
            sentPlatform = false;
            send(ChargerEventPayload.Kind.PLATFORM_END, 0, 0);
        }
        cooldown = (int) ClassAttributes.CLASS_COOLDOWN.of(p);
        regenDelay = (int) ClassAttributes.STAMINA_REGEN_DELAY.of(p);
        if (brake) {
            playLocal(p, ModSounds.CHARGER_BRAKE.get());
            send(ChargerEventPayload.Kind.BRAKE, 0, speed);
        }
    }

    private static void quickdrop(LocalPlayer p) {
        Vec3 dm = p.getDeltaMovement();
        p.setDeltaMovement(dm.x, -ClassAttributes.QUICKDROP_SPEED.of(p), dm.z);
        quickdropping = true;
    }

    // ---- Weak blocks ----------------------------------------------------------------------

    /**
     * If this tick's movement touches weak blocks (tag charger_passable), resolve the movement against the
     * remaining (strong) blocks ourselves and let vanilla apply it without collisions, so foliage never stops
     * or bonks the charge. Only runs when a weak block is actually in the way.
     */
    private static void passWeakBlocks(LocalPlayer p) {
        Vec3 dm = p.getDeltaMovement();
        Vec3 want = new Vec3(dm.x, p.onGround() ? 0 : dm.y, dm.z); // ignore the ground push while standing
        AABB box = p.getBoundingBox();
        AABB sweep = box.expandTowards(want);
        Level level = p.level();
        CollisionContext ctx = CollisionContext.of(p);
        VoxelShape sweepShape = Shapes.create(sweep);

        boolean weak = false;
        List<VoxelShape> strong = new ArrayList<>();
        for (BlockPos pos : blocksIn(sweep)) {
            BlockState state = level.getBlockState(pos);
            VoxelShape shape = state.getCollisionShape(level, pos, ctx);
            if (shape.isEmpty()) continue;
            VoxelShape moved = shape.move(pos.getX(), pos.getY(), pos.getZ());
            if (!Shapes.joinIsNotEmpty(moved, sweepShape, BooleanOp.AND)) continue;
            if (state.is(ModTags.CHARGER_PASSABLE)) weak = true;
            else strong.add(moved);
        }
        if (!weak) return;

        Vec3 allowed = clip(box, want, strong);
        p.noPhysics = true; // vanilla resets this at the start of every player tick
        p.setDeltaMovement(allowed);
        p.horizontalCollision = !Mth.equal(allowed.x, want.x) || !Mth.equal(allowed.z, want.z);
    }

    /** Every block position the box touches (BlockPos has no AABB overload in 1.21.1). */
    private static Iterable<BlockPos> blocksIn(AABB box) {
        return BlockPos.betweenClosed(
                Mth.floor(box.minX), Mth.floor(box.minY), Mth.floor(box.minZ),
                Mth.floor(box.maxX), Mth.floor(box.maxY), Mth.floor(box.maxZ));
    }

    /** Vanilla's axis-by-axis collision (Y, then the longer of X/Z first), against the given shapes only. */
    private static Vec3 clip(AABB box, Vec3 v, List<VoxelShape> shapes) {
        double x = v.x, y = v.y, z = v.z;
        if (y != 0.0) {
            y = Shapes.collide(Direction.Axis.Y, box, shapes, y);
            if (y != 0.0) box = box.move(0.0, y, 0.0);
        }
        boolean zFirst = Math.abs(x) < Math.abs(z);
        if (zFirst && z != 0.0) {
            z = Shapes.collide(Direction.Axis.Z, box, shapes, z);
            if (z != 0.0) box = box.move(0.0, 0.0, z);
        }
        if (x != 0.0) {
            x = Shapes.collide(Direction.Axis.X, box, shapes, x);
            if (!zFirst && x != 0.0) box = box.move(x, 0.0, 0.0);
        }
        if (!zFirst && z != 0.0) {
            z = Shapes.collide(Direction.Axis.Z, box, shapes, z);
        }
        return new Vec3(x, y, z);
    }

    /**
     * A real wall: still blocked after hopping up one step height (0.6) and reaching forward.
     * Stairs, slabs, tiny edges and weak blocks are free at that height, so they are not walls.
     */
    private static boolean isWall(LocalPlayer p) {
        double rad = heading * Mth.DEG_TO_RAD;
        AABB probe = p.getBoundingBox().move(-Mth.sin((float) rad) * 0.5, 0.6, Mth.cos((float) rad) * 0.5);
        return blockedByStrong(p.level(), p, probe);
    }

    private static boolean blockedByStrong(Level level, Entity entity, AABB aabb) {
        CollisionContext ctx = CollisionContext.of(entity);
        VoxelShape probe = Shapes.create(aabb);
        for (BlockPos pos : blocksIn(aabb)) {
            BlockState state = level.getBlockState(pos);
            if (state.is(ModTags.CHARGER_PASSABLE)) continue;
            VoxelShape shape = state.getCollisionShape(level, pos, ctx);
            if (shape.isEmpty()) continue;
            if (Shapes.joinIsNotEmpty(shape.move(pos.getX(), pos.getY(), pos.getZ()), probe, BooleanOp.AND)) return true;
        }
        return false;
    }

    // ---- Bonk, knock, shield bash ------------------------------------------------------------

    private static void bonk(LocalPlayer p) {
        applyBonk(p, lastCommandedSpeed);
    }

    /** Stops the charge, pushes the player back (scaled by impact speed) and locks control until landing. */
    private static void applyBonk(LocalPlayer p, float impact) {
        stopCharge(p, false);
        playLocal(p, ModSounds.CHARGER_BONK.get());
        send(ChargerEventPayload.Kind.BONK, 0, impact);

        double res = ClassAttributes.BONK_RESISTANCE.of(p);
        speed = 0;
        if (res >= 1.0) return; // fully resisted (future Helmet-style upgrade): stop, but no pushback or lock

        double min = Math.max(0.01, ClassAttributes.BONK_MIN_SPEED.of(p));
        double push = ClassAttributes.BONK_KNOCKBACK.of(p) * Math.pow(impact / min, ClassAttributes.BONK_EXPONENT.of(p));
        push = Math.min(push, ClassAttributes.BONK_MAX_KNOCKBACK.of(p)) * (1.0 - res);

        double rad = heading * Mth.DEG_TO_RAD;
        p.setDeltaMovement(Mth.sin((float) rad) * push, 0.35, -Mth.cos((float) rad) * push); // opposite of travel
        bonked = true;
        bonkTicks = 0;
    }

    private static void knock(LocalPlayer p) {
        if (lastCommandedSpeed < ClassAttributes.KNOCK_MIN_SPEED.of(p)) return;
        int cd = (int) ClassAttributes.KNOCK_HIT_COOLDOWN.of(p);
        // isBlocking() is true for any item that declares NeoForge's SHIELD_BLOCK ability, so modded shields work too.
        boolean bash = p.isBlocking();
        for (LivingEntity t : p.level().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(0.4),
                e -> e != p && e.isAlive() && !e.isSpectator())) {
            if (knockCooldowns.containsKey(t.getId())) continue;
            knockCooldowns.put(t.getId(), Math.max(1, cd));
            if (bash) {
                shieldBash(p, t);
                return;
            }
            send(ChargerEventPayload.Kind.KNOCK, t.getId(), lastCommandedSpeed);
        }
    }

    /**
     * Parry-style shield bash: swing the shield hand, random bash sound, white flash (unless photosensitive),
     * then everyone freezes for a moment. The server applies damage, shield wear and the mob's freeze; when
     * our own freeze ends, the player takes the bonk recoil.
     */
    private static void shieldBash(LocalPlayer p, LivingEntity target) {
        send(ChargerEventPayload.Kind.SHIELD_BASH, target.getId(), lastCommandedSpeed);
        p.swing(p.getUsedItemHand()); // also tells the server, which shows it to other players
        playLocal(p, ModSounds.SHIELD_BASH.get());
        BashFlash.trigger();

        bashImpact = Math.max(lastCommandedSpeed, (float) ClassAttributes.BONK_MIN_SPEED.of(p)); // never a negligible recoil
        stopCharge(p, false);
        speed = 0;
        p.setDeltaMovement(Vec3.ZERO);
        bashFreeze = (int) ClassAttributes.SHIELD_BASH_FREEZE.of(p);
        if (bashFreeze <= 0) applyBonk(p, bashImpact);
    }

    private static void resetState() {
        charging = platformActive = waterSkipping = bonked = false;
        speed = lastCommandedSpeed = 0;
        cooldown = platformTicks = regenDelay = bonkTicks = bashFreeze = 0;
        staminaInit = false;
        sentPlatform = false;
        quickdropping = false;
        knockCooldowns.clear();
    }

    private static void playLocal(LocalPlayer p, SoundEvent sound) {
        p.level().playLocalSound(p.getX(), p.getY(), p.getZ(), sound, SoundSource.PLAYERS, 1.0f, 1.0f, false);
    }

    private static void send(ChargerEventPayload.Kind kind, int targetId, float spd) {
        PacketDistributor.sendToServer(new ChargerEventPayload(kind, targetId, spd));
    }

    private ChargerController() {}
}
