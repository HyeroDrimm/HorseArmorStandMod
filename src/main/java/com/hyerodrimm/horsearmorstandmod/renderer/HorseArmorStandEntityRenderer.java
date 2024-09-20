package com.hyerodrimm.horsearmorstandmod.renderer;

import com.hyerodrimm.horsearmorstandmod.HorseArmorStandMod;
import com.hyerodrimm.horsearmorstandmod.entity.custom.HorseArmorStandEntity;
import com.hyerodrimm.horsearmorstandmod.model.HorseArmorStandEntityModel;
import com.hyerodrimm.horsearmorstandmod.renderer.layer.HorseArmorStandArmorRenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ArmorItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class HorseArmorStandEntityRenderer extends GeoEntityRenderer<HorseArmorStandEntity> {

    public HorseArmorStandEntityRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new HorseArmorStandEntityModel());
        this.addRenderLayer(new HorseArmorStandArmorRenderLayer(this));
    }

    @Override
    public Identifier getTextureLocation(HorseArmorStandEntity animatable) {
        return new Identifier(HorseArmorStandMod.MOD_ID, "textures/entity/horsearmorstandentity.png");
    }

    @Override
    public void render(HorseArmorStandEntity entity, float entityYaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {
        if (entity.isBaby()){
            poseStack.scale(0.4f,0.4f,0.4f);
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public boolean hasLabel(HorseArmorStandEntity animatable) {
        return !(!animatable.shouldRenderName() && (!animatable.hasCustomName() || animatable != this.dispatcher.targetedEntity));
    }
}
