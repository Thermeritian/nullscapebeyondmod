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
    public static final String CHARGER = "CHARGER";
    public static final String BONK = "BONK";
    public static final String KNOCK = "KNOCK";
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

    // ---- Charger ------------------------------------------------------------------------
    public static final Entry CHARGE_SPEED         = reg(CHARGER, "charge_speed",         "Charge Speed (b/t)",   0.8,  0.05,  5);
    public static final Entry CHARGE_ACCELERATION  = reg(CHARGER, "charge_acceleration",  "Charge Accel (0-1)",   0.12, 0.001, 1);
    public static final Entry CHARGE_TURN_SPEED    = reg(CHARGER, "charge_turn_speed",    "Charge Turn (deg/t)",  2.5,  0,     90);
    public static final Entry CHARGE_STAMINA_COST  = reg(CHARGER, "charge_stamina_cost",  "Charge Stamina (/t)",  1.5,  0,     100);
    public static final Entry PLATFORM_DURATION    = reg(CHARGER, "charge_platform_duration", "Platform Time (t)", 20,   0,     400);
    public static final Entry QUICKDROP_SPEED      = reg(CHARGER, "charge_quickdrop_speed", "Quickdrop (b/t)",    1.2,  0,     5);
    public static final Entry BONK_MIN_SPEED       = reg(BONK,    "bonk_min_speed",       "Bonk Min Speed (b/t)", 0.5,  0,     5);
    public static final Entry BONK_KNOCKBACK       = reg(BONK,    "bonk_knockback",       "Bonk Knockback",       0.4,  0,     5);
    public static final Entry BONK_EXPONENT        = reg(BONK,    "bonk_exponent",        "Bonk Exponent",        2.0,  0,     5);
    public static final Entry BONK_MAX_KNOCKBACK   = reg(BONK,    "bonk_max_knockback",   "Bonk Max Knockback",   2.0,  0,     10);
    public static final Entry BONK_RESISTANCE      = reg(BONK,    "bonk_resistance",      "Bonk Resist (0-1)",    0.0,  0,     1);
    public static final Entry BONK_DAMAGE          = reg(BONK,    "bonk_damage",          "Bonk Damage",          0.0,  0,     40);
    public static final Entry KNOCK_MIN_SPEED      = reg(KNOCK,   "knock_min_speed",      "Knock Min Speed (b/t)", 0.4, 0,     5);
    public static final Entry KNOCK_STRENGTH       = reg(KNOCK,   "knock_strength",       "Knock Strength",       1.2,  0,     10);
    public static final Entry KNOCK_DAMAGE         = reg(KNOCK,   "knock_damage",         "Knock Damage",         0.0,  0,     100);
    public static final Entry KNOCK_UPWARD         = reg(KNOCK,   "knock_upward",         "Knock Upward",         0.15, 0,     2);
    public static final Entry KNOCK_HIT_COOLDOWN   = reg(KNOCK,   "knock_hit_cooldown",   "Knock Hit CD (t)",     10,   0,     200);

    // ---- Accessory slots ----------------------------------------------------------------
    public static final Entry MAX_EXTRA_SLOTS      = reg(ACCESSORIES, "max_extra_slots",  "Max Extra Slots",      2,    0,     16);

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

    /**
     * Restores every CLASS attribute to its default base value.
     * Existing players keep their saved base values, so this is also how you pick up
     * changed defaults after editing the numbers above.
     */
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
