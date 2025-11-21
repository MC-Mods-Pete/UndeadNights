package net.petemc.undeadnights;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
import net.petemc.undeadnights.config.difficulty.DifficultyConfig;
import net.petemc.undeadnights.config.difficulty.DifficultyConfigLoader;
import net.petemc.undeadnights.effect.ModEffects;
import net.petemc.undeadnights.entity.ModEntities;
import net.petemc.undeadnights.item.ModCreativeModeTabs;
import net.petemc.undeadnights.item.ModItems;
import net.petemc.undeadnights.potion.ModPotions;
import net.petemc.undeadnights.sound.UndeadNightsSounds;
import net.petemc.undeadnights.util.StateSaverAndLoader;
import net.petemc.undeadnights.world.spawner.HordeSpawner;
import org.slf4j.Logger;

@Mod(UndeadNights.MOD_ID)
public class UndeadNights {
	public static final String MOD_ID = "undeadnights";
	public static final String MOD_NAME = "UndeadNights";
	public static final Logger LOGGER = LogUtils.getLogger();

	public static StateSaverAndLoader serverState = null;

    public static DifficultyConfig difficultyConfig;

	public static int globalSpawnCounter = 0;
    public static boolean automaticDifficultyProgressionActive = false;

	public UndeadNights(FMLJavaModLoadingContext context) {
		IEventBus modEventBus = context.getModEventBus();

		UndeadNightsSounds.register(modEventBus);
		ModEntities.register(modEventBus);
		ModItems.register(modEventBus);
		ModEffects.register(modEventBus);
		ModPotions.register(modEventBus);
		ModCreativeModeTabs.register(modEventBus);

		// Register the commonSetup method for loading the mod
		modEventBus.addListener(this::commonSetup);

		MinecraftForge.EVENT_BUS.register(this);
		modEventBus.addListener(this::addCreative);
		context.registerConfig(ModConfig.Type.SERVER, MainConfig.SPEC_SERVER);
		HordeConfig.loadConfig();

        DifficultyConfigLoader.loadDifficultyConfig();
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
		LOGGER.info("Initializing the {} Mod", MOD_NAME);
		event.enqueueWork(() -> {
			ModEntities.initModEntities();
			PotionBrewing.addMix(Potions.AWKWARD, ModItems.SLIMY_ROTTEN_FLESH.get(), ModPotions.LURE_HORDE_POTION.get());
		});
	}

	// Add the example block item to the building blocks tab
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
            UndeadNights.serverState = StateSaverAndLoader.getServerState(event.getServer());

            // TODO remove, only for testing
            //UndeadNights.serverState.setFirstDifficultyLevelPrinted(false);
            //UndeadNights.serverState.setCurrentDifficultyLevelIndex(0);

            UndeadNights.difficultyConfig.setCurrentDifficultyLevel(UndeadNights.difficultyConfig.getDifficultyLevels().get(UndeadNights.serverState.getCurrentDifficultyLevelIndex()));
            UndeadNights.automaticDifficultyProgressionActive = MainConfig.getEnableAutomaticDifficultyProgression();

            // check if the DaysCounter in the config was changed
			if (UndeadNights.serverState.getLastMaxDaysCounter() != UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getDaysBetweenHordeNights()) {
				UndeadNights.serverState.setDaysCounter(UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getDaysBetweenHordeNights());
				UndeadNights.serverState.setLastMaxDaysCounter(UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getDaysBetweenHordeNights());
			}

			// check if the Grace Period in the config was changed
			if (UndeadNights.serverState.getLastMaxGracePeriod() != MainConfig.getGracePeriodBeforeFirstHordeNight()) {
				UndeadNights.serverState.setGracePeriod(MainConfig.getGracePeriodBeforeFirstHordeNight());
				UndeadNights.serverState.setLastMaxGracePeriod(MainConfig.getGracePeriodBeforeFirstHordeNight());
			}

			// check if the maximum number of hordes per night in the config was changed
			if (UndeadNights.serverState.getLastMaxHordesCounter() != UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getMaxHordesPerHordeNight()) {
				if (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getMaxHordesPerHordeNight() != 0) {
					UndeadNights.serverState.setHordesCounter(UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getMaxHordesPerHordeNight() + 1);
				} else {
					UndeadNights.serverState.setHordesCounter(0);
				}
				UndeadNights.serverState.setLastMaxHordesCounter(UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeNights().getMaxHordesPerHordeNight());
			}

			if (!MainConfig.getNoNaturalSpawningBeforeFirstHordeNight()) {
				UndeadNights.serverState.setIsNaturalSpawningOk(true);
			}

			if (MainConfig.getPrintDebugMessages()) {
				UndeadNights.LOGGER.info("INIT DaysCounter: {} LastMaxDaysCounter: {}", UndeadNights.serverState.getDaysCounter(), UndeadNights.serverState.getLastMaxDaysCounter());
				UndeadNights.LOGGER.info("INIT HordeNight: {} SpawnZombies: {} RespawnZombies: {}", UndeadNights.serverState.getHordeNight(), UndeadNights.serverState.getSpawnZombies(), UndeadNights.serverState.getRespawnZombies());
			    UndeadNights.LOGGER.info("INIT Difficulty level: {}", UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultyName());
            }
			HordeSpawner.hordeIdFromHordesConfig = HordeConfig.getDefaultHorde();
			//UndeadSpawner.prevNormalizedTimeOfDay = event.getServer().overworld().getDayTime() - 1;
			HordeMobsCommand.hordeZombiesCanBreakBlocks = UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().isBlockBreaking();
			HordeMobsCommand.hordeZombiesBlockBreakingTier = UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getBlockBreakingTier();
		}
	}

	// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
	@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientModEvents {
		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event) {
			EntityRenderers.register(ModEntities.HORDE_ZOMBIE.get(), HordeZombieRenderer::new);
			EntityRenderers.register(ModEntities.DEMOLITION_ZOMBIE.get(), DemolitionZombieRenderer::new);
			EntityRenderers.register(ModEntities.ELITE_ZOMBIE.get(), EliteZombieRenderer::new);
		}
	}
}
