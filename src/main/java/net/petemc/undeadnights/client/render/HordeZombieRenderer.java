package net.petemc.undeadnights.client.render;

import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.petemc.undeadnights.entity.HordeZombieEntity;

@OnlyIn(Dist.CLIENT)
public class HordeZombieRenderer
        extends AbstractZombieRenderer<HordeZombieEntity, ZombieModel<HordeZombieEntity>> {

    //private static final Identifier TEXTURE = Identifier.of(UndeadNights.MOD_ID,"textures/entity/customzombie.png");

    public HordeZombieRenderer(EntityRendererProvider.Context pContext) {
        this(pContext, ModelLayers.ZOMBIE, ModelLayers.ZOMBIE_INNER_ARMOR, ModelLayers.ZOMBIE_OUTER_ARMOR);
    }

    public HordeZombieRenderer(EntityRendererProvider.Context pContext, ModelLayerLocation pZombieLayer, ModelLayerLocation pInnerArmor, ModelLayerLocation pOuterArmor) {
        super(pContext, new ZombieModel<>(pContext.bakeLayer(pZombieLayer)), new ZombieModel<>(pContext.bakeLayer(pInnerArmor)), new ZombieModel<>(pContext.bakeLayer(pOuterArmor)));
    }
}
