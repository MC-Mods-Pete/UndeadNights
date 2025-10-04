package net.petemc.undeadnights;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.petemc.undeadnights.client.render.DemolitionZombieRenderer;
import net.petemc.undeadnights.client.render.EliteZombieRenderer;
import net.petemc.undeadnights.client.render.HordeZombieRenderer;
import net.petemc.undeadnights.entity.ModEntities;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = UndeadNights.MOD_ID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = UndeadNights.MOD_ID, value = Dist.CLIENT)
public class UndeadNightsClient {

    public static boolean dayCountEnabled = false;

    public UndeadNightsClient(ModContainer modContainer) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        //modContainer.registerConfig(ModConfig.Type.CLIENT, MainConfig.SPEC_CLIENT);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        UndeadNights.LOGGER.info("Initializing DayCount mod for Forge");
        EntityRenderers.register(ModEntities.HORDE_ZOMBIE.get(), HordeZombieRenderer::new);
        EntityRenderers.register(ModEntities.DEMOLITION_ZOMBIE.get(), DemolitionZombieRenderer::new);
        EntityRenderers.register(ModEntities.ELITE_ZOMBIE.get(), EliteZombieRenderer::new);
    }
}
