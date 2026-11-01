package net.thmrite.nullscapebeyond.client.classcontrol;

import io.wispforest.accessories.api.AccessoriesCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thmrite.nullscapebeyond.NullscapeBeyond;
import net.thmrite.nullscapebeyond.accessory.classaccessory.DiverHelmet;
import net.thmrite.nullscapebeyond.attribute.ClassAttributes;
import net.thmrite.nullscapebeyond.client.ModKeybinds;
import net.thmrite.nullscapebeyond.server.classnetwork.DiverEventPayload;
import net.thmrite.nullscapebeyond.registry.ModItems;
import net.thmrite.nullscapebeyond.registry.ModSounds;

@EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
public final class DiverController {
    public static boolean hasClass, diving, sliding, bonked, hasNinjaBelt, hasSharkTail;
    public static boolean diveReady = true, sharkReady = true;
    public static float speed;
    public static int bonkTicks;

    private static Vec3 vel = Vec3.ZERO;
    private static Vec3 slideDir = Vec3.ZERO;
    private static float slideSpeed;
    private static int diveTicks, airSlideTicks;
    private static boolean prevAbility, prevAlt, prevJump, posing;

    @SubscribeEvent
    public static void onPlayerTickPre(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LocalPlayer p)) return;

        hasClass = DiverHelmet.isEquipped(p);
        if (!hasClass) {
            resetState();
            return;
        }

        AccessoriesCapability cap = AccessoriesCapability.get(p);
        hasNinjaBelt = cap != null && cap.isEquipped(ModItems.NINJA_BELT.get());
        hasSharkTail = cap != null && cap.isEquipped(ModItems.SHARK_TAIL.get());

        boolean ability = ModKeybinds.ABILITY.isDown();
        boolean alt = ModKeybinds.ALT_ABILITY.isDown();
        boolean jump = Minecraft.getInstance().options.keyJump.isDown();
        boolean abilityPress = ability && !prevAbility;
        boolean altPress = alt && !prevAlt && hasNinjaBelt; // ALT-ABILITY needs the Ninja Belt
        boolean jumpPress = jump && !prevJump;
        prevAbility = ability;
        prevAlt = alt;
        prevJump = jump;

        if (bonked && ++bonkTicks > 5 && p.onGround()) bonked = false;
        if (!diving && !sliding && p.onGround()) { // touching the ground replenishes the dive and the tail
            diveReady = true;
            sharkReady = true;
        }

        if (!canUse(p)) {
            if (diving || sliding) endMotion();
            return;
        }

        if (diving || sliding) {
            if (jumpPress) {
                jumpOut(p);
                return;
            }
            if (diving && hasSharkTail && sharkReady && diveTicks >= 2 && (abilityPress || altPress)) {
                sharkConvert(p);
                return;
            }
            if (diving) tickDive(p);
            else tickSlide(p);
        } else if (!bonked && diveReady && (abilityPress || altPress)) {
            startDive(p, altPress);
        }
    }

    /** After vanilla movement: landing, wall / entity contact, and the swimming pose. */
    @SubscribeEvent
    public static void onPlayerTickPost(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof LocalPlayer p) || !hasClass) return;

        if (diving) {
            double min = ClassAttributes.BONK_MIN_SPEED.of(p);
            if (speed >= min && ((p.horizontalCollision && isWall(p, vel.x, vel.z)) || hitsEntity(p))) {
                bonk(p, speed, vel.x, vel.z);
            } else if (diveTicks >= 3 && p.onGround()) {
                startSlide(p);
            }
        } else if (sliding && p.horizontalCollision) {
            if (slideSpeed >= ClassAttributes.BONK_MIN_SPEED.of(p) && isWall(p, slideDir.x, slideDir.z)) {
                bonk(p, slideSpeed, slideDir.x, slideDir.z);
            } else {
                endMotion(); // slid into something soft: just stop
            }
        }

        // Swimming pose for the dive and the slide. Vanilla recomputes the pose every tick, so set it after that.
        boolean want = diving || sliding;
        if (want) p.setPose(Pose.SWIMMING);
        if (want != posing) {
            posing = want;
            send(want ? DiverEventPayload.Kind.POSE_ON : DiverEventPayload.Kind.POSE_OFF, 0);
        }
    }

    /** No steering, jumping (read from the key directly) or sneaking while diving, sliding or bonked. */
    @SubscribeEvent
    public static void onInput(MovementInputUpdateEvent event) {
        if (!diving && !sliding && !bonked) return;
        var in = event.getInput();
        in.up = in.down = in.left = in.right = false;
        in.shiftKeyDown = false;
        in.forwardImpulse = 0;
        in.leftImpulse = 0;
        in.jumping = false;
    }

    // ---- Dive ------------------------------------------------------------------------------

    private static boolean canUse(LocalPlayer p) {
        return !p.isPassenger() && !p.isFallFlying() && !p.isSpectator() && !p.getAbilities().flying && !p.isInWater();
    }

    private static void startDive(LocalPlayer p, boolean powered) {
        Vec3 dm = p.getDeltaMovement();
        double momentum = Math.hypot(dm.x, dm.z);

        double dive;
        if (powered) { // Ninja Belt: the faster, the further
            dive = Mth.clamp(ClassAttributes.NINJA_DIVE_SPEED.of(p) + momentum * ClassAttributes.NINJA_DIVE_SCALE.of(p),
                    ClassAttributes.NINJA_DIVE_SPEED.of(p), ClassAttributes.NINJA_DIVE_MAX.of(p));
        } else {
            dive = ClassAttributes.DIVE_SPEED.of(p);
        }

        float pitch = Mth.clamp(p.getXRot(), (float) -ClassAttributes.DIVE_MAX_UP_ANGLE.of(p), 90f);
        Vec3 dir = Vec3.directionFromRotation(pitch, p.getYRot());
        vel = dir.scale(dive).add(0, ClassAttributes.DIVE_LIFT.of(p), 0);

        diving = true;
        sliding = false;
        diveReady = false;
        diveTicks = 0;
        speed = (float) Math.hypot(vel.x, vel.z);
        p.setDeltaMovement(vel);
        playLocal(p, ModSounds.DIVER_DIVE.get());
        send(DiverEventPayload.Kind.DIVE, speed);
    }

    private static void tickDive(LocalPlayer p) {
        diveTicks++;
        double drag = ClassAttributes.DIVE_DRAG.of(p);
        vel = new Vec3(vel.x * drag, vel.y * drag - ClassAttributes.DIVE_GRAVITY.of(p), vel.z * drag);
        speed = (float) Math.hypot(vel.x, vel.z);
        p.setDeltaMovement(vel);
        if (speed < ClassAttributes.DIVE_END_SPEED.of(p)) endMotion(); // the dive fizzles out: normal fall from here
    }

    /** Jump out of a dive or a slide: slight height and speed. */
    private static void jumpOut(LocalPlayer p) {
        double hx = diving ? vel.x : slideDir.x * slideSpeed;
        double hz = diving ? vel.z : slideDir.z * slideSpeed;
        double hs = Math.hypot(hx, hz);
        double out = hs * ClassAttributes.DIVE_JUMP_SPEED.of(p);
        Vec3 dir = hs > 1.0E-4 ? new Vec3(hx / hs, 0, hz / hs) : Vec3.directionFromRotation(0, p.getYRot());
        p.setDeltaMovement(dir.x * out, ClassAttributes.DIVE_JUMP_HEIGHT.of(p), dir.z * out);
        endMotion();
        playLocal(p, ModSounds.DIVER_JUMP.get());
        send(DiverEventPayload.Kind.JUMP_OUT, (float) out);
    }

    /** Shark Tail: horizontal speed becomes vertical speed; ends the dive. */
    private static void sharkConvert(LocalPlayer p) {
        double hs = Math.hypot(vel.x, vel.z);
        double keep = ClassAttributes.SHARK_TAIL_KEEP.of(p);
        p.setDeltaMovement(vel.x * keep, hs * ClassAttributes.SHARK_TAIL_RATIO.of(p), vel.z * keep);
        sharkReady = false;
        endMotion();
    }

    // ---- Slide ------------------------------------------------------------------------------

    private static void startSlide(LocalPlayer p) {
        double hs = Math.hypot(vel.x, vel.z);
        diving = false;
        if (hs < ClassAttributes.SLIDE_END_SPEED.of(p)) return;
        slideDir = new Vec3(vel.x / hs, 0, vel.z / hs);
        slideSpeed = (float) (hs * ClassAttributes.SLIDE_CARRY.of(p));
        speed = slideSpeed;
        sliding = true;
        airSlideTicks = 0;
        p.resetFallDistance(); // belly-flopping onto the ground is the point of sliding
        p.setDeltaMovement(slideDir.x * slideSpeed, p.getDeltaMovement().y, slideDir.z * slideSpeed);
        Minecraft.getInstance().getSoundManager().play(new DiverSlideSound());
    }

    private static void tickSlide(LocalPlayer p) {
        slideSpeed = (float) (slideSpeed * ClassAttributes.SLIDE_FRICTION.of(p) - ClassAttributes.SLIDE_DECEL.of(p));
        speed = slideSpeed;
        if (slideSpeed < ClassAttributes.SLIDE_END_SPEED.of(p)) {
            endMotion(); // gets up
            return;
        }
        if (p.onGround()) {
            airSlideTicks = 0;
        } else if (++airSlideTicks > 3) { // slid off an edge: keep the momentum, fall normally
            p.setDeltaMovement(slideDir.x * slideSpeed, p.getDeltaMovement().y, slideDir.z * slideSpeed);
            endMotion();
            return;
        }
        p.setDeltaMovement(slideDir.x * slideSpeed, p.getDeltaMovement().y, slideDir.z * slideSpeed);
    }

    // ---- Bonk ------------------------------------------------------------------------------

    /** A wall: still blocked after hopping up one step (stairs, slabs and small edges are not walls). */
    private static boolean isWall(LocalPlayer p, double dx, double dz) {
        double len = Math.hypot(dx, dz);
        if (len < 1.0E-4) return true;
        AABB probe = p.getBoundingBox().move(dx / len * 0.5, 0.6, dz / len * 0.5);
        return !p.level().noCollision(p, probe);
    }

    private static boolean hitsEntity(LocalPlayer p) {
        return !p.level().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(0.3),
                e -> e != p && e.isAlive() && !e.isSpectator()).isEmpty();
    }

    /** Same recoil as the Charger (shared BONK attributes): pushed back by impact speed, no control until landing. */
    private static void bonk(LocalPlayer p, float impact, double dx, double dz) {
        endMotion();
        playLocal(p, ModSounds.CHARGER_BONK.get());
        send(DiverEventPayload.Kind.BONK, impact);

        double res = ClassAttributes.BONK_RESISTANCE.of(p);
        if (res >= 1.0) return;

        double min = Math.max(0.01, ClassAttributes.BONK_MIN_SPEED.of(p));
        double push = ClassAttributes.BONK_KNOCKBACK.of(p) * Math.pow(impact / min, ClassAttributes.BONK_EXPONENT.of(p));
        push = Math.min(push, ClassAttributes.BONK_MAX_KNOCKBACK.of(p)) * (1.0 - res);

        double len = Math.max(1.0E-4, Math.hypot(dx, dz));
        p.setDeltaMovement(-dx / len * push, 0.35, -dz / len * push);
        bonked = true;
        bonkTicks = 0;
    }

    // ---- Helpers ------------------------------------------------------------------------------

    private static void endMotion() {
        diving = false;
        sliding = false; // DiverSlideSound watches this flag and stops itself
        speed = 0;
    }

    private static void resetState() {
        diving = sliding = bonked = hasNinjaBelt = hasSharkTail = false;
        diveReady = sharkReady = true;
        speed = 0;
        bonkTicks = diveTicks = airSlideTicks = 0;
        prevAbility = prevAlt = prevJump = false;
        if (posing) {
            posing = false;
            send(DiverEventPayload.Kind.POSE_OFF, 0);
        }
    }

    private static void playLocal(LocalPlayer p, SoundEvent sound) {
        p.level().playLocalSound(p.getX(), p.getY(), p.getZ(), sound, SoundSource.PLAYERS, 1.0f, 1.0f, false);
    }

    private static void send(DiverEventPayload.Kind kind, float value) {
        PacketDistributor.sendToServer(new DiverEventPayload(kind, value));
    }

    private DiverController() {}
}
