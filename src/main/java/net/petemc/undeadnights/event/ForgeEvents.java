package net.petemc.undeadnights.event;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.ZombieEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.ConfigCommand;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.command.*;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.EliteZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import net.petemc.undeadnights.entity.ai.goal.BreakBlockGoal;
import net.petemc.undeadnights.util.ModTags;

import java.util.List;

@Mod.EventBusSubscriber(modid = UndeadNights.MOD_ID)
public class ForgeEvents {
    @SubscribeEvent
    public static void denyReinforcementsForHordeZombies(ZombieEvent.SummonAidEvent event) {
        if ((event.getEntity() instanceof DemolitionZombieEntity) ||
                (event.getEntity() instanceof HordeZombieEntity) ||
                (event.getEntity() instanceof EliteZombieEntity)) {
            //if (MainConfig.getPrintDebugMessages()) {
            //    UndeadNights.LOGGER.info("Reinforcement denied for {}", event.getEntity().getName().getString());
            //}
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if(!event.getLevel().isClientSide()) {
            if (UndeadNights.serverState != null) {
                if (UndeadNights.serverState.spawnedHordeMobs.contains(event.getEntity().getUUID())) {
                    if (UndeadNights.serverState.hordeMobsToRemove.contains(event.getEntity().getUUID())) {
                        UndeadNights.serverState.hordeMobsToRemove.remove(event.getEntity().getUUID());
                        event.getEntity().remove(Entity.RemovalReason.DISCARDED);
                        event.setCanceled(true);
                        if (MainConfig.getPrintDebugMessages()) {
                            UndeadNights.LOGGER.info("LOAD canceled, Entity marked for removal: {}", event.getEntity().getUUID());
                        }
                    } else {
                        UndeadNights.globalSpawnCounter++;
                        if (event.getEntity() instanceof Zombie zombie) {
                            if (!(zombie instanceof HordeZombieEntity) && !(zombie instanceof DemolitionZombieEntity) && !(zombie instanceof EliteZombieEntity)) {
                                if (MainConfig.getPrintDebugMessages()) {
                                    UndeadNights.LOGGER.info("Vanilla zombie detected, adding float and block breaking goals.");
                                }
                                zombie.goalSelector.addGoal(1, new FloatGoal(zombie));
                                zombie.goalSelector.addGoal(1, new BreakBlockGoal(zombie));
                            }
                            if (zombie instanceof EliteZombieEntity) {
                                UndeadNights.serverState.setFirstEliteZombieHasSpawned(true);
                            }
                            if (zombie instanceof DemolitionZombieEntity) {
                                UndeadNights.serverState.setFirstDemolitionZombieHasSpawned(true);
                            }
                        }
                    }
                    if (MainConfig.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("LOAD GlobalSpawnCount: : {} {} {}", UndeadNights.globalSpawnCounter, event.getEntity().getName().getString(), event.getEntity().getUUID());
                    }
                    //event.getEntity().kill();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveWorld(EntityLeaveLevelEvent event) {
        if(!event.getLevel().isClientSide()) {
            if (UndeadNights.serverState != null) {
                if (UndeadNights.serverState.spawnedHordeMobs.contains(event.getEntity().getUUID())) {
                    if (event.getEntity().getRemovalReason() != null) {
                        if ((event.getEntity().getRemovalReason() == Entity.RemovalReason.KILLED) || (event.getEntity().getRemovalReason() == Entity.RemovalReason.DISCARDED)) {
                            UndeadNights.globalSpawnCounter--;
                            UndeadNights.serverState.spawnedHordeMobs.remove(event.getEntity().getUUID());
                            if (MainConfig.getPrintDebugMessages()) {
                                if (event.getEntity().getRemovalReason() != null) {
                                    UndeadNights.LOGGER.info("UNLOAD GlobalSpawnCount: {} {} {}", UndeadNights.globalSpawnCounter, event.getEntity().getRemovalReason().name(), event.getEntity().getUUID());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        new SpawnHordeCommand(event.getDispatcher());
        new HordeMobsCommand(event.getDispatcher());
        new StatusCommand(event.getDispatcher());
        new SetDefaultHordeCommand(event.getDispatcher());
        new DifficultyLevelCommand(event.getDispatcher());

        ConfigCommand.register(event.getDispatcher());
    }


    @SubscribeEvent
    public static void onEntityTick(LivingEvent.LivingTickEvent event) {
        Entity entity = event.getEntity();

        final int HORDE_CLIMB_CHECK_INTERVAL = 2;

        if (entity == null) {
            return;
        }

        if (BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity.getType()).is(ModTags.EntityTypes.HORDE_MOBS)) {
            if ((entity.tickCount + entity.getId()) % HORDE_CLIMB_CHECK_INTERVAL != 0) {
                return;
            }

            final Vec3 entityPosition = entity.position();
            final AABB entitySearchArea = new AABB(entityPosition, entityPosition).inflate(0.45 / 2d);
            List<Entity> nearbyHordeMobs = entity.level().getEntitiesOfClass(Entity.class, entitySearchArea, entityTagCheck ->
                    entityTagCheck.getType().builtInRegistryHolder().is(ModTags.EntityTypes.HORDE_MOBS));

            for (Entity hordeMobIterator : nearbyHordeMobs) {
                if (hordeMobIterator == entity) {
                    continue;
                }
                if (entity.getX() != hordeMobIterator.getX()) {
                    double randomValue = Math.random();
                    if (randomValue > 0.16D) {
                        randomValue = 0.16D;
                    }
                    Vec3 entityVec3 = new Vec3(
                            (entity.getDeltaMovement().x() + (randomValue / 20.0D) * Mth.nextInt(RandomSource.create(), -1, 1)),
                            randomValue,
                            (entity.getDeltaMovement().z() + (randomValue / 20.0D) * Mth.nextInt(RandomSource.create(), -1, 1)));

                    entity.setDeltaMovement(entityVec3);
                    if (entity instanceof LivingEntity livingEntity) {
                        if (livingEntity.isBaby()) {
                            entity.setDeltaMovement(entityVec3.add(0, randomValue, 0));
                        }
                        if (!livingEntity.hasEffect(MobEffects.SLOW_FALLING)) {
                            livingEntity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 5, 0, false, false));
                        }
                    }
                    entity.fallDistance = 0;
                    break;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (!event.level.isClientSide() && event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel serverLevel) {
            boolean spawnMonsters = serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
            UndeadNights.hordeSpawner.tick(serverLevel, spawnMonsters);
        }
    }
}
