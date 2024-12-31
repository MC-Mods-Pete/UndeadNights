package net.petemc.undeadnights.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ZombieBaseEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ZombieEntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.ZombieEntityRenderState;
import net.minecraft.util.Identifier;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;

@Environment(EnvType.CLIENT)
public class DemolitionZombieRenderer
        extends ZombieBaseEntityRenderer<DemolitionZombieEntity, ZombieEntityRenderState, ZombieEntityModel<ZombieEntityRenderState>> {

    //private static final Identifier TEXTURE = Identifier.of(UndeadNights.MOD_ID,"textures/entity/customzombie.png");

    public DemolitionZombieRenderer(EntityRendererFactory.Context ctx) {
            super(
                    ctx,
                    new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)),
                    new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE_BABY)),
                    new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE_INNER_ARMOR)),
                    new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE_OUTER_ARMOR)),
                    new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE_BABY_INNER_ARMOR)),
                    new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE_BABY_OUTER_ARMOR))
            );
        }

    @Override
    public ZombieEntityRenderState createRenderState() {
        return new ZombieEntityRenderState();
    }
}
