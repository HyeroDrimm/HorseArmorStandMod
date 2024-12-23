package com.hyerodrimm.horsearmorstandmod.entity;

import com.hyerodrimm.horsearmorstandmod.HorseArmorStandMod;
import com.hyerodrimm.horsearmorstandmod.entity.custom.HorseArmorStandEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityType<HorseArmorStandEntity> HORSE_ARMOR_STAND = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(HorseArmorStandMod.MOD_ID, "horsearmorstandentity"),
            EntityType.Builder.create(HorseArmorStandEntity::new, SpawnGroup.MISC).dimensions(1.375f, 1.375f).build("horsearmorstandentity"));

    public static void registerModEntities(){
        HorseArmorStandMod.LOGGER.info("Registering Mod Entities for" + HorseArmorStandMod.MOD_ID);

        FabricDefaultAttributeRegistry.register(HORSE_ARMOR_STAND, HorseArmorStandEntity.createLivingAttributes());
    }
}
