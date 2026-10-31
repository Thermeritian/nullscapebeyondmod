package net.thmrite.nullscapebeyond.client.renderer.accessory;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import io.wispforest.accessories.api.client.AccessoryRenderer;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.thmrite.nullscapebeyond.accessory.GeoAccessoryItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;

public class GeoAccessoryRenderer implements AccessoryRenderer {
    private static final EquipmentSlot SLOT = EquipmentSlot.CHEST;

    @Override
    @SuppressWarnings("unchecked")
    public <M extends LivingEntity> void render(ItemStack stack, SlotReference reference, PoseStack matrices,
                                                EntityModel<M> model, MultiBufferSource buffers, int light,
                                                float limbSwing, float limbSwingAmount, float partialTicks,
                                                float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(model instanceof HumanoidModel<?> humanoid) || !(stack.getItem() instanceof GeoAccessoryItem item)) return;

        LivingEntity wearer = reference.entity();
        HumanoidModel<?> provided = GeoRenderProvider.of(stack)
                .getGeoArmorRenderer(wearer, stack, SLOT, (HumanoidModel<LivingEntity>) humanoid);
        if (!(provided instanceof AccessoryArmorRenderer<?>)) return;

        AccessoryArmorRenderer<GeoAccessoryItem> geo = (AccessoryArmorRenderer<GeoAccessoryItem>) provided;
        geo.prepForRender(wearer, stack, SLOT, humanoid);
        VertexConsumer vc = buffers.getBuffer(RenderType.armorCutoutNoCull(geo.getTextureLocation(item)));
        geo.renderToBuffer(matrices, vc, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
    }
}
