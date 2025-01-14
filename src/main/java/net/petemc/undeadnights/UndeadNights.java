package net.petemc.undeadnights;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.petemc.undeadnights.client.render.DemolitionZombieRenderer;
import net.petemc.undeadnights.client.render.EliteZombieRenderer;
import net.petemc.undeadnights.client.render.HordeZombieRenderer;
import net.petemc.undeadnights.entity.ModEntities;
import net.petemc.undeadnights.sound.UndeadNightsSounds;
import net.petemc.undeadnights.util.StateSaverAndLoader;
import org.slf4j.Logger;

@Mod(UndeadNights.MOD_ID)
public class UndeadNights {
	public static final String MOD_ID = "undeadnights";
	public static final Logger LOGGER = LogUtils.getLogger();

	public static StateSaverAndLoader serverState = null;
	//public static boolean hordeNight = false;

	public static int globalSpawnCounter = 0;

	public UndeadNights() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		//ModCreativeModTabs.register(modEventBus);

		//ModItems.register(modEventBus);
		//ModBlocks.register(modEventBus);

		//ModLootModifiers.register(modEventBus);
		//ModVillagers.register(modEventBus);

		UndeadNightsSounds.register(modEventBus);
		ModEntities.register(modEventBus);

		modEventBus.addListener(this::commonSetup);

		MinecraftForge.EVENT_BUS.register(this);
		//modEventBus.addListener(this::addCreative);
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SPEC_SERVER);
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {

		});
	}

	private void addCreative(BuildCreativeModeTabContentsEvent event) {
		if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {

		}
	}

	// You can use SubscribeEvent and let the Event Bus discover methods to call
	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) {

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
/*
public class UndeadNights implements ModInitializer {
	public static final String MOD_ID = "undeadnights";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static boolean hordeNight = false;

	public static int globalSpawnCounter = 0;

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Undead Nights Mod");
		UndeadNightsConfig.init();
		ServerEntityLoadEvent.registerEvent();
		ServerEntityUnLoadEvent.registerEvent();
		FabricDefaultAttributeRegistry.register(ModEntities.HORDE_ZOMBIE, HordeZombieEntity.createHordeZombieAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.DEMOLITION_ZOMBIE, DemolitionZombieEntity.createHordeZombieAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.ELITE_ZOMBIE, EliteZombieEntity.createHordeZombieAttributes());
		UndeadNightsSounds.registerSounds();
	}
}

 */