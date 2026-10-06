package net.thmrite.nullscapebeyond.client.classcontrol;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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


@EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
public final class ChargerController {
    // Read-only state for the HUD / debug screen
    public static boolean hasClass, charging, platformActive, bonked;
    public static float speed, stamina, maxStamina = 1f, heading, lastCommandedSpeed;
    public static int cooldown;

    private static boolean staminaInit, prevAbility, prevAlt;
    private static int platformTicks, regenDelay, bonkTicks;
    private static double groundY;
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

    /** After vanilla movement has run: detect wall (bonk) and entity (knock) contact. */
    @SubscribeEvent
    public static void onPlayerTickPost(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof LocalPlayer p) || !charging) return;

        if (p.horizontalCollision && lastCommandedSpeed >= ClassAttributes.BONK_MIN_SPEED.of(p) && isWall(p)) {
            bonk(p);
        } else {
            knock(p);
        }
    }

    /** No steering or jumping while charging or bonked. */
    @SubscribeEvent
    public static void onInput(MovementInputUpdateEvent event) {
        if (!charging && !bonked) return;
        var in = event.getInput();
        in.up = in.down = in.left = in.right = false;
        in.shiftKeyDown = false;
        in.forwardImpulse = 0;
        in.leftImpulse = 0;
        in.jumping = false;
    }

    // ------------------------------------------------------------------------------------

    private static boolean canUse(LocalPlayer p) {
        return !p.isPassenger() && !p.isFallFlying() && !p.isInWater() && !p.isSpectator()
                && !p.getAbilities().flying;
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
        if (!held || stamina <= 0 || !canUse(p)) {
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
        boolean grounded = p.onGround();
        platformActive = false;
        if (grounded) {
            groundY = p.getY();
            platformTicks = (int) ClassAttributes.PLATFORM_DURATION.of(p);
        } else if (alt && platformTicks > 0) {
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

        p.setDeltaMovement(-Mth.sin((float) rad) * speed, dy, Mth.cos((float) rad) * speed);
        lastCommandedSpeed = speed;
    }

    private static void stopCharge(LocalPlayer p, boolean brake) {
        stopLocal(p, ModSounds.CHARGER_CHARGE.get());
        charging = false;
        platformActive = false;
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
    }

    private static boolean isWall(LocalPlayer p) {
        double rad = heading * Mth.DEG_TO_RAD;
        AABB probe = p.getBoundingBox().move(-Mth.sin((float) rad) * 0.5, 0.6, Mth.cos((float) rad) * 0.5);
        return !p.level().noCollision(p, probe);
    }

    private static void bonk(LocalPlayer p) {
        float impact = lastCommandedSpeed;
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
        for (LivingEntity t : p.level().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(0.4),
                e -> e != p && e.isAlive() && !e.isSpectator())) {
            if (knockCooldowns.containsKey(t.getId())) continue;
            knockCooldowns.put(t.getId(), Math.max(1, cd));
            send(ChargerEventPayload.Kind.KNOCK, t.getId(), lastCommandedSpeed);
        }
    }

    private static void resetState() {
        charging = platformActive = bonked = false;
        speed = lastCommandedSpeed = 0;
        cooldown = platformTicks = regenDelay = bonkTicks = 0;
        staminaInit = false;
        knockCooldowns.clear();
    }

    private static void playLocal(LocalPlayer p, SoundEvent sound) {
        p.level().playLocalSound(p.getX(), p.getY(), p.getZ(), sound, SoundSource.PLAYERS, 1.0f, 1.0f, false);
    }

    private static void stopLocal(LocalPlayer p, SoundEvent sound) {
        Minecraft.getInstance().getSoundManager().stop(sound.getLocation(), SoundSource.PLAYERS);
    }

    private static void send(ChargerEventPayload.Kind kind, int targetId, float spd) {
        PacketDistributor.sendToServer(new ChargerEventPayload(kind, targetId, spd));
    }

    private ChargerController() {}
}
