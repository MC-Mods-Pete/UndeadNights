package net.petemc.undeadnights;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.petemc.undeadnights.command.ModCommands;
import net.petemc.undeadnights.config.HordeConfig;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.effect.ModEffects;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.EliteZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import net.petemc.undeadnights.entity.ModEntities;
import net.petemc.undeadnights.event.ModServerEntityEvents;
import net.petemc.undeadnights.event.ModServerLifecycleEvents;
import net.petemc.undeadnights.item.ModCreativeModeTabs;
import net.petemc.undeadnights.item.ModItems;
import net.petemc.undeadnights.potion.ModPotions;
import net.petemc.undeadnights.sound.UndeadNightsSounds;
import net.petemc.undeadnights.util.StateSaverAndLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UndeadNights implements ModInitializer {
	public static final String MOD_ID = "undeadnights";
	public static final String MOD_NAME = "Undead Nights";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static StateSaverAndLoader serverState = null;

	public static int globalSpawnCounter = 0;

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing the {} Mod", MOD_NAME);
		MainConfig.init();
		HordeConfig.loadConfig();
		ModItems.registerItems();
		ModServerEntityEvents.registerEvents();
		ModServerLifecycleEvents.registerEvents();
		ModCommands.registerCommands();
		UndeadNightsSounds.registerSounds();
		ModEffects.registerEffects();
		ModEntities.initModEntities();
		ModPotions.registerPotions();
		ModCreativeModeTabs.registerItemGroups();
		FabricDefaultAttributeRegistry.register(ModEntities.HORDE_ZOMBIE, HordeZombieEntity.createHordeZombieAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.DEMOLITION_ZOMBIE, DemolitionZombieEntity.createHordeZombieAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.ELITE_ZOMBIE, EliteZombieEntity.createHordeZombieAttributes());
	}
}