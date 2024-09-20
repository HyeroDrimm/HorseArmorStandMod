package com.hyerodrimm.horsearmorstandmod.item;

import com.hyerodrimm.horsearmorstandmod.HorseArmorStandMod;
import com.hyerodrimm.horsearmorstandmod.item.custom.HorseArmorStandItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item HORSE_ARMOR_STAND_ITEM  = registerItem("horsearmorstanditem", new HorseArmorStandItem(new FabricItemSettings()));

    private static void addItemsToFunctionalTabItemGroup(FabricItemGroupEntries entries){
        entries.add(HORSE_ARMOR_STAND_ITEM);
    }
    private static Item registerItem(String name, Item item){
        return Registry.register(Registries.ITEM, new Identifier(HorseArmorStandMod.MOD_ID, name), item);
    }
    public static void registerModItems(){
        HorseArmorStandMod.LOGGER.info("Registering Mod Items for " + HorseArmorStandMod.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(ModItems::addItemsToFunctionalTabItemGroup);
    }
}
