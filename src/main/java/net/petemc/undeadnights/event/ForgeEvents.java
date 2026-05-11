package net.petemc.undeadnights.event;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Result;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.brewing.BrewingRecipeRegisterEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.ZombieEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.ConfigCommand;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.casts.BlockBreakingZombie;
import net.petemc.undeadnights.command.*;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.EliteZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import net.petemc.undeadnights.entity.ai.goal.BreakBlockGoal;
import net.petemc.undeadnights.item.ModItems;
import net.petemc.undeadnights.potion.ModPotions;
import net.petemc.undeadnights.util.ModTags;

import java.util.Comparator;
import java.util.List;

@Mod.EventBusSubscriber(modid = UndeadNights.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {
    @SubscribeEvent
    public static void denyReinforcementsForHordeZombies(ZombieEvent.SummonAidEvent event) {
        if ((event.getEntity() instanceof DemolitionZombieEntity) ||
                (event.getEntity() instanceof HordeZombieEntity) ||
                (event.getEntity() instanceof EliteZombieEntity)) {
            //if (MainConfig.getPrintDebugMessages()) {
            //    UndeadNights.LOGGER.info("Reinforcement denied for {}", event.getEntity().getName().getString());
            //}
            event.setResult(Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if(!event.getLevel().isClientSide()) {
            if (UndeadNights.serverState != null) {
                if (UndeadNights.serverState.spawnedHordeMobs.containsKey(event.getEntity().getUUID())) {
                    if (UndeadNights.serverState.hordeMobsToRemove.containsKey(event.getEntity().getUUID())) {
                        UndeadNights.serverState.hordeMobsToRemove.remove(event.getEntity().getUUID());
                        event.getEntity().remove(Entity.RemovalReason.DISCARDED);
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
                if (UndeadNights.serverState.spawnedHordeMobs.containsKey(event.getEntity().getUUID())) {
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
    public static void onPlayerTrySleep(PlayerSleepInBedEvent event) {
        if (UndeadNights.serverState.getHordeNight() && MainConfig.getHordeNightsDisableSleeping()
                && !event.getEntity().level().isClientSide() && !event.getEntity().isCreative()) {
            event.setResult(Player.BedSleepingProblem.NOT_SAFE);
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
    public static void onRegisterBrewingRecipes(BrewingRecipeRegisterEvent event) {
        PotionBrewing.Builder builder = event.getBuilder();
        builder.addMix(Potions.AWKWARD, ModItems.SLIMY_ROTTEN_FLESH.get(), ModPotions.LURE_HORDE_POTION.getHolder().get());
        builder.addMix(ModPotions.LURE_HORDE_POTION.getHolder().get(), ModItems.SLIMY_ROTTEN_FLESH.get(), ModPotions.STRONG_LURE_HORDE_POTION.getHolder().get());
    }

    @SubscribeEvent
    public static void onEntityTick(LivingEvent.LivingTickEvent event) {
        LevelAccessor world = event.getEntity().level();
        Entity entity = event.getEntity();
        if (entity == null) {
            return;
        }

        if (BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity.getType()).is(ModTags.EntityTypes.HORDE_MOBS)) {
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
                                BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entityTagCheck.getType()).is(ModTags.EntityTypes.HORDE_MOBS))
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
}
