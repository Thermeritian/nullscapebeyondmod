package net.thmrite.nullscapebeyond.accessory;

/**
 * Implemented by every accessory that uses the custom tooltip.
 * Texts come from lang keys derived from the item's description id (e.g. item.nscapebeyond.charger_boots):
 *   <id>          name (yellow for Class accessories)
 *   <id>.flavor   gray italic flavour text next to the name
 *   <id>.desc     description; %1$s = Ability key, %2$s = Alt-Ability key; "\n" makes a new line
 */
public interface TooltipAccessory {
    /** Slot type name shown as "Slot: X" (head, torso, legs, feet, extra). */
    String tooltipSlot();

    /** RGB colour of the item name. */
    int tooltipColor();
}
