package net.petemc.undeadnights.event;

import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.EliteZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import net.petemc.undeadnights.entity.ModEntities;

@Mod.EventBusSubscriber(modid = UndeadNights.MOD_ID)
public class ModEvents {
    @SubscribeEvent
    public static void spawnPlacementEvent(SpawnPlacementRegisterEvent event) {
        event.register(ModEntities.HORDE_ZOMBIE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                HordeZombieEntity::checkHordeZombieSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);

        event.register(ModEntities.ELITE_ZOMBIE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                EliteZombieEntity::checkEliteZombieSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);

        event.register(ModEntities.DEMOLITION_ZOMBIE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                DemolitionZombieEntity::checkDemolitionZombieSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
    }
}
