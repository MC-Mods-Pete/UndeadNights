package net.petemc.undeadnights.event;

import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.EliteZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import net.petemc.undeadnights.entity.ModEntities;

@EventBusSubscriber(modid = UndeadNights.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEvents {
    @SubscribeEvent
    public static void spawnPlacementEvent(RegisterSpawnPlacementsEvent event) {
        event.register(ModEntities.HORDE_ZOMBIE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                HordeZombieEntity::checkHordeZombieSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);

        event.register(ModEntities.ELITE_ZOMBIE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                EliteZombieEntity::checkEliteZombieSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);

        event.register(ModEntities.DEMOLITION_ZOMBIE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                DemolitionZombieEntity::checkDemolitionZombieSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
    }
}