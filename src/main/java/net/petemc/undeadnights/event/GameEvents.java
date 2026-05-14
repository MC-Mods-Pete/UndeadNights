package net.petemc.undeadnights.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.server.command.ConfigCommand;
import net.petemc.undeadnights.casts.BlockBreakingZombie;
import net.petemc.undeadnights.command.*;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.EliteZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import net.petemc.undeadnights.entity.ai.goal.BreakBlockGoal;
import net.petemc.undeadnights.item.ModItems;
import net.petemc.undeadnights.potion.ModPotions;
import net.petemc.undeadnights.util.ModTags;

import java.util.Comparator;
import java.util.List;

@EventBusSubscriber(modid = UndeadNights.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class GameEvents {
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
        if (!event.getLevel().isClientSide()) {
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
    public static void onPlayerTrySleep(CanPlayerSleepEvent event) {
        if (UndeadNights.serverState.getHordeNight() && MainConfig.getHordeNightsDisableSleeping()) {
            event.setProblem(Player.BedSleepingProblem.NOT_SAFE);
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
        public static void onRegisterBrewingRecipes(RegisterBrewingRecipesEvent event) {
            PotionBrewing.Builder builder = event.getBuilder();
            builder.addMix(Potions.AWKWARD, ModItems.SLIMY_ROTTEN_FLESH.get(), ModPotions.LURE_HORDE_POTION);
            builder.addMix(ModPotions.LURE_HORDE_POTION, ModItems.SLIMY_ROTTEN_FLESH.get(), ModPotions.STRONG_LURE_HORDE_POTION);
        }


    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        LevelAccessor world = event.getEntity().level();
        Entity entity = event.getEntity();

        if (entity.getType().is(ModTags.EntityTypes.HORDE_MOBS)) {
            if (entity instanceof HordeZombieEntity hordeZombie) {
                if (hordeZombie.isBreakingBlock()) {
                    return;
                }
            }
            if (entity instanceof EliteZombieEntity eliteZombieEntity) {
                if (eliteZombieEntity.isBreakingBlock()) {
                    return;
                }
            }

            if (entity instanceof BlockBreakingZombie blockBreakingZombie) {
                if (blockBreakingZombie.isBreakingBlock()) {
                    return;
                }
            }

            if (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().isHordeMobsCanClimbEachOther()) {
                final Vec3 entityPosition = entity.position();
                final AABB entitySearchArea = new AABB(entityPosition, entityPosition).inflate(0.45 / 2d);
                List<Entity> sortedEntityList = world.getEntitiesOfClass(Entity.class, entitySearchArea , entityTagCheck ->
                                entityTagCheck.getType().is(ModTags.EntityTypes.HORDE_MOBS))
                        .stream().sorted(Comparator.comparingDouble(entityDistSort -> entityDistSort.distanceToSqr(entityPosition))).toList();

                for (Entity hordeMobIterator : sortedEntityList) {
                    if (!(entity.getX() == hordeMobIterator.getX())) {
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
                            livingEntity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 5, 0, false, false));
                        }
                        entity.fallDistance = 0;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!event.getLevel().isClientSide() && event.getLevel() instanceof ServerLevel serverLevel) {
            boolean spawnMonsters = serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
            UndeadNights.hordeSpawner.tick(serverLevel, spawnMonsters, true);
        }
    }
}
