package net.petemc.undeadnights.client.render;

import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.petemc.undeadnights.entity.EliteZombieEntity;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class EliteZombieRenderer
        extends AbstractZombieRenderer<EliteZombieEntity, ZombieRenderState, ZombieModel<ZombieRenderState>> {

    public EliteZombieRenderer(EntityRendererProvider.Context context) {
        this(
                context,
                ModelLayers.ZOMBIE,
                ModelLayers.ZOMBIE_BABY,
                ModelLayers.ZOMBIE_INNER_ARMOR,
                ModelLayers.ZOMBIE_OUTER_ARMOR,
                ModelLayers.ZOMBIE_BABY_INNER_ARMOR,
                ModelLayers.ZOMBIE_BABY_OUTER_ARMOR
        );
    }

    public EliteZombieRenderer(EntityRendererProvider.Context context, ModelLayerLocation adultModel, ModelLayerLocation babyModel, ModelLayerLocation innerModel, ModelLayerLocation outerModel, ModelLayerLocation innerModelBaby, ModelLayerLocation outerModelBaby) {
        super(
                context,
                new ZombieModel<>(context.bakeLayer(adultModel)),
                new ZombieModel<>(context.bakeLayer(babyModel)),
                new ZombieModel<>(context.bakeLayer(innerModel)),
                new ZombieModel<>(context.bakeLayer(outerModel)),
                new ZombieModel<>(context.bakeLayer(innerModelBaby)),
                new ZombieModel<>(context.bakeLayer(outerModelBaby))
        );
    }

    @Override
    public @NotNull ZombieRenderState createRenderState() {
        return new ZombieRenderState();
    }
}
