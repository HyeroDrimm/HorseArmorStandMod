package com.hyerodrimm.horsearmorstandmod.model;


import com.hyerodrimm.horsearmorstandmod.HorseArmorStandMod;
import com.hyerodrimm.horsearmorstandmod.entity.custom.HorseArmorStandEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class HorseArmorStandEntityModel extends GeoModel<HorseArmorStandEntity> {

    @Override
    public Identifier getModelResource(HorseArmorStandEntity animatable) {
        return Identifier.of(HorseArmorStandMod.MOD_ID, "geo/horsearmorstand.geo.json");
    }

    @Override
    public Identifier getTextureResource(HorseArmorStandEntity animatable) {
        return Identifier.of(HorseArmorStandMod.MOD_ID, "textures/entity/horsearmorstandentity.png");
    }

    @Override
    public Identifier getAnimationResource(HorseArmorStandEntity animatable) {
        return Identifier.of(HorseArmorStandMod.MOD_ID, "animations/horsearmorstand.animation.json");
    }
}
