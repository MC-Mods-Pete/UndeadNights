package net.petemc.undeadnights.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import net.petemc.undeadnights.entity.ModEntities;

import java.util.concurrent.ThreadLocalRandom;

public class SpawnLocationFinder {
    // check if the block light level at the given position is dark enough for monster spawns
    public static boolean isDarkEnoughToSpawn(ServerLevelAccessor level, BlockPos pos) {
        return level.getBrightness(LightLayer.BLOCK, pos) <= MainConfig.getMaxBlockLightLevelForMonsterSpawns();
    }

    // check if the given location is suitable for spawning horde mobs
    public static boolean checkSpawnLocation(ServerLevel level, double x, double y, double z) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(x, y, z);

        BlockState blockState = level.getBlockState(mutable);
        Block block = blockState.getBlock();
        boolean doesNotBlockMovement = block != Blocks.COBWEB && block != Blocks.BAMBOO_SAPLING;
        boolean notWater = true;
        boolean darkEnough = true;
        if (!MainConfig.getHordeWavesCanSpawnInWater()) {
            notWater = !(blockState.getFluidState().is(FluidTags.WATER));
        }
        boolean notLeaves = true;
        if (!MainConfig.getHordeWavesCanSpawnOnTrees()) {
            notLeaves = !(blockState.getBlock() instanceof LeavesBlock);
        }
        if (MainConfig.getBlockLightLevelsInfluenceMonsterSpawns()) {
            darkEnough = isDarkEnoughToSpawn(level, mutable);
        }

