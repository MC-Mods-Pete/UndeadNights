package net.petemc.undeadnights;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.petemc.undeadnights.client.render.DemolitionZombieRenderer;
import net.petemc.undeadnights.client.render.EliteZombieRenderer;
import net.petemc.undeadnights.client.render.HordeZombieRenderer;
import net.petemc.undeadnights.command.HordeMobsCommand;
import net.petemc.undeadnights.config.HordeConfig;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.entity.ModEntities;
import net.petemc.undeadnights.item.ModItems;
import net.petemc.undeadnights.sound.UndeadNightsSounds;
import net.petemc.undeadnights.util.StateSaverAndLoader;
import net.petemc.undeadnights.world.spawner.UndeadSpawner;
import org.slf4j.Logger;

@Mod(UndeadNights.MOD_ID)
public class UndeadNights {
	public static final String MOD_ID = "undeadnights";
	public static final String MOD_NAME = "UndeadNights";
	public static final Logger LOGGER = LogUtils.getLogger();

	public static StateSaverAndLoader serverState = null;

	public static int globalSpawnCounter = 0;

	public UndeadNights(IEventBus modEventBus, ModContainer modContainer) {
		UndeadNightsSounds.register(modEventBus);
		ModEntities.register(modEventBus);
		ModItems.register(modEventBus);

		// Register the commonSetup method for modloading
		modEventBus.addListener(this::commonSetup);

		// Register ourselves for server and other game events we are interested in.
		// Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
		// Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
		NeoForge.EVENT_BUS.register(this);
		modEventBus.addListener(this::addCreative);
		modContainer.registerConfig(ModConfig.Type.SERVER, MainConfig.SPEC_SERVER);
		HordeConfig.loadConfig();
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
		LOGGER.info("Initializing the {} Mod", MOD_NAME);
		event.enqueueWork(() -> {

		});
	}

	// Add the example block item to the building blocks tab
	private void addCreative(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
			event.accept(ModItems.HORDE_ZOMBIE_SPAWN_EGG.get());
			event.accept(ModItems.ELITE_ZOMBIE_SPAWN_EGG.get());
			event.accept(ModItems.DEMOLITION_ZOMBIE_SPAWN_EGG.get());
		}
	}

	// You can use SubscribeEvent and let the Event Bus discover methods to call
	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) {
		LOGGER.info("Initializing UndeadNights Mod");
		if (UndeadNights.serverState == null) {
			UndeadNights.serverState = StateSaverAndLoader.getServerState(event.getServer());
			// check if the DaysCounter in the config was changed
			if (UndeadNights.serverState.getLastMaxDaysCounter() != MainConfig.getDaysBetweenHordeNights()) {
				UndeadNights.serverState.setDaysCounter(MainConfig.getDaysBetweenHordeNights());
				UndeadNights.serverState.setLastMaxDaysCounter(MainConfig.getDaysBetweenHordeNights());
			}

			if (MainConfig.getPrintDebugMessages()) {
				UndeadNights.LOGGER.info("INIT DaysCounter: {} LastMaxDaysCounter: {}", UndeadNights.serverState.getDaysCounter(), UndeadNights.serverState.getLastMaxDaysCounter());
				UndeadNights.LOGGER.info("INIT HordeNight: {} SpawnZombies: {} RespawnZombies: {}", UndeadNights.serverState.getHordeNight(), UndeadNights.serverState.getSpawnZombies(), UndeadNights.serverState.getRespawnZombies());
			}
			UndeadSpawner.hordeToSpawn = HordeConfig.getDefaultHorde();
			HordeMobsCommand.hordeZombiesCanBreakBlocks = MainConfig.getHordeZombiesCanBreakBlocks();
			HordeMobsCommand.hordeZombiesBlockBreakingTier = MainConfig.getHordeZombiesBlockBreakTier();
		}
	}

	// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
	@EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientModEvents {
		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event) {
			EntityRenderers.register(ModEntities.HORDE_ZOMBIE.get(), HordeZombieRenderer::new);
			EntityRenderers.register(ModEntities.DEMOLITION_ZOMBIE.get(), DemolitionZombieRenderer::new);
			EntityRenderers.register(ModEntities.ELITE_ZOMBIE.get(), EliteZombieRenderer::new);
		}
	}
}
