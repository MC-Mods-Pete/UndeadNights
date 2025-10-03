package net.petemc.undeadnights;

import net.fabricmc.api.ClientModInitializer;
import net.petemc.undeadnights.render.DemolitionZombieRenderer;
import net.petemc.undeadnights.render.EliteZombieRenderer;
import net.petemc.undeadnights.render.HordeZombieRenderer;
import net.petemc.undeadnights.entity.ModEntities;
import net.minecraft.client.render.entity.EntityRendererFactories;

public class UndeadNightsClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
        EntityRendererFactories.register(ModEntities.HORDE_ZOMBIE, HordeZombieRenderer::new);
        EntityRendererFactories.register(ModEntities.DEMOLITION_ZOMBIE, DemolitionZombieRenderer::new);
        EntityRendererFactories.register(ModEntities.ELITE_ZOMBIE, EliteZombieRenderer::new);
	}
}