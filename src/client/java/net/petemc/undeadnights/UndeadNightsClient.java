package net.petemc.undeadnights;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.petemc.undeadnights.render.DemolitionZombieRenderer;
import net.petemc.undeadnights.render.EliteZombieRenderer;
import net.petemc.undeadnights.render.HordeZombieRenderer;
import net.petemc.undeadnights.entity.ModEntities;

public class UndeadNightsClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
        EntityRenderers.register(ModEntities.HORDE_ZOMBIE, HordeZombieRenderer::new);
        EntityRenderers.register(ModEntities.DEMOLITION_ZOMBIE, DemolitionZombieRenderer::new);
        EntityRenderers.register(ModEntities.ELITE_ZOMBIE, EliteZombieRenderer::new);
	}
}