package net.petemc.undeadnights.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ZombieBaseEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EquipmentModelData;
import net.minecraft.client.render.entity.model.ZombieEntityModel;
import net.minecraft.client.render.entity.state.ZombieEntityRenderState;
import net.minecraft.entity.EquipmentSlot;
import net.petemc.undeadnights.entity.HordeZombieEntity;

@Environment(EnvType.CLIENT)
public class HordeZombieRenderer
            extends ZombieBaseEntityRenderer<HordeZombieEntity, ZombieEntityRenderState, ZombieEntityModel<ZombieEntityRenderState>> {

    public HordeZombieRenderer(EntityRendererFactory.Context ctx) {
        this(ctx, EntityModelLayers.ZOMBIE, EntityModelLayers.ZOMBIE_BABY, EntityModelLayers.ZOMBIE_EQUIPMENT, EntityModelLayers.ZOMBIE_BABY_EQUIPMENT
            );
        }

    public HordeZombieRenderer(EntityRendererFactory.Context ctx, EntityModelLayer layer, EntityModelLayer legsArmorLayer, EquipmentModelData<EntityModelLayer> equipmentModelData, EquipmentModelData<EntityModelLayer> equipmentModelData2) {
        super(ctx, new ZombieEntityModel<>(ctx.getPart(layer)), new ZombieEntityModel<>(ctx.getPart(legsArmorLayer)), EquipmentModelData.mapToEntityModel(equipmentModelData, ctx.getEntityModels(), ZombieEntityModel::new), EquipmentModelData.mapToEntityModel(equipmentModelData2, ctx.getEntityModels(), ZombieEntityModel::new));
    }

    @Override
    public ZombieEntityRenderState createRenderState() {
        return new ZombieEntityRenderState();
    }
}
