package net.petemc.undeadnights.client.render;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.zombie.ZombieModel;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class HordeZombieRenderer
        extends AbstractZombieRenderer<HordeZombieEntity, ZombieRenderState, ZombieModel<ZombieRenderState>> {

    public HordeZombieRenderer(EntityRendererProvider.Context ctx) {
        this(ctx, ModelLayers.ZOMBIE, ModelLayers.ZOMBIE_BABY, ModelLayers.ZOMBIE_ARMOR, ModelLayers.ZOMBIE_BABY_ARMOR);
    }

    public HordeZombieRenderer(EntityRendererProvider.Context ctx, ModelLayerLocation layer, ModelLayerLocation legsArmorLayer, ArmorModelSet<ModelLayerLocation> equipmentModelData, ArmorModelSet<ModelLayerLocation> equipmentModelData2) {
        super(ctx, new ZombieModel<>(ctx.bakeLayer(layer)), new ZombieModel<>(ctx.bakeLayer(legsArmorLayer)), ArmorModelSet.bake(equipmentModelData, ctx.getModelSet(), ZombieModel::new), ArmorModelSet.bake(equipmentModelData2, ctx.getModelSet(), ZombieModel::new));
    }

    @Override
    public @NotNull ZombieRenderState createRenderState() {
        return new ZombieRenderState();
    }
}
