package net.petemc.undeadnights;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.petemc.undeadnights.client.render.DemolitionZombieRenderer;
import net.petemc.undeadnights.client.render.HordeZombieRenderer;
import net.petemc.undeadnights.entity.ModEntities;

public class UndeadNightsClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntities.HORDE_ZOMBIE, HordeZombieRenderer::new);
		EntityRendererRegistry.register(ModEntities.DEMOLITION_ZOMBIE, DemolitionZombieRenderer::new);
	}
}