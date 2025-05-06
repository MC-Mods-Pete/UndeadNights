package net.petemc.undeadnights.world.spawner;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.registries.ForgeRegistries;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.command.SpawnHordeCommand;
import net.petemc.undeadnights.config.HordeConfig;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import net.petemc.undeadnights.entity.ModEntities;
import net.petemc.undeadnights.sound.UndeadNightsSounds;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class UndeadSpawner implements CustomSpawner {
    public static boolean invalidHordeMobEntry = false;
    public static int hordeToSpawn = 1;
    public static long prevNormalizedTimeOfDay = 0;

    private double x = 0;
    private double z = 0;
    private double d = 0;

    private boolean checkSpawnLocation(ServerLevel level, double x, double y, double z) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(x, y, z);

        BlockState blockState = level.getBlockState(mutable);
        Block block = blockState.getBlock();
        boolean doesNotblockMovement = block != Blocks.COBWEB && block != Blocks.BAMBOO_SAPLING;
        boolean notWater = true;
        if (!MainConfig.getHordeWavesCanSpawnInWater()) {
            notWater = !(blockState.getFluidState().is(FluidTags.WATER));
        }
        boolean notLeaves = true;
        if (!MainConfig.getHordeWavesCanSpawnOnTrees()) {
            notLeaves = !(blockState.getBlock() instanceof LeavesBlock);
        }
        return doesNotblockMovement && notLeaves && notWater;
    }

    private BlockPos getBlockPosWithDistance(BlockPos pos, Level level, int distanceMin, int distanceMax) {
        final RandomSource random = level.random;
        double _d = 0;
        double _x = 0;
        double _z = 0;
        _d = random.nextIntBetweenInclusive(distanceMin, distanceMax);
        _x = random.nextIntBetweenInclusive(0, (int) _d);
        if (_x == 0) {
            _z = _d;
        } else {
            _z = Math.sqrt((_d * _d) - (_x * _x));
            if (random.nextBoolean()) {
                _x = _x * -1;
            }
        }
        if (random.nextBoolean()) {
            _z = _z * -1;
        }

        return new BlockPos(pos.getX() + (int) _x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX() + (int) _x, pos.getZ() + (int) _z), pos.getZ() + (int) _z);
    }

    
    private void spawnHordeMob(ServerLevel level, RandomSource randomSource, BlockPos pos, Player player, HordeConfig.MobSpawnData mobSpawnData) {
        EntityType<?> mobType = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(mobSpawnData.mobId()));
        assert mobType != null;
        //Mob mob = (Mob) mobType.create(level);
        int deltaX = randomSource.nextInt(8);
        int deltaZ = randomSource.nextInt(8);
        //assert mob != null;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(pos.getX(), pos.getY() - 1, pos.getZ());
        BlockState blockState = level.getBlockState(mutable);
        int y = 0;
        if (blockState.getFluidState().is(FluidTags.WATER)) {
            y = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX() + deltaX, pos.getZ() + deltaZ);
        }
        if (MainConfig.getHordeWavesCanSpawnOnTrees()) {
            y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX() + deltaX, pos.getZ() + deltaZ);
        } else {
            y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX() + deltaX, pos.getZ() + deltaZ);
        }

        if (mobSpawnData.mobId().equals("minecraft:ghast")) {
            y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX() + deltaX, pos.getZ() + deltaZ) + 20;
        }

        CompoundTag nbtCompound = new CompoundTag();
        if (!Objects.equals(mobSpawnData.nbt(), "")) {
            try {
                nbtCompound = TagParser.parseTag(mobSpawnData.nbt());
            } catch (CommandSyntaxException e) {
                UndeadNights.LOGGER.error("Parsing NBT-tags for {} failed!", mobSpawnData.mobId());
            }
        }
        if (!invalidHordeMobEntry) {
            nbtCompound.putString("id", mobSpawnData.mobId());
        } else {
            nbtCompound.putString("id", "undeadnights:horde_zombie");
        }

        int finalY = y;
        Entity entity = EntityType.loadEntityRecursive(nbtCompound, level, EntitySpawnReason.COMMAND, entityx -> {
            entityx.moveTo(pos.getX() + deltaX, finalY, pos.getZ() + deltaZ, entityx.getYRot(), entityx.getXRot());
            return entityx;
        });

        if (entity instanceof DemolitionZombieEntity demolitionZombie) {
            String str = mobSpawnData.extra();
            String[] strA = str.split(":");
            if (strA[0].equals("tnt")) {
                try {
                    int numberTnt = Integer.parseInt(strA[1]);
                    if (numberTnt > 64) {
                        numberTnt = 64;
                    }
                    //DemolitionZombieEntity demolitionZombie = (DemolitionZombieEntity) mob;
                    demolitionZombie.setNumberTnt(numberTnt);
                } catch (Exception e) {
                    UndeadNights.LOGGER.warn("extraSpawnInfo for {} has non valid value, using default TNT stack size!", mobSpawnData.mobId());
                }
            } else {
                UndeadNights.LOGGER.warn("extraSpawnInfo for {} could be read, using default TNT stack size!", mobSpawnData.mobId());
            }
        }
        DifficultyInstance localDifficulty = level.getCurrentDifficultyAt(player.blockPosition());
        try {
            if (entity instanceof Mob mob) {
                mob.finalizeSpawn(level, localDifficulty, EntitySpawnReason.NATURAL, new Zombie.ZombieGroupData(false, false));
                mob.setTarget(player);

                if ((!mobSpawnData.mobId().equals("undeadnights:horde_zombie")) &&
                        (!mobSpawnData.mobId().equals("undeadnights:elite_zombie")) &&
                        (!mobSpawnData.mobId().equals("undeadnights:demolition_zombie"))) {
                    Objects.requireNonNull(mob.getAttribute(Attributes.FOLLOW_RANGE)).setBaseValue(128.0f);
                    mob.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(mob, Player.class, false, false));
                }
            }
            assert entity != null;
            UndeadNights.serverState.spawnedHordeMobs.add(entity.getUUID());
            level.tryAddFreshEntityWithPassengers(entity);
        } catch (Exception e) {
            invalidHordeMobEntry = true;
            UndeadNights.LOGGER.warn("Spawning entry {} from the config file failed! Spawning default horde zombie instead.", mobSpawnData.mobId());
            HordeZombieEntity hZombie = new HordeZombieEntity(ModEntities.HORDE_ZOMBIE.get(), level);
            hZombie.setPos(pos.getX() + deltaX, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX() + deltaX, pos.getZ() + deltaZ), pos.getZ() + deltaZ);
            if (MainConfig.getPersistentMobs()) {
                hZombie.setPersistenceRequired();
            }
            hZombie.finalizeSpawn(level, localDifficulty, EntitySpawnReason.NATURAL, new Zombie.ZombieGroupData(false, false));
            hZombie.setTarget(player);
            UndeadNights.serverState.spawnedHordeMobs.add(hZombie.getUUID());
            level.addFreshEntity(hZombie);
        }
    }

    public int spawnHorde(ServerLevel level, ServerPlayer player, RandomSource randomSource) {
        int randomValue = 0;
        BlockPos pos = player.blockPosition();
        boolean foundHordeSpawnLocation = false;
        int currentHordeCounter = UndeadNights.globalSpawnCounter;

        for (int i= 0; i < 20; i++){
            // for the given min/max distance, calculate the x and z coordinates deltas
            if (d == 0) {
                d = randomSource.nextIntBetweenInclusive(MainConfig.getDistanceMin(), MainConfig.getDistanceMax());
                x = randomSource.nextIntBetweenInclusive(0, (int) d);
                if (x == 0) {
                    z = d;
                } else {
                    z = Math.sqrt((d * d) - (x * x));
                    if (randomSource.nextBoolean()) {
                        x = x * -1;
                    }
                }
                if (randomSource.nextBoolean()) {
                    z = z * -1;
                }
            }

            pos = player.blockPosition().offset((int) x, 0, (int) z);
            pos = new BlockPos(pos.getX(), level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ()), pos.getZ());
            foundHordeSpawnLocation = checkSpawnLocation(level, pos.getX(), pos.getY() - 1, pos.getZ());
            if (!foundHordeSpawnLocation) {
                d = 0;
                x = 0;
                z = 0;
            } else {
                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("It took {} tries to find a valid Horde spawn location for player: {}", i + 1, player.getName().getString());
                }
                break;
            }
        }

        if (!foundHordeSpawnLocation) {
            UndeadNights.LOGGER.info("Could not find a valid Horde spawn location for player: {}", player.getName().getString());
            return -1;
        }

        if (MainConfig.getPrintDebugMessages()) {
            UndeadNights.LOGGER.info("Spawning Horde for player: {}", player.getName().getString());
        }

        boolean spawnCapReached = false;
        /*
         * Horde config variant 1
         */
        if (HordeConfig.getConfigVariant() == 1) {
            if (MainConfig.getPrintDebugMessages()) {
                UndeadNights.LOGGER.info("Horde config variant 1 detected.");
            }
            int waveMobCounter = 0;
            while (waveMobCounter < (HordeConfig.getMaxWaveSize())) {
                if (!HordeConfig.getHordeMobs().isEmpty()) {
                    for (var mobSpawnData : HordeConfig.getHordeMobs()) {
                        if (UndeadNights.globalSpawnCounter < MainConfig.getHordeMobsSpawnCap()) {
                            randomValue = randomSource.nextIntBetweenInclusive(1, 100);
                            spawnHordeMob(level, randomSource, pos, player, (randomValue > (100 - mobSpawnData.chance())) ? mobSpawnData : HordeConfig.getDefaultHordeMob());
                            waveMobCounter++;
                            if (waveMobCounter >= HordeConfig.getMaxWaveSize()) {
                                d = 0;
                                break;
                            }
                        } else {
                            // spawn cap reached, don't spawn anymore mobs in this wave
                            waveMobCounter = HordeConfig.getMaxWaveSize();
                            spawnCapReached = true;
                            d = 0;
                            break;
                        }
                    }
                } else {
                    spawnHordeMob(level, randomSource, pos, player, HordeConfig.getDefaultHordeMob());
                    waveMobCounter++;
                }
            }
        }

        /*
         * Horde config variant 2
         */
        if (HordeConfig.getConfigVariant() == 2) {
            if (MainConfig.getPrintDebugMessages()) {
                UndeadNights.LOGGER.info("Horde config variant 2 detected.");
            }
            List<HordeConfig.HordesData> hordes = HordeConfig.getHordes();
            int hordeIdToSpawn = hordeToSpawn - 1;
            if (hordeToSpawn == 0) {
                hordeIdToSpawn = randomSource.nextIntBetweenInclusive(0, hordes.size()-1);
            }
            for (var mobSpawnData : hordes.get(hordeIdToSpawn).hordeMobs()) {
                int mobCount = 0;
                if (mobSpawnData.countMin() >= mobSpawnData.countMax()) {
                    mobCount = mobSpawnData.countMin();
                } else {
                    mobCount = randomSource.nextInt(mobSpawnData.countMin(),mobSpawnData.countMax());
                    if (MainConfig.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("Spawning {} {}", mobCount, mobSpawnData.mobId());
                    }
                }
                for (int i = 0; i < mobCount; i++) {
                    boolean spawnMob = true;
                    if (mobSpawnData.chance() != 100) {
                        randomValue = randomSource.nextIntBetweenInclusive(1, 100);
                        if (MainConfig.getPrintDebugMessages()) {
                            UndeadNights.LOGGER.info("Chance value for horde config (variant 2) found, chance value: {}, randomValue: {}", mobSpawnData.chance(), randomValue);
                        }
                        if (!(randomValue > (100 - mobSpawnData.chance()))) {
                            spawnMob = false;
                        }
                    }
                    if (spawnMob) {
                        spawnHordeMob(level, randomSource, pos, player, mobSpawnData);
                    }
                    if (UndeadNights.globalSpawnCounter >= MainConfig.getHordeMobsSpawnCap()) {
                        spawnCapReached = true;
                        break;
                    }
                }
                if (UndeadNights.globalSpawnCounter >= MainConfig.getHordeMobsSpawnCap()) {
                    spawnCapReached = true;
                    break;
                }
            }
        }

        if (currentHordeCounter != UndeadNights.globalSpawnCounter) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), UndeadNightsSounds.HORDE_SCREAM.get(), SoundSource.HOSTILE, 4.0F, 1);
            player.sendSystemMessage(Component.translatable("message.undeadnights.horde_spawned").withStyle(ChatFormatting.RED));
            if (MainConfig.getPrintDebugMessages()) {
                UndeadNights.LOGGER.info("A Horde has spanned!");
            }
        }

        d = 0;
        if (spawnCapReached) {
            UndeadNights.LOGGER.info("Spawncap reached, {} Horde Zombies are already loaded into this world.", MainConfig.getHordeMobsSpawnCap());
            return -1;
        }
        return 0;
    }


    @Override
    public int tick(@NotNull ServerLevel level, boolean spawnMonsters, boolean spawnAnimals) {
        // check if Horde Nights and monster spawning is enabled
        if (!spawnMonsters || !MainConfig.getUndeadNightsEnabled()) {
            return 0;
        }

        // Are we in the Overworld?
        if (!(level.dimension() == Level.OVERWORLD)) {
            return 0;
        }

        // Check if the SaveState is already initialized
        if (UndeadNights.serverState == null) {
            return 0;
        }

        // calculate normalized time of day and set "Is It Night" flag
        long normalizedTimeOfDay = level.getDayTime() - ((level.getDayTime() / 24000L) * 24000);
        if (prevNormalizedTimeOfDay == normalizedTimeOfDay) {
            return 0;
        }
        boolean nightIsStarting = ((prevNormalizedTimeOfDay < 12000L) && (normalizedTimeOfDay >= 12000L));
        prevNormalizedTimeOfDay = normalizedTimeOfDay;

        boolean itIsNight = normalizedTimeOfDay >= 12000 && normalizedTimeOfDay < 22500;

        final RandomSource randomSource = level.random;
        int randomValue = 0;

        if (SpawnHordeCommand.spawnHorde) {
            SpawnHordeCommand.spawnHorde = false;
            if (SpawnHordeCommand.entities != null) {
                for (var player : SpawnHordeCommand.entities.stream().toList()) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        if (spawnHorde(level, serverPlayer, randomSource) == -1) {
                            break;
                        }
                    }
                }
                SpawnHordeCommand.entities = null;
            } else {
                for (ServerPlayer player : level.getPlayers(LivingEntity::isAlive)) {
                    if (spawnHorde(level, player, randomSource) == -1) {
                        break;
                    }
                }
            }
        }

        // is it night...?
        if (itIsNight) {
            // if it's already a horde night, check if we should respawn new waves
            if (UndeadNights.serverState.getRespawnZombies() && UndeadNights.serverState.getHordeNight() && MainConfig.getSpawnAdditionalWaves()) {
                if (UndeadNights.serverState.getTickCounter() > 0) {
                    UndeadNights.serverState.setTickCounter(UndeadNights.serverState.getTickCounter() - 1);
                    return 0;
                } else {
                    UndeadNights.serverState.setTickCounter(MainConfig.getCooldownBetweenWaves() * 20);
                }

                randomValue = randomSource.nextIntBetweenInclusive(1, 100);
                if (randomValue > (100 - MainConfig.getChanceForAdditionalWaves())) {
                    if (MainConfig.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("New Wave, randomValue was: {}", randomValue);
                    }
                    UndeadNights.serverState.setSpawnZombies(true);
                    UndeadNights.serverState.setRespawnZombies(false);
                } else {
                    if (MainConfig.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("RandomValue: {}", randomValue);
                    }
                    return 0;
                }
            }

            // if a new night just started count down the days
            if ((nightIsStarting) && (UndeadNights.serverState.getDaysCounter() >= 1)) {
                UndeadNights.serverState.setDaysCounter(UndeadNights.serverState.getDaysCounter() - 1);
                if ((UndeadNights.serverState.getDaysCounter() > 0) && (MainConfig.getSendHordeNightsCountdownMessage())) {
                    for (ServerPlayer player : level.getPlayers(LivingEntity::isAlive)) {
                        if (UndeadNights.serverState.getDaysCounter() > 1) {
                            player.sendSystemMessage(Component.translatable("message.undeadnights.nights_remaining", String.valueOf(UndeadNights.serverState.getDaysCounter())));
                        } else {
                            player.sendSystemMessage(Component.translatable("message.undeadnights.last_nights"));
                        }
                    }
                }
                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("Night is coming, NormalizedTimeOfDay: {}, TimeOfDay: {} DaysCounter: {}", normalizedTimeOfDay, level.getDayTime(), UndeadNights.serverState.getDaysCounter());
                }
            }

            // spawn stray zombies for non horde nights
            if (UndeadNights.serverState.getDaysCounter() > 0 && !UndeadNights.serverState.getHordeNight()) {
                if (MainConfig.getSpawnStrayHordeZombies() && (UndeadNights.globalSpawnCounter < MainConfig.getHordeMobsSpawnCap())) {
                    if (UndeadNights.serverState.getTickCounter() > 0) {
                        UndeadNights.serverState.setTickCounter(UndeadNights.serverState.getTickCounter() - 1);
                        return 0;
                    } else {
                        UndeadNights.serverState.setTickCounter(5 * 20);
                    }
                    if (!(randomSource.nextFloat() < 0.03F)) {
                        return 0;
                    }
                    for (ServerPlayer player : level.getPlayers(LivingEntity::isAlive)) {
                        BlockPos pos = getBlockPosWithDistance(player.blockPosition(), level, MainConfig.getDistanceMin(), MainConfig.getDistanceMax());
                        if (!checkSpawnLocation(level, pos.getX(), pos.getY() - 1, pos.getZ())) {
                            return 0;
                        } else {
                            spawnHordeMob(level, randomSource, pos, player, new HordeConfig.MobSpawnData("undeadnights:horde_zombie",100, 0, 0, "none", ""));
                            if (MainConfig.getPrintDebugMessages()) {
                                UndeadNights.LOGGER.info("A stray horde zombie spawned!");
                            }
                        }
                    }
                }
                return 0;
            }

            // this is the first tick of a new night
            if (nightIsStarting) {
                randomValue = randomSource.nextIntBetweenInclusive(1, 100);
                if (!(randomValue > (100 - MainConfig.getChanceForHordeNight()))) {
                    return 0;
                } else {
                    UndeadNights.serverState.setHordeNight(true);
                    UndeadNights.serverState.setSpawnZombies(true);
                    for (ServerPlayer player : level.getPlayers(LivingEntity::isAlive)) {
                        player.sendSystemMessage(Component.translatable("message.undeadnights.horde_night").withStyle(ChatFormatting.RED));
                    }
                    if (MainConfig.getPrintDebugMessages()) {
                        UndeadNights.LOGGER.info("The coming night is a Horde Night, HordeNight: {}", UndeadNights.serverState.getHordeNight());
                    }
                }
            }

            // spawn the waves
            if (UndeadNights.serverState.getSpawnZombies() && UndeadNights.serverState.getHordeNight() && normalizedTimeOfDay >= 12542) {
                for (ServerPlayer player : level.getPlayers(LivingEntity::isAlive)) {
                    if (spawnHorde(level, player, randomSource) == -1) {
                        break;
                    }
                }

                UndeadNights.serverState.setTickCounter(MainConfig.getCooldownBetweenWaves() * 20);
                UndeadNights.serverState.setSpawnZombies(false);
                UndeadNights.serverState.setRespawnZombies(true);
                UndeadNights.serverState.setDaysCounter(MainConfig.getDaysBetweenHordeNights());
                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("Spawned Waves for every player: DaysCounter: {} GlobalSpawnCounter: {} Spawn: {}, respawn: {}", UndeadNights.serverState.getDaysCounter(), UndeadNights.globalSpawnCounter, UndeadNights.serverState.getSpawnZombies(), UndeadNights.serverState.getRespawnZombies());
                }
                d = 0;
                x = 0;
                z = 0;
            }
        } else {
            if (UndeadNights.serverState.getHordeNight()) {
                for (ServerPlayer player : level.getPlayers(LivingEntity::isAlive)) {
                    player.sendSystemMessage(Component.translatable("message.undeadnights.horde_night_over"));
                }
                UndeadNights.serverState.setDaysCounter(MainConfig.getDaysBetweenHordeNights());
                if (MainConfig.getPrintDebugMessages()) {
                    UndeadNights.LOGGER.info("The Night of the Undead is over, TimeOfDay: {} DaysCounter: {} GlobalSpawnCounter: {}", level.getDayTime(), UndeadNights.serverState.getDaysCounter(), UndeadNights.globalSpawnCounter);
                }
            }
            UndeadNights.serverState.setHordeNight(false);
            UndeadNights.serverState.setSpawnZombies(true);
            UndeadNights.serverState.setRespawnZombies(false);
        }
        return 0;
    }
}
