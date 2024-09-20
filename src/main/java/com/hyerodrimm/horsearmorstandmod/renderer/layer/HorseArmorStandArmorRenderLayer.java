package com.hyerodrimm.horsearmorstandmod.renderer.layer;

import com.hyerodrimm.horsearmorstandmod.HorseArmorStandMod;
import com.hyerodrimm.horsearmorstandmod.entity.custom.HorseArmorStandEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.AnimalArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.math.ColorHelper;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class HorseArmorStandArmorRenderLayer extends GeoRenderLayer<HorseArmorStandEntity> {
    public HorseArmorStandArmorRenderLayer(GeoRenderer entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(MatrixStack poseStack, HorseArmorStandEntity animatable, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        ItemStack itemStack = animatable.getArmorType();

        if (!itemStack.isEmpty() && itemStack.getItem() instanceof AnimalArmorItem horseArmorItem) {
            if (horseArmorItem.getType() == AnimalArmorItem.Type.EQUESTRIAN) {
                float r;
                float b;
                float g;
                if (itemStack.isIn(ItemTags.DYEABLE)) {
                    int m = DyedColorComponent.getColor(itemStack, -6265536);
                    r = (float) ColorHelper.Argb.getRed(m) / 255.0F;
                    g = (float) ColorHelper.Argb.getGreen(m) / 255.0F;
                    b = (float) ColorHelper.Argb.getBlue(m) / 255.0F;
                } else {
                    r = 1.0f;
                    g = 1.0f;
                    b = 1.0f;
                }

                RenderLayer armorRenderLayer = RenderLayer.getArmorCutoutNoCull(horseArmorItem.getEntityTexture());
                getRenderer().reRender(getDefaultBakedModel(animatable), poseStack, bufferSource, animatable, armorRenderLayer, bufferSource.getBuffer(armorRenderLayer), partialTick, packedLight, OverlayTexture.DEFAULT_UV, r, g, b, 1);
            }
        }
    }
}
