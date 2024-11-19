package net.petemc.undeadnights.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ZombieBaseEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ZombieEntityModel;
import net.petemc.undeadnights.entity.HordeZombieEntity;

@Environment(EnvType.CLIENT)
public class HordeZombieRenderer
        extends ZombieBaseEntityRenderer<HordeZombieEntity, ZombieEntityModel<HordeZombieEntity>> {

    // private static final Identifier TEXTURE = Identifier.of(UndeadNights.MOD_ID,"textures/entity/customzombie.png");

    public HordeZombieRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)), new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE_INNER_ARMOR)), new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE_OUTER_ARMOR)));
    }
}
