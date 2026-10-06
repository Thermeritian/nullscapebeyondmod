package net.thmrite.nullscapebeyond.client.renderer;

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
import net.thmrite.nullscapebeyond.accessory.classaccessory.ChargerBoots;
import net.thmrite.nullscapebeyond.client.renderer.ChargerBootsArmorRenderer;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;

public class ChargerBootsRenderer implements AccessoryRenderer {
    @Override
    @SuppressWarnings("unchecked")
    public <M extends LivingEntity> void render(ItemStack stack, SlotReference reference, PoseStack matrices,
                                                EntityModel<M> model, MultiBufferSource buffers, int light,
                                                float limbSwing, float limbSwingAmount, float partialTicks,
                                                float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(model instanceof HumanoidModel<?> humanoid) || !(stack.getItem() instanceof ChargerBoots boots)) return;

        LivingEntity wearer = reference.entity();
        HumanoidModel<?> provided = GeoRenderProvider.of(stack)
                .getGeoArmorRenderer(wearer, stack, EquipmentSlot.FEET, (HumanoidModel<LivingEntity>) humanoid);
        if (!(provided instanceof ChargerBootsArmorRenderer geo)) return;

        geo.prepForRender(wearer, stack, EquipmentSlot.FEET, humanoid);
        VertexConsumer vc = buffers.getBuffer(RenderType.armorCutoutNoCull(geo.getTextureLocation(boots)));
        geo.renderToBuffer(matrices, vc, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
    }
}