        return doesNotBlockMovement && notLeaves && notWater && darkEnough;
    }

    public static BlockPos getBlockPosWithDistance(BlockPos pos, Level level, int distanceMin, int distanceMax) {
        final RandomExtention random = new RandomExtention();
        double _d;
        double _x;
        double _z;
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

    public static BlockPos findNearbySurfaceSpawnPosition(ServerLevel level, BlockPos pos, RandomExtention randomSource, boolean playerInCave) {
        int deltaX = randomSource.nextInt(5);
        int deltaZ = randomSource.nextInt(5);
        if (!randomSource.nextBoolean()) {
            deltaX = deltaX * -1;
        }
        if (!randomSource.nextBoolean()) {
            deltaZ = deltaZ * -1;
        }
        int y;

        if (MainConfig.getHordeWavesCanSpawnOnTrees()) {
            y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX() + deltaX, pos.getZ() + deltaZ);
        } else {
            y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX() + deltaX, pos.getZ() + deltaZ);
        }

        return new BlockPos(pos.getX() + deltaX, y, pos.getZ() + deltaZ);
    }

    /**
     * Try to find an end blockpos at approximately 'distance' steps away from start using
     * Minecraft's actual mob pathfinding (PathNavigation) for confirmation.
     * This function creates a temporary HordeZombieEntity (not added to the world) and
     * asks its navigation to compute a path to sampled candidate positions. Returns the
     * first candidate the navigation can path to, or null if none found.
     */
    public static BlockPos findEndPositionUsingMinecraftPathfinding(Level level, ServerPlayer player, int distance, boolean allowEndInWater) {
        if (level == null || player == null || distance <= 0) return null;
        final boolean debug = MainConfig.getPrintDebugMessages();
        final ThreadLocalRandom rnd = ThreadLocalRandom.current();
        final int attempts = 600; // sampling attempts
        final int maxYDelta = 8; // how much to search up/down for standable Y

        BlockPos start = player.blockPosition();

        // quick start validation
        if (!isAABBFree(level, new AABB(start.getX() + 0.5 - 0.3, start.getY(), start.getZ() + 0.5 - 0.3, start.getX() + 0.5 + 0.3, start.getY() + 1.8, start.getZ() + 0.5 + 0.3))) {
            if (debug) UndeadNights.LOGGER.info("findEndUsingMinecraftPF: start pos AABB not free: {}", start);
            return null;
        }

        // create a temporary mob used for pathfinding computations (do not add to world)
        HordeZombieEntity probe = new HordeZombieEntity(ModEntities.HORDE_ZOMBIE, level);
        probe.finalizeSpawn((ServerLevelAccessor) level, ((ServerLevelAccessor) level).getCurrentDifficultyAt(start), EntitySpawnReason.MOB_SUMMONED, null);
        probe.setTarget(player);
        level.addFreshEntity(probe);

        for (int i = 0; i < attempts; i++) {
            // sample a candidate at approximate chebyshev distance
            int dx = rnd.nextInt(-distance - 3, distance + 4);
            int dz = rnd.nextInt(-distance - 3, distance + 4);
            int cheb = Math.max(Math.abs(dx), Math.abs(dz));
            // bias towards values near the requested distance
            if (Math.abs(cheb - distance) > 4) continue;

            int cx = start.getX() + dx;
            int cz = start.getZ() + dz;

            // scan vertically around start Y to find a standable block
            int baseY = start.getY();
            for (int dy = -maxYDelta; dy <= maxYDelta; dy++) {
                int cy = baseY + dy;
                BlockPos cand = new BlockPos(cx, cy, cz);
                BlockState feet = level.getBlockState(cand);
                if (feet.is(Blocks.LAVA) || feet.getFluidState().is(FluidTags.LAVA)) continue;
                if (!allowEndInWater && (feet.is(Blocks.WATER) || feet.getFluidState().is(FluidTags.WATER))) continue;
                if (MainConfig.getBlockLightLevelsInfluenceMonsterSpawns() && !SpawnLocationFinder.isDarkEnoughToSpawn((ServerLevelAccessor) level, cand)) continue;
                if (!hasSolidBlockBelow(level, cand)) continue;
                if (!isAABBFreeForSpawn(level, new AABB(cand.getX() + 0.5 - 0.3, cand.getY() + 0.001, cand.getZ() + 0.5 - 0.3, cand.getX() + 0.5 + 0.3, cand.getY() + 1.8 - 0.001, cand.getZ() + 0.5 + 0.3))) continue;

                try {
                    // set up probe for pathfinding
                    probe.setOnGround(true);
                    // place probe at candidate center before asking it to path to the player
                    probe.setPos(cand.getX() + 0.5, cand.getY(), cand.getZ() + 0.5);
                    // get probe navigation
                    //GroundPathNavigationExtended nav = (GroundPathNavigationExtended) probe.getNavigation();
                    GroundPathNavigationLegacy nav = new GroundPathNavigationLegacy(probe, level);
                    // createPath may return null or an empty path if unreachable
                    Path path = nav.createPathLegacy(player, 0);

                    if (path != null) {
                        if (debug) UndeadNights.LOGGER.info("findEndUsingMinecraftPF: candidate {} accepted by vanilla navigation (attempt {})", cand, i);
                        probe.remove(Entity.RemovalReason.DISCARDED);
                        return cand;
                    } else {
                        if (debug) UndeadNights.LOGGER.info("findEndUsingMinecraftPF: candidate {} rejected by vanilla navigation (null/empty)", cand);
                    }
                } catch (Throwable t) {
                    UndeadNights.LOGGER.warn("findEndUsingMinecraftPF: navigation threw for candidate {}: {}", cand, t.toString());
                }
            }
        }

        probe.remove(Entity.RemovalReason.DISCARDED);
        if (debug) UndeadNights.LOGGER.info("findEndUsingMinecraftPF: no candidate found (distance={})", distance);
        return null;
    }

    // check if the given AABB is free of blocking collision shapes
    private static boolean isAABBFree(Level level, AABB box) {
        int minX = (int) Math.floor(box.minX);
        int minY = (int) Math.floor(box.minY);
        int minZ = (int) Math.floor(box.minZ);
        int maxX = (int) Math.floor(box.maxX);
        int maxY = (int) Math.floor(box.maxY);
        int maxZ = (int) Math.floor(box.maxZ);

        for (int bx = minX; bx <= maxX; bx++) {
            for (int by = minY; by <= maxY; by++) {
                for (int bz = minZ; bz <= maxZ; bz++) {
                    BlockPos bpos = new BlockPos(bx, by, bz);
                    BlockState state = level.getBlockState(bpos);
                    VoxelShape shape = state.getCollisionShape(level, bpos);
                    if (!shape.isEmpty()) {
                        // Treat fences, fence gates and doors as non-blocking for pathfinding purposes
                        if (state.getBlock() instanceof FenceBlock || state.getBlock() instanceof FenceGateBlock || state.getBlock() instanceof DoorBlock) {
                            continue;
                        }
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // check if there is a solid block below the given position that can support a spawn
    private static boolean hasSolidBlockBelow(Level level, BlockPos center) {
        BlockPos below = center.below();
        if (below.getY() < level.getMinY()) return false;
        BlockState belowState = level.getBlockState(below);
        // treat slabs and stairs explicitly as supporting blocks, and also any block with a collision shape
        boolean hasCollision = !belowState.getCollisionShape(level, below).isEmpty();
        boolean isSlab = belowState.is(BlockTags.SLABS);
        boolean isStair = belowState.is(BlockTags.STAIRS);
        boolean isFence = belowState.getBlock() instanceof FenceBlock;
        boolean isFenceGate = belowState.getBlock() instanceof FenceGateBlock;
        boolean isDoor = belowState.getBlock() instanceof DoorBlock;
        return hasCollision || isSlab || isStair || isFence || isFenceGate || isDoor;
    }

    // check if the given AABB is free of blocking collision shapes for spawning
    private static boolean isAABBFreeForSpawn(Level level, AABB box) {
        int minX = (int) Math.floor(box.minX);
        int minY = (int) Math.floor(box.minY);
        int minZ = (int) Math.floor(box.minZ);
        int maxX = (int) Math.floor(box.maxX);
        int maxY = (int) Math.floor(box.maxY);
        int maxZ = (int) Math.floor(box.maxZ);

        for (int bx = minX; bx <= maxX; bx++) {
            for (int by = minY; by <= maxY; by++) {
                for (int bz = minZ; bz <= maxZ; bz++) {
                    BlockPos bpos = new BlockPos(bx, by, bz);
                    BlockState state = level.getBlockState(bpos);
                    VoxelShape shape = state.getCollisionShape(level, bpos);
                    if (!shape.isEmpty()) return false;
                }
            }
        }
        return true;
    }

    /**
     * Find a spawnable block position within X/Z radius and Y +/- deltaY around center.
     * This function intentionally does NOT use heightmaps; it searches directly and
     * returns the first valid position found (randomized). Lava is never allowed.
     */
    public static BlockPos findSpawnablePosition(Level level, BlockPos center, int radius, int deltaY) {
        if (level == null || center == null) return null;
        final int minY = level.getMinY();
        final int maxY = level.getMaxY() - 1;

        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        final float mobWidth = 0.6f;
        final float mobHeight = 1.8f;
        final boolean allowWater = MainConfig.getHordeWavesCanSpawnInWater();

        // randomized attempts first to avoid deterministic results
        final int attempts = 200;
        for (int i = 0; i < attempts; i++) {
            int dx = rnd.nextInt(-radius, radius + 1);
            int dz = rnd.nextInt(-radius, radius + 1);
            int dy = deltaY > 0 ? rnd.nextInt(-deltaY, deltaY + 1) : 0;

            int x = center.getX() + dx;
            int z = center.getZ() + dz;
            int y = center.getY() + dy;
            if (y < minY || y > maxY) continue;

            BlockPos cand = new BlockPos(x, y, z);
            BlockState feet = level.getBlockState(cand);
            if (feet.is(Blocks.LAVA) || feet.getFluidState().is(FluidTags.LAVA)) continue;
            if (isValidSpawnPos(level, cand, mobWidth, mobHeight, allowWater)) return cand;
        }

        // fallback: deterministic spiral scan (X/Z) with Y window if randomized attempts fail
        for (int r = 0; r <= radius; r++) {
            for (int dx = -r; dx <= r; dx++) {
                int[] zs = (r == 0) ? new int[]{0} : new int[]{-r, r};
                for (int zOff : zs) {
                    int x = center.getX() + dx;
                    int z = center.getZ() + zOff;
                    for (int dy = -deltaY; dy <= deltaY; dy++) {
                        int y = center.getY() + dy;
                        if (y < minY || y > maxY) continue;
                        BlockPos cand = new BlockPos(x, y, z);
                        BlockState feet = level.getBlockState(cand);
                        if (feet.is(Blocks.LAVA) || feet.getFluidState().is(FluidTags.LAVA)) continue;
                        if (isValidSpawnPos(level, cand, mobWidth, mobHeight, allowWater)) return cand;
                    }
                }
            }
        }

        return null;
    }

    private static boolean isValidSpawnPos(Level level, BlockPos pos, float mobWidth, float mobHeight, boolean allowWater) {
        if (level == null || pos == null) return false;
        int minY = level.getMinY();
        int maxY = level.getMaxY();
        if (pos.getY() < minY || pos.getY() >= maxY) return false;

        BlockState feetState = level.getBlockState(pos);
        boolean feetIsWater = feetState.is(Blocks.WATER) || feetState.getFluidState().is(FluidTags.WATER);
        boolean feetIsLava = feetState.is(Blocks.LAVA) || feetState.getFluidState().is(FluidTags.LAVA);
        if (feetIsLava) return false;
        if (feetIsWater && !allowWater) return false;

        VoxelShape feetShape = feetState.getCollisionShape(level, pos);
        if (!feetShape.isEmpty() && !feetIsWater) return false;

        double cx = pos.getX() + 0.5;
        double cz = pos.getZ() + 0.5;
        double bottomY = pos.getY();
        double topY = bottomY + mobHeight;
        double halfWidth = mobWidth / 2.0;

        final double eps = 1e-3;
        AABB box = new AABB(cx - halfWidth + eps, bottomY + eps, cz - halfWidth + eps, cx + halfWidth - eps, topY - eps, cz + halfWidth - eps);

        if (!isAABBFreeForSpawn(level, box)) return false;

        if (!allowWater) {
            int bottomBlock = (int) Math.floor(box.minY);
            int topBlock = (int) Math.floor(box.maxY);
            for (int by = bottomBlock; by <= topBlock; by++) {
                BlockState s = level.getBlockState(new BlockPos(pos.getX(), by, pos.getZ()));
                if (s.is(Blocks.WATER) || s.getFluidState().is(FluidTags.WATER)) return false;
            }
        }

        return hasSolidBlockBelow(level, pos);
    }
}
