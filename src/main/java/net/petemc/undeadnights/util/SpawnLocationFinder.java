package net.petemc.undeadnights.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
     * Tries to find a cave spawn block position approximately {@code distance} blocks away
     * from the player, using Minecraft's vanilla WalkNodeEvaluator / PathFinder to confirm
     * that a zombie can actually walk from the candidate to the player.
     *
     * <p>Design goals:
     * <ul>
     *   <li><b>Speed</b>: at most 80 XZ candidates are tested; each XZ column produces at most
     *       one pathfinding call (all Y-values are evaluated geometrically first, best Y wins).
     *       A 400 ms hard budget guarantees the method returns quickly even if many columns fail.</li>
     *   <li><b>Reliability</b>: vanilla Minecraft pathfinding (WalkNodeEvaluator + PathFinder)
     *       is used for the final connectivity check so the result is consistent with actual
     *       mob navigation.</li>
     *   <li><b>Safety</b>: after a valid position is found, the player's current location is
     *       re-checked. If the player has walked to within half the requested distance of the
     *       candidate during the search, the candidate is discarded and the search continues –
     *       preventing mobs from spawning on top of a fast-moving player.</li>
     * </ul>
     *
     * <p>The probe entity is <em>never</em> added to the world. {@link net.minecraft.world.level.pathfinder.PathFinder}
     * only needs the entity for its bounding-box dimensions and current position,
     * neither of which requires world registration.
     *
     * <h4>Two-phase acceptance</h4>
     * <ol>
     *   <li><b>Phase 1</b> – prefer positions with a <em>complete</em> path
     *       ({@code path.canReach()}): the zombie is guaranteed to be able to
     *       walk to the player.</li>
     *   <li><b>Phase 2 fallback</b> – if no complete path was found within the
     *       time budget, return the best partial-path candidate (path made the
     *       most progress toward the player).  This handles deep cave systems
     *       where the A* node budget may be exhausted before the full route is
     *       traced, even though the zombie would still navigate successfully in
     *       practice.</li>
     * </ol>
     */
    public static BlockPos findEndPositionUsingMinecraftPathfinding(Level level, ServerPlayer player, int distance, boolean allowEndInWater) {
        if (level == null || player == null || distance <= 0) return null;
        final boolean debug = MainConfig.getPrintDebugMessages();
        final ThreadLocalRandom rnd = ThreadLocalRandom.current();

        // Reduced attempt count: every XZ candidate is on the right ring (no wasted samples).
        final int maxAttempts = 80;
        // Broader Y window – handles deep cave drops / tall rooms.
        final int maxYDelta = 12;
        // Hard time budget: prevents multi-second stalls on the server thread.
        // 400 ms is generous – typical successful searches finish in < 50 ms.
        final long deadlineMs = System.currentTimeMillis() + 400L;

        BlockPos start = player.blockPosition();

        // Create the probe entity for navigation – NOT added to the world.
        // PathNavigation / WalkNodeEvaluator only need the entity's bounding box and
        // current position, which we set manually before each pathfinding call.
        HordeZombieEntity probe = new HordeZombieEntity(ModEntities.HORDE_ZOMBIE.get(), level);
        probe.setOnGround(true);
        // Increase the follow-range attribute to 256 blocks so the pathfinder is never
        // limited by tracking distance (the node budget is the real constraint).
        var followRange = probe.getAttribute(Attributes.FOLLOW_RANGE);
        if (followRange != null) followRange.setBaseValue(256.0);

        // Best partial-path fallback: track the candidate whose path got closest
        // to the player in case no complete path is found within the time budget.
        BlockPos bestPartialCandidate = null;
        float bestPartialDistSq = Float.MAX_VALUE;

        for (int i = 0; i < maxAttempts; i++) {
            if (System.currentTimeMillis() >= deadlineMs) {
                if (debug) UndeadNights.LOGGER.info("findEndUsingMinecraftPF: 400 ms budget exhausted after {} attempts", i);
                break;
            }

            // -----------------------------------------------------------------
            // Circular ring sampling: every candidate sits exactly ~distance
            // blocks from the player. Unlike Chebyshev-biased random sampling,
            // no attempt is wasted on positions that are trivially off-distance.
            // -----------------------------------------------------------------
            double angle = rnd.nextDouble() * 2.0 * Math.PI;
            int jitter = rnd.nextInt(-3, 4);
            int r = Math.max(1, distance + jitter);
            int dx = (int) Math.round(Math.cos(angle) * r);
            int dz = (int) Math.round(Math.sin(angle) * r);
            int cx = start.getX() + dx;
            int cz = start.getZ() + dz;

            // Geometric pre-filter: find the best walkable Y in this column.
            // Only ONE pathfinding call is made per XZ column (not one per Y-value).
            BlockPos candidate = findBestCaveY(level, cx, start.getY(), cz, maxYDelta, allowEndInWater);
            if (candidate == null) continue;

            try {
                // Position the probe at the candidate and run vanilla pathfinding.
                // The node multiplier is raised to 8× vanilla so winding cave routes
                // (which can be 3–5× the straight-line distance) are fully explored.
                probe.setPos(candidate.getX() + 0.5, candidate.getY(), candidate.getZ() + 0.5);
                GroundPathNavigationLegacy nav = new GroundPathNavigationLegacy(probe, level);
                Path path = nav.createPathLegacy(player, 0);

                if (path == null) {
                    // Null means the start node couldn't be evaluated – skip silently.
                    continue;
                }

                // -----------------------------------------------------------------
                // Phase 1: full path – the zombie is guaranteed to reach the player.
                // canReach() == true means the A* search completed successfully.
                // -----------------------------------------------------------------
                if (path.canReach()) {
                    double minSafeDistSq = (distance * 0.5) * (distance * 0.5);
                    if (player.blockPosition().distSqr(candidate) < minSafeDistSq) {
                        if (debug) UndeadNights.LOGGER.info(
                                "findEndUsingMinecraftPF: candidate {} discarded – player moved too close (attempt {})", candidate, i);
                        continue;
                    }
                    if (debug) UndeadNights.LOGGER.info(
                            "findEndUsingMinecraftPF: candidate {} accepted (complete path, attempt {})", candidate, i);
                    return candidate;
                }

                // -----------------------------------------------------------------
                // Phase 2 bookkeeping: partial path – the pathfinder ran out of
                // nodes but made some progress. getDistToTarget() is the remaining
                // distance from the path end to the player; smaller = more progress.
                // -----------------------------------------------------------------
                float distLeft = path.getDistToTarget();
                if (distLeft < bestPartialDistSq) {
                    bestPartialDistSq = distLeft;
                    bestPartialCandidate = candidate;
                }

                if (debug) UndeadNights.LOGGER.info(
                        "findEndUsingMinecraftPF: candidate {} partial path, distLeft={} (attempt {})", candidate, distLeft, i);
            } catch (Throwable t) {
                UndeadNights.LOGGER.warn("findEndUsingMinecraftPF: navigation threw for candidate {}: {}", candidate, t.toString());
            }
        }

        // -----------------------------------------------------------------
        // Phase 2 fallback: no complete path found – use best partial candidate.
        // This keeps cave spawns alive in very complex underground systems where
        // the A* budget is always exceeded, while still preferring positions that
        // progressed the most toward the player.
        // -----------------------------------------------------------------
        if (bestPartialCandidate != null) {
            double minSafeDistSq = (distance * 0.5) * (distance * 0.5);
            if (player.blockPosition().distSqr(bestPartialCandidate) >= minSafeDistSq) {
                if (debug) UndeadNights.LOGGER.info(
                        "findEndUsingMinecraftPF: using best partial-path fallback candidate {} (distance={})", bestPartialCandidate, distance);
                return bestPartialCandidate;
            }
        }

        if (debug) UndeadNights.LOGGER.info("findEndUsingMinecraftPF: no candidate found (distance={})", distance);
        return null;
    }

    /**
     * Searches vertically around {@code baseY} at column ({@code cx}, {@code cz}) for the
     * first block position that passes all geometric spawn-validity checks:
     * solid floor below, enough headroom above, no lava, optionally no water, and (if
     * configured) dark enough for monster spawns.
     *
     * <p>Scans outward from {@code baseY} so the elevation closest to the player is
     * preferred, which keeps mobs on the same cave level as the player.
     *
     * @return the first valid {@link BlockPos}, or {@code null} if none found within
     *         ±{@code deltaY} of {@code baseY}.
     */
    // package-private so CaveSpawnSearchTask can reuse it without duplication
    static BlockPos findBestCaveY(Level level, int cx, int baseY, int cz, int deltaY, boolean allowEndInWater) {
        for (int delta = 0; delta <= deltaY; delta++) {
            // delta == 0 → check baseY once; delta > 0 → check above then below
            int[] offsets = (delta == 0) ? new int[]{0} : new int[]{delta, -delta};
            for (int offset : offsets) {
                int cy = baseY + offset;
                if (cy < level.getMinBuildHeight() || cy >= level.getMaxBuildHeight()) continue;

                BlockPos cand = new BlockPos(cx, cy, cz);
                BlockState feet = level.getBlockState(cand);
                if (feet.is(Blocks.LAVA) || feet.getFluidState().is(FluidTags.LAVA)) continue;
                if (!allowEndInWater && (feet.is(Blocks.WATER) || feet.getFluidState().is(FluidTags.WATER))) continue;
                if (MainConfig.getBlockLightLevelsInfluenceMonsterSpawns()
                        && !isDarkEnoughToSpawn((ServerLevelAccessor) level, cand)) continue;
                if (!hasSolidBlockBelow(level, cand)) continue;
                // Zombie bounding box: width 0.6 (half = 0.3), height 1.8
                AABB box = new AABB(cx + 0.2, cy + 0.001, cz + 0.2,
                                    cx + 0.8, cy + 1.799,             cz + 0.8);
                if (!isAABBFreeForSpawn(level, box)) continue;
                return cand;
            }
        }
        return null;
    }


    // check if there is a solid block below the given position that can support a spawn
    private static boolean hasSolidBlockBelow(Level level, BlockPos center) {
        BlockPos below = center.below();
        if (below.getY() < level.getMinBuildHeight()) return false;
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
        final int minY = level.getMinBuildHeight();
        final int maxY = level.getMaxBuildHeight() - 1;

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
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight();
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
