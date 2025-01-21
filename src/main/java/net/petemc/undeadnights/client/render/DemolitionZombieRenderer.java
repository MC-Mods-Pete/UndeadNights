package net.petemc.undeadnights.client.render;


import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;

@OnlyIn(Dist.CLIENT)
public class DemolitionZombieRenderer
        extends AbstractZombieRenderer<DemolitionZombieEntity, ZombieModel<DemolitionZombieEntity>> {

    public DemolitionZombieRenderer(EntityRendererProvider.Context pContext) {
        this(pContext, ModelLayers.ZOMBIE, ModelLayers.ZOMBIE_INNER_ARMOR, ModelLayers.ZOMBIE_OUTER_ARMOR);
    }

    public DemolitionZombieRenderer(EntityRendererProvider.Context pContext, ModelLayerLocation pZombieLayer, ModelLayerLocation pInnerArmor, ModelLayerLocation pOuterArmor) {
        super(pContext, new ZombieModel<>(pContext.bakeLayer(pZombieLayer)), new ZombieModel<>(pContext.bakeLayer(pInnerArmor)), new ZombieModel<>(pContext.bakeLayer(pOuterArmor)));
    }
}
