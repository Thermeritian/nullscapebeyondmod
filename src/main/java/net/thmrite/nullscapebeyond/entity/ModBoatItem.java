package net.thmrite.nullscapebeyond.entity;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Copy of vanilla BoatItem (whose getBoat is private) that spawns our own boat entity type. */
public class ModBoatItem extends Item {
    private static final Predicate<Entity> ENTITY_PREDICATE = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);
    private final Supplier<? extends EntityType<? extends Boat>> type;

    public ModBoatItem(Supplier<? extends EntityType<? extends Boat>> type, Properties properties) {
        super(properties);
        this.type = type;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        HitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hit.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(stack);
        }
        Vec3 view = player.getViewVector(1.0F);
        List<Entity> nearby = level.getEntities(player, player.getBoundingBox().expandTowards(view.scale(5.0)).inflate(1.0), ENTITY_PREDICATE);
        if (!nearby.isEmpty()) {
            Vec3 eye = player.getEyePosition();
            for (Entity entity : nearby) {
                AABB box = entity.getBoundingBox().inflate(entity.getPickRadius());
                if (box.contains(eye)) {
                    return InteractionResultHolder.pass(stack);
                }
            }
        }
        if (hit.getType() == HitResult.Type.BLOCK) {
            Boat boat = type.get().create(level);
            if (boat == null) {
                return InteractionResultHolder.fail(stack);
            }
            Vec3 loc = hit.getLocation();
            boat.setPos(loc.x, loc.y, loc.z);
            boat.xo = loc.x;
            boat.yo = loc.y;
            boat.zo = loc.z;
            boat.setYRot(player.getYRot());
            if (!level.noCollision(boat, boat.getBoundingBox())) {
                return InteractionResultHolder.fail(stack);
            }
            if (!level.isClientSide) {
                level.addFreshEntity(boat);
                level.gameEvent(player, GameEvent.ENTITY_PLACE, loc);
                stack.consume(1, player);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.pass(stack);
    }
}
