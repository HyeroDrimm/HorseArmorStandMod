package com.hyerodrimm.horsearmorstandmod;

import com.hyerodrimm.horsearmorstandmod.entity.ModEntities;
import com.hyerodrimm.horsearmorstandmod.item.ModItems;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HorseArmorStandMod implements ModInitializer {
	public static final String MOD_ID = "horsearmorstandmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initialize " + MOD_ID);
		ModItems.registerModItems();
		ModEntities.registerModEntities();
	}
}