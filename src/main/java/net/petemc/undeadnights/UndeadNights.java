package net.petemc.undeadnights;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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
	public static final Logger LOGGER = LogUtils.getLogger();

	public static StateSaverAndLoader serverState = null;

	public static int globalSpawnCounter = 0;

	public UndeadNights(FMLJavaModLoadingContext context) {
		var modBusGroup = context.getModBusGroup();

		// Register the commonSetup method for modloading
		FMLCommonSetupEvent.getBus(modBusGroup).addListener(this::commonSetup);
		FMLClientSetupEvent.getBus(modBusGroup).addListener(ClientModEvents::onClientSetup);

		ServerStartingEvent.BUS.addListener(this::onServerStarting);

		// Register the item to a creative tab
		BuildCreativeModeTabContentsEvent.getBus(modBusGroup).addListener(this::addCreative);

		UndeadNightsSounds.register(modBusGroup);
		ModEntities.register(modBusGroup);
		ModItems.register(modBusGroup);

		context.registerConfig(ModConfig.Type.SERVER, MainConfig.SPEC_SERVER);
		HordeConfig.loadConfig();
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {

		});
	}

	private void addCreative(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
			event.accept(ModItems.HORDE_ZOMBIE_SPAWN_EGG);
			event.accept(ModItems.ELITE_ZOMBIE_SPAWN_EGG);
			event.accept(ModItems.DEMOLITION_ZOMBIE_SPAWN_EGG);
		}
	}

	// You can use SubscribeEvent and let the Event Bus discover methods to call
	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) {
		LOGGER.info("Initializing UndeadNights Mod");
		if (UndeadNights.serverState == null) {
			UndeadNights.serverState = event.getServer().overworld().getDataStorage().computeIfAbsent(StateSaverAndLoader.createStateType());
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
			UndeadSpawner.prevNormalizedTimeOfDay = event.getServer().overworld().getDayTime() - 1;
			HordeMobsCommand.hordeZombiesCanBreakBlocks = MainConfig.getHordeZombiesCanBreakBlocks();
			HordeMobsCommand.hordeZombiesBlockBreakingTier = MainConfig.getHordeZombiesBlockBreakTier();
		}
	}

	// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
	@Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
	public static class ClientModEvents {
		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event) {
			EntityRenderers.register(ModEntities.HORDE_ZOMBIE.get(), HordeZombieRenderer::new);
			EntityRenderers.register(ModEntities.DEMOLITION_ZOMBIE.get(), DemolitionZombieRenderer::new);
			EntityRenderers.register(ModEntities.ELITE_ZOMBIE.get(), EliteZombieRenderer::new);
		}
	}
}
