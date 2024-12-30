package net.petemc.undeadnights;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.petemc.undeadnights.config.UndeadNightsConfig;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import net.petemc.undeadnights.entity.ModEntities;
import net.petemc.undeadnights.sound.UndeadNightsSounds;
import net.petemc.undeadnights.util.ServerEntityLoadEvent;
import net.petemc.undeadnights.util.ServerEntityUnLoadEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UndeadNights implements ModInitializer {
	public static final String MOD_ID = "undeadnights";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static boolean hordeNight = false;

	public static int globalSpawnCounter = 0;

	public static boolean printDebugMessages = false;

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Undead Nights Mod");
		UndeadNightsConfig.init();
		ServerEntityLoadEvent.registerEvent();
		ServerEntityUnLoadEvent.registerEvent();
		FabricDefaultAttributeRegistry.register(ModEntities.HORDE_ZOMBIE, HordeZombieEntity.createHordeZombieAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.DEMOLITION_ZOMBIE, DemolitionZombieEntity.createHordeZombieAttributes());
		UndeadNightsSounds.registerSounds();
	}
}