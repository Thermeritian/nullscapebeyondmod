package net.thmrite.nullscapebeyond.attribute;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thmrite.nullscapebeyond.NullscapeBeyond;

@EventBusSubscriber(modid = NullscapeBeyond.MODID, value = Dist.CLIENT)
public final class ClassAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, NullscapeBeyond.MODID);

    private static final List<Entry> ENTRIES = new ArrayList<>();

    public static final String SHARED = "SHARED";

    public static final String DIVER = "DIVER";
    public static final String CHARGER = "CHARGER";

    public static final String BONK = "BONK";
    public static final String SLIDE = "SLIDE";
    public static final String KNOCK = "KNOCK";

    public static final String NINJA_BELT = "NINJA BELT";
    public static final String SHARK_TAIL = "SHARK TAIL";
    public static final String SHIELD = "SHIELD";

    public static final String ACCESSORIES = "ACCESSORIES"; // not a CLASS attribute

    public record Entry(String id, String section, String label, double def,
                        DeferredHolder<Attribute, Attribute> holder) {
        public double of(LivingEntity e) {
            return e.getAttributeValue(holder);
        }
    }

    // ---- Shared by every Class accessory --------------------------------------------------
    public static final Entry CLASS_COOLDOWN       = reg(SHARED,  "class_cooldown",       "Ability Cooldown (t)", 20,   0,     1200);
    public static final Entry STAMINA_MAX          = reg(SHARED,  "class_stamina_max",    "Stamina Max",          100,  1,     1000);
    public static final Entry STAMINA_REGEN        = reg(SHARED,  "class_stamina_regen",  "Stamina Regen (/t)",   0.8,  0,     50);
    public static final Entry STAMINA_REGEN_DELAY  = reg(SHARED,  "class_stamina_regen_delay", "Regen Delay (t)", 20,   0,     400);

    // Diver
    public static final Entry DIVE_SPEED        = reg(DIVER, "dive_speed",         "Dive Speed (b/t)",       0.85, 0.05, 5);
    public static final Entry DIVE_LIFT         = reg(DIVER, "dive_lift", "Dive Lift (b/t)", 0.3, 0, 1);
    public static final Entry DIVE_DRAG         = reg(DIVER, "dive_drag",          "Dive Drag (x/t)",        0.97, 0.5,  1);
    public static final Entry DIVE_GRAVITY         = reg(DIVER, "dive_gravity",          "Dive Gravity (b/t)",      0.05, 0, 0.2);
    public static final Entry DIVE_FALL_MULTIPLIER = reg(DIVER, "dive_fall_multiplier",  "Dive Fall Gravity (x)",   1.8,  1, 5);
    public static final Entry DIVE_END_SPEED    = reg(DIVER, "dive_end_speed",     "Dive End Speed (b/t)",   0.3,  0,    2);
    public static final Entry DIVE_JUMP_HEIGHT  = reg(DIVER, "dive_jump_height",   "Dive Jump Height (b/t)", 0.42, 0,    2);
    public static final Entry DIVE_MAX_UP_ANGLE = reg(DIVER, "dive_max_up_angle", "Dive Max Up Angle", 0, 0, 90);
    public static final Entry DIVE_JUMP_SPEED   = reg(DIVER, "dive_jump_speed",    "Dive Jump Speed (x)",    1.15, 0,    3);
    public static final Entry DIVE_STAMINA_COST = reg(DIVER,       "dive_stamina_cost",       "Dive Stamina",          15, 0, 100);
    // Diver (Ninja Belt)
    public static final Entry NINJA_DIVE_SPEED  = reg(NINJA_BELT, "ninja_dive_speed", "Powered Dive Base (b/t)",  1.6, 0.05, 6);
    public static final Entry NINJA_DIVE_SCALE  = reg(NINJA_BELT, "ninja_dive_scale", "Powered Dive Momentum (x)", 0.8, 0,    5);
    public static final Entry NINJA_DIVE_MAX    = reg(NINJA_BELT, "ninja_dive_max",   "Powered Dive Max (b/t)",   3.0, 0.1,  8);
    public static final Entry NINJA_DIVE_STAMINA_COST = reg(NINJA_BELT,  "ninja_dive_stamina_cost", "Powered Dive Stamina",  20, 0, 100);
    // Diver (Shark Tail)
    public static final Entry SHARK_TAIL_RATIO  = reg(SHARK_TAIL, "shark_tail_ratio", "Shark Tail Conversion (x)", 1.0, 0,   3);
    public static final Entry SHARK_TAIL_KEEP   = reg(SHARK_TAIL, "shark_tail_keep",  "Shark Tail Kept Speed (x)",  0.0, 0,   1);
    public static final Entry SHARK_TAIL_STAMINA_COST = reg(SHARK_TAIL,  "shark_tail_stamina_cost", "Shark Tail Stamina",    5, 0, 100);

    // Charger
    public static final Entry CHARGE_SPEED         = reg(CHARGER, "charge_speed",         "Charge Speed (b/t)",   0.8,  0.05,  5);
    public static final Entry CHARGE_ACCELERATION  = reg(CHARGER, "charge_acceleration",  "Charge Accel (0-1)",   0.12, 0.001, 1);
    public static final Entry CHARGE_TURN_SPEED    = reg(CHARGER, "charge_turn_speed",    "Charge Turn (deg/t)",  2.5,  0,     90);
    public static final Entry CHARGE_STAMINA_COST  = reg(CHARGER, "charge_stamina_cost",  "Charge Stamina (/t)",  1.5,  0,     100);
    public static final Entry PLATFORM_DURATION    = reg(CHARGER, "charge_platform_duration", "Platform Time (t)", 20,   0,     400);
    public static final Entry QUICKDROP_SPEED      = reg(CHARGER, "charge_quickdrop_speed", "Quickdrop (b/t)",    1.2,  0,     5);
    public static final Entry WATER_SKIP_RATIO     = reg(CHARGER, "charge_water_skip_ratio", "Water Skip Speed (0-1)", 0.97, 0,    1);
    // Charger (Ninja Belt)
    public static final Entry NINJA_LONG_JUMP_SPEED    = reg(NINJA_BELT, "ninja_long_jump_speed",    "Long Jump Speed (x)",    1.3,  0, 4);
    public static final Entry NINJA_LONG_JUMP_HEIGHT   = reg(NINJA_BELT, "ninja_long_jump_height",   "Long Jump Height (b/t)", 0.5,  0, 2);
    // Charger (Shark Tail)
    public static final Entry SHARK_TAIL_RISE          = reg(SHARK_TAIL, "shark_tail_rise_speed",    "Platform Rise (b/t)",    0.05, 0, 0.5);

    //Bonking
    public static final Entry BONK_MIN_SPEED       = reg(BONK,    "bonk_min_speed",       "Bonk Min Speed (b/t)", 0.5,  0,     5);
    public static final Entry BONK_KNOCKBACK       = reg(BONK,    "bonk_knockback",       "Bonk Knockback",       0.4,  0,     5);
    public static final Entry BONK_EXPONENT        = reg(BONK,    "bonk_exponent",        "Bonk Exponent",        2.0,  0,     5);
    public static final Entry BONK_MAX_KNOCKBACK   = reg(BONK,    "bonk_max_knockback",   "Bonk Max Knockback",   2.0,  0,     10);
    public static final Entry BONK_RESISTANCE      = reg(BONK,    "bonk_resistance",      "Bonk Resist (0-1)",    0.0,  0,     1);
    public static final Entry BONK_DAMAGE          = reg(BONK,    "bonk_damage",          "Bonk Damage",          0.0,  0,     40);

    // Sliding
    public static final Entry SLIDE_CARRY       = reg(SLIDE, "slide_carry",        "Slide Carry (x)",        0.9,  0,    1.5);
    public static final Entry SLIDE_FRICTION    = reg(SLIDE, "slide_friction",     "Slide Friction (x/t)",   0.95, 0.5,  1);
    public static final Entry SLIDE_DECEL       = reg(SLIDE, "slide_decel",        "Slide Decel (b/t)",      0.004, 0,   0.1);
    public static final Entry SLIDE_END_SPEED   = reg(SLIDE, "slide_end_speed",    "Slide End Speed (b/t)",  0.1,  0,    1);

    //Knocking
    public static final Entry KNOCK_MIN_SPEED      = reg(KNOCK,   "knock_min_speed",      "Knock Min Speed (b/t)", 0.4, 0,     5);
    public static final Entry KNOCK_STRENGTH       = reg(KNOCK,   "knock_strength",       "Knock Strength",       1.2,  0,     10);
    public static final Entry KNOCK_DAMAGE         = reg(KNOCK,   "knock_damage",         "Knock Damage",         0.0,  0,     100);
    public static final Entry KNOCK_UPWARD         = reg(KNOCK,   "knock_upward",         "Knock Upward",         0.15, 0,     2);
    public static final Entry KNOCK_HIT_COOLDOWN   = reg(KNOCK,   "knock_hit_cooldown",   "Knock Hit CD (t)",     10,   0,     200);

    // ---- Shield bash (charging while blocking with a shield) -----------------------------
    public static final Entry SHIELD_BASH_DAMAGE   = reg(SHIELD,  "shield_bash_damage",   "Bash Damage",          10.0, 0,     200);
    public static final Entry SHIELD_BASH_KNOCKBACK = reg(SHIELD, "shield_bash_knockback", "Bash Knockback",     2.5,  0,     20);
    public static final Entry SHIELD_BASH_DURABILITY = reg(SHIELD, "shield_bash_durability", "Bash Durability Dmg", 25,   0,     1000);
    public static final Entry SHIELD_BASH_FREEZE   = reg(SHIELD,  "shield_bash_freeze",   "Bash Freeze (t)",      6,    0,     40);
    public static final Entry SHIELD_BASH_UPWARD   = reg(SHIELD,  "shield_bash_upward",   "Bash Upward",          0.35, 0,     2);


    // ---- Accessory slots ----------------------------------------------------------------
    public static final Entry MAX_EXTRA_SLOTS = reg(ACCESSORIES, "max_extra_slots", "Max Extra Slots", 1, 0, 3);

    private static Entry reg(String section, String id, String label, double def, double min, double max) {
        var holder = ATTRIBUTES.register(id, () ->
                new RangedAttribute("attribute.name." + NullscapeBeyond.MODID + "." + id, def, min, max).setSyncable(true));
        Entry e = new Entry(id, section, label, def, holder);
        ENTRIES.add(e);
        return e;
    }

    public static List<Entry> entries() {
        return ENTRIES;
    }

    /** Give every attribute above to players. */
    @SubscribeEvent
    public static void attach(EntityAttributeModificationEvent event) {
        for (Entry e : ENTRIES) {
            event.add(EntityType.PLAYER, e.holder());
        }
    }

    public static void resetToDefaults(Player player, boolean clearModifiers) {
        for (Entry e : ENTRIES) {
            if (e.section().equals(ACCESSORIES)) continue;
            AttributeInstance inst = player.getAttribute(e.holder());
            if (inst == null) continue;
            inst.setBaseValue(e.def());
            if (clearModifiers) inst.removeModifiers();
        }
    }

    /** Lines for the Movement Debug "CLASS" category (section headers + "Label: value"). */
    public static List<String> debugLines(Player player) {
        List<String> out = new ArrayList<>();
        String last = "";
        for (Entry e : ENTRIES) {
            if (e.section().equals(ACCESSORIES)) continue;
            if (!e.section().equals(last)) {
                out.add("-- " + e.section() + " --");
                last = e.section();
            }
            out.add(String.format("%s: %.3f", e.label(), e.of(player)));
        }
        return out;
    }

    private ClassAttributes() {}
}
