package net.petemc.undeadnights.util;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.petemc.undeadnights.config.MainConfig;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import net.petemc.undeadnights.entity.ModEntities;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class Helpers {
    public static boolean caveCheckStageOne(Level level, BlockPos pos) {
        int layersAbove = 0;
        int x = pos.getX();
        int z = pos.getZ();
        int y;

        //UndeadNights.LOGGER.info("--------------> Player position {} {} {}", x, pos.getY(), z);

        y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        //UndeadNights.LOGGER.info("----------------------------------------> Player is on surface (MOTION_BLOCKING_NO_LEAVES) {} {} {}", y, pos.getY(), level.getBlockState(pos));
        if (y == pos.getY()) {
            return false; // on surface
        }

        y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
        //UndeadNights.LOGGER.info("----------------------------------------> Player is on surface (MOTION_BLOCKING) {} {} {}", y, pos.getY(), level.getBlockState(pos));
        if (y == pos.getY()) {
            return false; // on surface
        }

        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos(x, pos.getY() + 1, z);
        while (checkPos.getY() < y) { //level.getMaxBuildHeight()) {
            BlockState state = level.getBlockState(checkPos);

            if (state.isAir()) {
                layersAbove = 0;
                checkPos.move(0, 1, 0);
                continue;
            }
            if (state.is(Blocks.WATER)) {
                layersAbove = 0;
                checkPos.move(0, 1, 0);
                continue;
            }
            if ((state.is(Blocks.DEEPSLATE)) && (checkPos.getY() > 8)) {
                layersAbove = 0;
                checkPos.move(0, 1, 0);
                continue;
            }
            if (state.is(Blocks.COBBLESTONE)) {
                layersAbove = 0;
                checkPos.move(0, 1, 0);
                continue;
            }
            if (state.is(BlockTags.LEAVES)) {
                layersAbove = 0;
                checkPos.move(0, 1, 0);
                continue;
            }
            layersAbove++;
            if (layersAbove > 3) {
                return true;
            }
            checkPos.move(0, 1, 0);
        }
        return false; // too few layers above -> do not consider as cave
    }

    public static boolean caveCheckStageTwo(Level level, BlockPos pos) {
        //UndeadNights.LOGGER.info("--------------------> Cave2 check at position {} {} {}", pos.getX(), pos.getY(), pos.getZ());
        AABB box = new AABB(pos).inflate(10, 0, 10);
        AtomicBoolean isCave = new AtomicBoolean(true);
        BlockPos.MutableBlockPos.betweenClosedStream(box)
            .forEach(c -> {
                //int y2 = level.getHeight(Heightmap.Types.MOTION_BLOCKING, c.getX(), c.getZ());
                int y1 = level.getHeight(Heightmap.Types.MOTION_BLOCKING, c.getX(), c.getZ());
                if ((c.getY() + 5) >= y1) {
                    isCave.set(false);
                }
            });
        return isCave.get();
    }

    @SuppressWarnings("unused")
    public static boolean hasDirectPath(Level level, BlockPos start, BlockPos end) {
        net.minecraft.world.phys.Vec3 startVec = net.minecraft.world.phys.Vec3.atCenterOf(start);
        net.minecraft.world.phys.Vec3 endVec = net.minecraft.world.phys.Vec3.atCenterOf(end);
        net.minecraft.world.phys.Vec3 dir = endVec.subtract(startVec);
        double dist = dir.length();
        if (dist == 0.0) return true;

        double samplesPerBlock = 4.0; // Precision: samples per block
        int steps = (int) Math.ceil(dist * samplesPerBlock);
        net.minecraft.world.phys.Vec3 step = dir.scale(1.0 / steps);
        net.minecraft.world.phys.Vec3 cur = startVec;

        for (int i = 0; i <= steps; i++) {
            BlockPos samplePos = BlockPos.containing(cur.x(), cur.y(), cur.z());
            BlockState state = level.getBlockState(samplePos);
            // If the block has a collision shape, the path is blocked
            if (!state.getCollisionShape(level, samplePos).isEmpty()) {
                return false;
            }
            cur = cur.add(step);
        }
        return true;
    }

    @SuppressWarnings("unused")
    public static boolean canPathfind(Level level, BlockPos start, BlockPos end, float mobWidth, float mobHeight) {
        net.minecraft.world.phys.Vec3 startVec = net.minecraft.world.phys.Vec3.atCenterOf(start);
        net.minecraft.world.phys.Vec3 endVec = net.minecraft.world.phys.Vec3.atCenterOf(end);
        net.minecraft.world.phys.Vec3 dir = endVec.subtract(startVec);
        double dist = dir.length();
        if (dist == 0.0) return true;

        double samplesPerBlock = 3.0;
        int steps = (int) Math.ceil(dist * samplesPerBlock);
        net.minecraft.world.phys.Vec3 step = dir.scale(1.0 / steps);
        net.minecraft.world.phys.Vec3 cur = startVec;

        double halfWidth = mobWidth / 2.0;

        for (int i = 0; i <= steps; i++) {
            AABB entityBox = new AABB(
                    cur.x - halfWidth, cur.y, cur.z - halfWidth,
                    cur.x + halfWidth, cur.y + mobHeight, cur.z + halfWidth
            );

            // compute integer block ranges from AABB bounds
            int minX = (int) Math.floor(entityBox.minX);
            int minY = (int) Math.floor(entityBox.minY);
            int minZ = (int) Math.floor(entityBox.minZ);
            int maxX = (int) Math.floor(entityBox.maxX);
            int maxY = (int) Math.floor(entityBox.maxY);
            int maxZ = (int) Math.floor(entityBox.maxZ);

            for (int bx = minX; bx <= maxX; bx++) {
                for (int by = minY; by <= maxY; by++) {
                    for (int bz = minZ; bz <= maxZ; bz++) {
                        BlockPos bpos = new BlockPos(bx, by, bz);
                        BlockState state = level.getBlockState(bpos);
                        if (!state.getCollisionShape(level, bpos).isEmpty()) {
                            return false;
                        }
                    }
                }
            }

            cur = cur.add(step);
        }

        return true;
    }

    /**
     * Check whether a mob (with given width and height) can pathfind from start to end.
     * This allows stepping up or down by one block when necessary.
     * It samples points along a straight line and verifies the entity AABB is free.
     */
    @SuppressWarnings("unused")
    public static boolean canMobPathfind(Level level, BlockPos start, BlockPos end, float mobWidth, float mobHeight) {
        Vec3 startVec = Vec3.atCenterOf(start);
        Vec3 endVec = Vec3.atCenterOf(end);
        Vec3 dir = endVec.subtract(startVec);
        double dist = dir.length();
        if (dist == 0.0) return true;

        double samplesPerBlock = 3.0; // sampling density
        int steps = (int) Math.ceil(dist * samplesPerBlock);
        Vec3 step = dir.scale(1.0 / steps);
        Vec3 cur = startVec;

        double halfWidth = mobWidth / 2.0;

        for (int i = 0; i <= steps; i++) {
            AABB entityBox = new AABB(
                    cur.x() - halfWidth, cur.y(), cur.z() - halfWidth,
                    cur.x() + halfWidth, cur.y() + mobHeight, cur.z() + halfWidth
            );

            // If space is free at current height, continue
            if (isAABBFree(level, entityBox)) {
                cur = cur.add(step);
                continue;
            }

            // Try stepping up by 1 block
            Vec3 upVec = new Vec3(cur.x(), cur.y() + 1.0, cur.z());
            AABB upBox = new AABB(
                    upVec.x() - halfWidth, upVec.y(), upVec.z() - halfWidth,
                    upVec.x() + halfWidth, upVec.y() + mobHeight, upVec.z() + halfWidth
            );
            BlockPos upCenter = BlockPos.containing(upVec.x(), upVec.y(), upVec.z());
            if (upVec.y() + mobHeight <= level.getMaxBuildHeight() && isAABBFree(level, upBox) && hasSolidBlockBelow(level, upCenter)) {
                // step up and continue
                cur = upVec.add(step);
                continue;
            }

            // Try stepping down by 1 block
            Vec3 downVec = new Vec3(cur.x(), cur.y() - 1.0, cur.z());
            AABB downBox = new AABB(
                    downVec.x() - halfWidth, downVec.y(), downVec.z() - halfWidth,
                    downVec.x() + halfWidth, downVec.y() + mobHeight, downVec.z() + halfWidth
            );
            BlockPos downCenter = BlockPos.containing(downVec.x(), downVec.y(), downVec.z());
            if (downVec.y() >= level.getMinBuildHeight() && isAABBFree(level, downBox) && hasSolidBlockBelow(level, downCenter)) {
                // step down and continue
                cur = downVec.add(step);
                continue;
            }

            // No valid adjustment found -> path blocked
            return false;
        }

        return true;
    }

    /**
     * A simple A* entry points (overloads). The real implementation lives in findPathAStarWithLimits.
     */
    public static List<BlockPos> findPathAStar(Level level, BlockPos start, BlockPos end, float mobWidth, float mobHeight, int maxNodes) {
        // maintain previous default behavior: allow drops up to 4 blocks, default maxUp=1, exact target
        return findPathAStarWithLimits(level, start, end, mobWidth, mobHeight, maxNodes, 4, 1, 0);
    }

    public static List<BlockPos> findPathAStar(Level level, BlockPos start, BlockPos end, float mobWidth, float mobHeight, int maxNodes, int maxDown) {
        // forward with default maxUp=1 and exact target
        return findPathAStarWithLimits(level, start, end, mobWidth, mobHeight, maxNodes, maxDown, 1, 0);
    }

    public static List<BlockPos> findPathAStar(Level level, BlockPos start, BlockPos end, float mobWidth, float mobHeight, int maxNodes, int maxDown, int maxUp, int endRadius) {
        return findPathAStarWithLimits(level, start, end, mobWidth, mobHeight, maxNodes, maxDown, maxUp, Math.max(0, endRadius));
    }

    public static boolean canMobPathfindAStar(Level level, BlockPos start, BlockPos end, float mobWidth, float mobHeight, int maxNodes, int maxDown, int maxUp) {
        return canMobPathfindAStar(level, start, end, mobWidth, mobHeight, maxNodes, maxDown, maxUp, 10);
    }

    /**
     * Backwards-compatible: keep existing 2-arg public overloads as-is; new overloads above call the 4-arg private implementation.
     */

    /** Final overload: configurable maxDown, maxUp and endRadius. */
    public static boolean canMobPathfindAStar(Level level, BlockPos start, BlockPos end, float mobWidth, float mobHeight, int maxNodes, int maxDown, int maxUp, int endRadius) {
        List<BlockPos> path = findPathAStar(level, start, end, mobWidth, mobHeight, maxNodes, maxDown, maxUp, Math.max(0, endRadius));
        return !path.isEmpty();
    }

    /**
     * Internal implementation supporting configurable maxUp (how many blocks can be stepped up in one move)
     * and endRadius (goal tolerance). This is an overloaded variant of the older findPathAStarWithLimits.
     */
    private static List<BlockPos> findPathAStarWithLimits(Level level, BlockPos start, BlockPos end, float mobWidth, float mobHeight, int maxNodes, int maxDown, int maxUp, int endRadius) {
        class PathNode {
            final BlockPos pos;
            final double g; // cost from start
            final double f; // g + heuristic
            final PathNode parent;
            PathNode(BlockPos pos, double g, double f, PathNode parent) { this.pos = pos; this.g = g; this.f = f; this.parent = parent; }
        }

        BlockPos fixedStart = start == null ? null : start.immutable();
        BlockPos fixedEnd = end == null ? null : end.immutable();
        if (fixedStart == null || fixedEnd == null) return Collections.emptyList();

        if (level.getBlockState(fixedStart).is(Blocks.WATER)) {
            BlockPos alt = findNearbyStandable(level, start, mobWidth, mobHeight, 5);
            if (alt == null || level.getBlockState(alt).is(Blocks.WATER)) return Collections.emptyList();
            fixedStart = alt.immutable();
        }

        PriorityQueue<PathNode> open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        Map<BlockPos, Double> gScore = new HashMap<>();
        Set<BlockPos> closed = new HashSet<>();

        PathNode startNode = new PathNode(fixedStart, 0.0, heuristic(fixedStart, fixedEnd), null);
        open.add(startNode);
        gScore.put(fixedStart, 0.0);

        int expanded = 0;
        int[][] dirs = new int[][]{{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight();

        while (!open.isEmpty() && expanded < maxNodes) {
            PathNode current = open.poll();
            if (current == null) break;

            double distToGoal = heuristic(current.pos, fixedEnd);
            if (distToGoal <= (double) endRadius) {
                LinkedList<BlockPos> path = new LinkedList<>();
                PathNode it = current;
                while (it != null) {
                    path.addFirst(it.pos);
                    it = it.parent;
                }
                return path;
            }

            Double bestG = gScore.get(current.pos);
            if (bestG != null && current.g > bestG + 1e-6) continue;

            closed.add(current.pos);
            expanded++;

            for (int[] d : dirs) {
                int nx = current.pos.getX() + d[0];
                int nz = current.pos.getZ() + d[1];

                int scanTop = Math.min(current.pos.getY() + maxUp, maxY);
                int scanBottom = Math.max(current.pos.getY() - maxDown, minY);
                for (int ny = scanTop; ny >= scanBottom; ny--) {
                    int vertDiff = ny - current.pos.getY();
                    if (vertDiff > maxUp || vertDiff < -maxDown) continue;
                    BlockPos neighbor = new BlockPos(nx, ny, nz);

                    if (closed.contains(neighbor)) continue;

                    BlockState neighborState = level.getBlockState(neighbor);
                    // reject water blocks (both source and flowing) from being expanded
                    if (neighborState.is(Blocks.WATER) || neighborState.getFluidState().is(FluidTags.WATER)) continue;
                    if (!canStandAt(level, neighbor, mobWidth, mobHeight)) continue;

                    boolean isDiagonal = Math.abs(d[0]) == 1 && Math.abs(d[1]) == 1;
                    double horizontalCost = isDiagonal ? 1.41421356237 : 1.0;
                    if (vertDiff == 0) horizontalCost *= 0.9;
                    double verticalPenalty = Math.abs(vertDiff) * (vertDiff < 0 ? 1.0 : 0.5);
                    double tentativeG = current.g + horizontalCost + verticalPenalty;

                    Double existingG = gScore.get(neighbor);
                    if (existingG == null || tentativeG < existingG - 1e-9) {
                        gScore.put(neighbor, tentativeG);
                        double f = tentativeG + heuristic(neighbor, fixedEnd);
                        PathNode node = new PathNode(neighbor, tentativeG, f, current);
                        open.add(node);
                    }

                    // stop vertical scanning when we found a standable block (we already ensured neighbor is standable above)
                    if (canStandAt(level, neighbor, mobWidth, mobHeight)) break;
                }
            }
        }

        return Collections.emptyList();
    }

    private static BlockPos findNearbyStandable(Level level, BlockPos center, float mobWidth, float mobHeight, int radius) {
        BlockPos best = null;
        int bestDist = Integer.MAX_VALUE;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos candidate = center.offset(dx, dy, dz);
                    if (canStandAt(level, candidate, mobWidth, mobHeight)) {
                        int dist = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
                        if (dist < bestDist) {
                            bestDist = dist;
                            best = candidate.immutable();
                        }
                    }
                }
            }
        }
        return best;
    }

    private static double heuristic(BlockPos a, BlockPos b) {
        double dx = (double)a.getX() - (double)b.getX();
        double dy = (double)a.getY() - (double)b.getY();
        double dz = (double)a.getZ() - (double)b.getZ();
        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    private static boolean checkSpawnLocation(ServerLevel level, double x, double y, double z) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(x, y, z);

        BlockState blockState = level.getBlockState(mutable);
        Block block = blockState.getBlock();
        boolean doesNotBlockMovement = block != Blocks.COBWEB && block != Blocks.BAMBOO_SAPLING;
        boolean notWater = true;
        if (!MainConfig.getHordeWavesCanSpawnInWater()) {
            notWater = !(blockState.getFluidState().is(FluidTags.WATER));
        }
        boolean notLeaves = true;
        if (!MainConfig.getHordeWavesCanSpawnOnTrees()) {
            notLeaves = !(blockState.getBlock() instanceof LeavesBlock);
        }

        return doesNotBlockMovement && notLeaves && notWater;
    }

    private static boolean canStandAt(Level level, BlockPos pos, float mobWidth, float mobHeight) {
        double cx = pos.getX() + 0.5;
        double cz = pos.getZ() + 0.5;
        double bottomY = pos.getY();
        double topY = bottomY + mobHeight;
        double halfWidth = mobWidth / 2.0;

        AABB box = new AABB(cx - halfWidth, bottomY, cz - halfWidth, cx + halfWidth, topY, cz + halfWidth);

        if (!isAABBFree(level, box)) return false;

        return hasSolidBlockBelow(level, pos);
    }

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

   public static BlockPos findEndPositionForPathAStar(Level level, BlockPos end, int distance) {
        // default settings: do not allow end in water, mob size standard, large node limit, maxDown=4, maxUp=1
        return findEndPositionForPathAStar(level, end, distance, false, 0.6f, 1.8f, 10000, 4, 1, 0);
    }

    public static BlockPos findEndPositionForPathAStar(Level level, BlockPos start, int distance, boolean canEndInWater, float mobWidth, float mobHeight, int maxNodes, int maxDown, int maxUp) {
        // default radius 0 for exact target behavior
        return findEndPositionForPathAStar(level, start, distance, canEndInWater, mobWidth, mobHeight, maxNodes, maxDown, maxUp, 10);
    }

    public static BlockPos findEndPositionForPathAStar(Level level, BlockPos start, int distance, boolean canEndInWater, float mobWidth, float mobHeight, int maxNodes, int maxDown, int maxUp, int radius) {
        if (start == null || level == null) return null;

        final int minWorldY = level.getMinBuildHeight();
        final int maxWorldY = level.getMaxBuildHeight();

        // quick AABB check for start
        double sx = start.getX() + 0.5;
        double sz = start.getZ() + 0.5;
        double bottomY = start.getY();
        double topY = bottomY + mobHeight;
        double halfWidth = mobWidth / 2.0;
        AABB startBox = new AABB(sx - halfWidth, bottomY, sz - halfWidth, sx + halfWidth, topY, sz + halfWidth);
        if (!isAABBFree(level, startBox)) return null;

        // if start is in air, lower until we find a standable block
        if (!hasSolidBlockBelow(level, start)) {
            BlockPos found = null;
            int sxInt = start.getX();
            int szInt = start.getZ();
            for (int y = start.getY() - 1; y >= minWorldY; y--) {
                BlockPos candidate = new BlockPos(sxInt, y, szInt);
                double cbottomY = candidate.getY();
                double ctopY = cbottomY + mobHeight;
                AABB centered = new AABB(candidate.getX() + 0.5 - halfWidth, cbottomY, candidate.getZ() + 0.5 - halfWidth,
                                         candidate.getX() + 0.5 + halfWidth, ctopY, candidate.getZ() + 0.5 + halfWidth);
                if (!isAABBFree(level, centered)) continue;
                if (hasSolidBlockBelow(level, candidate)) { found = candidate; break; }
            }
            if (found == null) return null;
            start = found;
        }

        final int[][] dirs = new int[][]{{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        Map<Long, BlockState> blockStateCache = new HashMap<>();
        Map<Long, Integer> bestDepth = new HashMap<>();
        List<BlockPos> exactDepthCandidates = new ArrayList<>();

        // lightweight node for search
        class Node { final int x,y,z; final long key; final int depth; final double score; Node(int x,int y,int z,long key,int depth,double score){this.x=x;this.y=y;this.z=z;this.key=key;this.depth=depth;this.score=score;} }
        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.score));

        int startX = start.getX(); int startY = start.getY(); int startZ = start.getZ();
        open.add(new Node(startX, startY, startZ, start.asLong(), 0, -0.0));
        bestDepth.put(start.asLong(), 0);

        int expanded = 0;

        while (!open.isEmpty() && expanded < Math.max(1, maxNodes)) {
            Node cur = open.poll();
            if (cur == null) break;

            if (cur.depth == distance) {
                mutable.set(cur.x, cur.y, cur.z);
                long cKey = mutable.asLong();
                BlockState endState = blockStateCache.computeIfAbsent(cKey, k -> level.getBlockState(mutable));
                boolean isLava = endState.is(Blocks.LAVA) || endState.getFluidState().is(FluidTags.LAVA);
                if (isLava) continue;
                boolean isWater = endState.is(Blocks.WATER) || endState.getFluidState().is(FluidTags.WATER);
                if (isWater && !canEndInWater) continue;
                exactDepthCandidates.add(new BlockPos(cur.x, cur.y, cur.z));
                continue;
            }

            Integer recorded = bestDepth.get(cur.key);
            if (recorded != null && cur.depth > recorded) continue;
            expanded++;

            for (int[] d : dirs) {
                int nx = cur.x + d[0];
                int nz = cur.z + d[1];
                int scanTop = Math.min(cur.y + maxUp, maxWorldY);
                int scanBottom = Math.max(cur.y - maxDown, minWorldY);
                for (int ny = scanTop; ny >= scanBottom; ny--) {
                    int vert = ny - cur.y;
                    if (vert > maxUp || vert < -maxDown) continue;
                    mutable.set(nx, ny, nz);
                    long nKey = mutable.asLong();
                    int nextDepth = cur.depth + 1;
                    Integer prev = bestDepth.get(nKey);
                    if (prev != null && nextDepth >= prev) continue;

                    BlockState neighborState = blockStateCache.computeIfAbsent(nKey, k -> level.getBlockState(mutable));
                    boolean neighborIsWater = neighborState.is(Blocks.WATER) || neighborState.getFluidState().is(FluidTags.WATER);
                    if (nextDepth < distance && neighborIsWater) continue;
                    if (!canStandAt(level, mutable, mobWidth, mobHeight)) continue;

                    double dx = (double)nx - (double)startX;
                    double dz = (double)nz - (double)startZ;
                    double horiz = Math.sqrt(dx*dx + dz*dz);
                    double vertFromStart = Math.abs((double)ny - (double)startY);
                    double score = -horiz + (nextDepth * 0.001) + vertFromStart;

                    bestDepth.put(nKey, nextDepth);
                    open.add(new Node(nx, ny, nz, nKey, nextDepth, score));

                    // stop vertical scanning when standable (we already checked)
                    if (canStandAt(level, mutable, mobWidth, mobHeight)) break;
                }
            }
        }

        if (!exactDepthCandidates.isEmpty()) {
            exactDepthCandidates.sort((a,b) -> {
                double adx = a.getX() - startX; double adz = a.getZ() - startZ; double ad = Math.sqrt(adx*adx + adz*adz);
                double bdx = b.getX() - startX; double bdz = b.getZ() - startZ; double bd = Math.sqrt(bdx*bdx + bdz*bdz);
                return Double.compare(bd, ad);
            });

            double minStraightFactor = 0.8;
            double minStrictHoriz = Math.max(radius, distance * minStraightFactor);
            boolean requireStrict = distance >= 50;

            for (BlockPos c : exactDepthCandidates) {
                double ddx = c.getX() - startX; double ddz = c.getZ() - startZ;
                double h = Math.sqrt(ddx*ddx + ddz*ddz);
                if (h < minStrictHoriz) continue;
                if (Math.abs(c.getY() - startY) > 20) continue;
                return c;
            }

            if (!requireStrict) {
                for (BlockPos c : exactDepthCandidates) {
                    double ddx = c.getX() - startX; double ddz = c.getZ() - startZ;
                    double h = Math.sqrt(ddx*ddx + ddz*ddz);
                    if (h < radius) continue;
                    if (Math.abs(c.getY() - startY) > 20) continue;
                    return c;
                }
            }
        }

        return null;
    }

    // --- FAST greedy randomized alternative (much faster but not guaranteed optimal) ---

    public static BlockPos findEndPositionForPathFast(Level level, BlockPos start, int distance) {
        // default: preserve previous defaults but tuned for reliability
        return findEndPositionForPathFast(level, start, distance, false, 0.6f, 1.8f, 250, 4, 1, 0, 3);
     }

    public static BlockPos findEndPositionForPathFast(Level level, BlockPos start, int distance, boolean canEndInWater, float mobWidth, float mobHeight, int maxAttempts, int maxDown, int maxUp, int radius, int tries) {
         if (level == null || start == null || distance <= 0 || maxAttempts <= 0 || tries <= 0) return null;

        // enable temporary debug logging controlled by the global config flag
        final boolean debug = MainConfig.getPrintDebugMessages();

        // quick validation and adjust start if necessary (similar to A* version)
        final int minWorldY = level.getMinBuildHeight();
        final int maxWorldY = level.getMaxBuildHeight();

        double sx = start.getX() + 0.5;
        double sz = start.getZ() + 0.5;
        double bottomY = start.getY();
        double topY = bottomY + mobHeight;
        double halfWidth = mobWidth / 2.0;
        AABB startBox = new AABB(sx - halfWidth, bottomY, sz - halfWidth, sx + halfWidth, topY, sz + halfWidth);
        if (!isAABBFree(level, startBox)) return null;

        // if start is in air, lower until we find a standable block
        if (!hasSolidBlockBelow(level, start)) {
            BlockPos found = null;
            int sxInt = start.getX();
            int szInt = start.getZ();
            for (int y = start.getY() - 1; y >= minWorldY; y--) {
                BlockPos candidate = new BlockPos(sxInt, y, szInt);
                double cbottomY = candidate.getY();
                double ctopY = cbottomY + mobHeight;
                AABB centered = new AABB(candidate.getX() + 0.5 - halfWidth, cbottomY, candidate.getZ() + 0.5 - halfWidth,
                                         candidate.getX() + 0.5 + halfWidth, ctopY, candidate.getZ() + 0.5 + halfWidth);
                if (!isAABBFree(level, centered)) continue;
                if (hasSolidBlockBelow(level, candidate)) { found = candidate; break; }
            }
            if (found == null) return null;
            start = found;
        }

        final int[][] dirs = new int[][]{{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
        BlockPos.MutableBlockPos cur = new BlockPos.MutableBlockPos(start.getX(), start.getY(), start.getZ());
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        // Try many randomized greedy walks; each walk takes exactly 'distance' steps. Fast because we avoid global open sets.
        // limit how many full A* confirmations we perform per function call
        int astarChecks = 0;
        final int MAX_ASTAR_CHECKS = 5;

        double bestOverallScore = Double.NEGATIVE_INFINITY;
        BlockPos bestOverallCandidate = null;

         for (int attempt = 0; attempt < maxAttempts; attempt++) {
            // start from original start each attempt
            cur.set(start.getX(), start.getY(), start.getZ());
            int curY = cur.getY();

            boolean failed = false;
            for (int stepIdx = 0; stepIdx < distance; stepIdx++) {
                // snapshot base coordinates for this step (do not mutate while evaluating candidates)
                final int baseX = cur.getX();
                final int baseZ = cur.getZ();
                final int baseY = curY;

                // randomized order of directions
                Integer[] order = new Integer[dirs.length];
                for (int i = 0; i < dirs.length; i++) order[i] = i;
                Collections.shuffle(Arrays.asList(order), new Random(rnd.nextLong()));

                int localTries = Math.min(tries, dirs.length);
                int tried = 0;

                double bestScore = Double.NEGATIVE_INFINITY;
                int chosenX = Integer.MIN_VALUE, chosenY = Integer.MIN_VALUE, chosenZ = Integer.MIN_VALUE;

                for (int oi = 0; oi < order.length && tried < localTries; oi++) {
                    int[] d = dirs[order[oi]];
                    tried++;
                    int nx = baseX + d[0];
                    int nz = baseZ + d[1];

                    // pick ny by scanning from top allowed downwards to find first standable block
                    int scanTop = Math.min(baseY + maxUp, maxWorldY);
                    int scanBottom = Math.max(baseY - maxDown, minWorldY);
                    int foundNy = Integer.MIN_VALUE;
                    for (int ny = scanTop; ny >= scanBottom; ny--) {
                        BlockPos cand = new BlockPos(nx, ny, nz);
                        BlockState st = level.getBlockState(cand);
                        boolean isWater = st.is(Blocks.WATER) || st.getFluidState().is(FluidTags.WATER);
                        if (stepIdx < distance - 1 && isWater) continue; // avoid water on intermediate steps
                        if (st.is(Blocks.LAVA) || st.getFluidState().is(FluidTags.LAVA)) continue;
                        // Fast pre-check: cheap heuristic to avoid expensive AABB checks most of the time
                        if (!quickStandable(level, cand, mobWidth, mobHeight)) continue;
                        if (canStandAt(level, cand, mobWidth, mobHeight)) { foundNy = ny; break; }
                    }
                    if (foundNy == Integer.MIN_VALUE) continue;

                    // cheap chebyshev lower bound to avoid obviously unreachable candidates
                    int cheb = Math.max(Math.abs(nx - start.getX()), Math.abs(nz - start.getZ()));
                    if (cheb > distance + 3) continue;

                    double dx = (double)nx - (double)start.getX();
                    double dz = (double)nz - (double)start.getZ();
                    double horiz = Math.sqrt(dx*dx + dz*dz);
                    double verticalPenalty = Math.abs(foundNy - start.getY()) * 0.2;
                    double score = horiz - verticalPenalty + rnd.nextDouble() * 0.15;
                    if (score > bestScore) {
                        bestScore = score;
                        chosenX = nx; chosenY = foundNy; chosenZ = nz;
                    }
                }

                if (chosenX == Integer.MIN_VALUE) {
                    if (debug) UndeadNights.LOGGER.info("findEndFast: attempt {} step {} - no valid direction found from {} {} {}", attempt, stepIdx, baseX, baseY, baseZ);
                    failed = true; break;
                }

                cur.set(chosenX, chosenY, chosenZ);
                curY = chosenY;
            }

            if (failed) continue;

            BlockPos candidate = cur.immutable();
            // final candidate checks
            BlockState finalState = level.getBlockState(candidate);
            if (finalState.is(Blocks.LAVA) || finalState.getFluidState().is(FluidTags.LAVA)) {
                if (debug) UndeadNights.LOGGER.info("findEndFast: candidate {} rejected: lava", candidate);
                continue;
            }
            if (!canEndInWater && (finalState.is(Blocks.WATER) || finalState.getFluidState().is(FluidTags.WATER))) {
                if (debug) UndeadNights.LOGGER.info("findEndFast: candidate {} rejected: water (end not allowed)", candidate);
                continue;
            }

            // Chebyshev distance (diagonal moves allowed) is a lower bound on number of steps
            int cheb = Math.max(Math.abs(candidate.getX() - start.getX()), Math.abs(candidate.getZ() - start.getZ()));
            if (cheb > distance + 3) {
                if (debug) UndeadNights.LOGGER.info("findEndFast: candidate {} rejected: chebyshev {} > allowed {}", candidate, cheb, distance + 3);
                continue; // too far horizontally to reach in 'distance' steps
            }

            // compute a simple candidate score (higher is better) to use as fallback
            double candidateScore = -Math.abs(cheb - distance) - Math.abs(candidate.getY() - start.getY()) * 0.1 + rnd.nextDouble() * 0.01;

            // If chebyshev is within a reasonable window try a cheap straight-line sampling path check
            if (Math.abs(cheb - distance) <= 4) {
                try {
                    boolean cheapOk = canMobPathfind(level, start, candidate, mobWidth, mobHeight);
                    if (cheapOk) {
                        if (debug) UndeadNights.LOGGER.info("findEndFast: candidate {} accepted by cheap path check", candidate);
                        return candidate;
                    } else {
                        if (debug) UndeadNights.LOGGER.info("findEndFast: candidate {} failed cheap path check", candidate);
                    }
                } catch (Throwable t) {
                    if (debug) UndeadNights.LOGGER.info("findEndFast: candidate {} cheap path check threw: {}", candidate, t.toString());
                }
            }

            // Fallback: limited A* confirmation but cap the number of such checks
            if (astarChecks < MAX_ASTAR_CHECKS) {
                astarChecks++;
                int aStarMaxNodes = Math.max(300, Math.min(2000, distance * 8));
                List<BlockPos> realPath = findPathAStar(level, start, candidate, mobWidth, mobHeight, aStarMaxNodes, Math.max(1, maxDown), Math.max(1, maxUp), 0);
                if (realPath == null || realPath.isEmpty()) {
                    if (debug) UndeadNights.LOGGER.info("findEndFast: candidate {} A* returned empty path", candidate);
                    // no valid path -> skip candidate
                    double penalized = candidateScore - 5.0;
                    if (penalized > bestOverallScore) { bestOverallScore = penalized; bestOverallCandidate = candidate; }
                    continue;
                }
                int pathSteps = Math.max(0, realPath.size() - 1);
                if (Math.abs(pathSteps - distance) > 3) {
                    if (debug) UndeadNights.LOGGER.info("findEndFast: candidate {} A* pathSteps {} deviates from required {}", candidate, pathSteps, distance);
                    // path length deviates too much -> skip
                    double penalized = candidateScore - 2.0;
                    if (penalized > bestOverallScore) { bestOverallScore = penalized; bestOverallCandidate = candidate; }
                    continue;
                }
                if (debug) UndeadNights.LOGGER.info("findEndFast: candidate {} accepted by A* (steps={})", candidate, pathSteps);
                return candidate;
            }

            // No A* confirmations left and cheap checks failed -> keep as fallback candidate
            if (candidateScore > bestOverallScore) { bestOverallScore = candidateScore; bestOverallCandidate = candidate; if (debug) UndeadNights.LOGGER.info("findEndFast: candidate {} recorded as fallback (score={})", candidate, candidateScore); }
             continue;
         }

         // If we reached here no confirmed candidate was found. Return the best-scoring fallback if available.
        if (debug) UndeadNights.LOGGER.info("findEndFast: returning fallback candidate {} (score={})", bestOverallCandidate, bestOverallScore);
        return bestOverallCandidate;
     }

    private static boolean tryComputePath(Level level, BlockPos start, BlockPos end, long pTime) {
        //BlockPos blockpos = pTarget.getTarget().currentBlockPosition();

        HordeZombieEntity pMob = new HordeZombieEntity(ModEntities.HORDE_ZOMBIE.get(), level);
        pMob.setPos(start.getX() + 0.5, start.getY(), start.getZ() + 0.5);

        Path path = pMob.getNavigation().createPath(end, 0);
        float speedModifier = 1.2f;//pTarget.getSpeedModifier();
        Brain<?> brain = pMob.getBrain();
        if (reachedTarget(pMob, end,0)) {
            brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        } else {
            boolean flag = path != null && path.canReach();
            if (flag) {
                brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            } else if (!brain.hasMemoryValue(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)) {
                brain.setMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, pTime);
            }

            if (path != null) {
                return true;
            }

            Vec3 vec3 = DefaultRandomPos.getPosTowards((PathfinderMob)pMob, 10, 7, Vec3.atBottomCenterOf(end), (double)((float)Math.PI / 2F));
            if (vec3 != null) {
                path = pMob.getNavigation().createPath(vec3.x, vec3.y, vec3.z, 0);
                return path != null;
            }
        }

        return false;
    }

    private static boolean reachedTarget(Mob pMob, BlockPos pTarget, float closeEnoughDist) {
        return pTarget.distManhattan(pMob.blockPosition()) <= closeEnoughDist;
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
        HordeZombieEntity probe = new HordeZombieEntity(ModEntities.HORDE_ZOMBIE.get(), level);
        probe.finalizeSpawn((ServerLevelAccessor) level, level.getCurrentDifficultyAt(start), MobSpawnType.MOB_SUMMONED, null, null);
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
                if (MainConfig.getBlockLightLevelsInfluenceMonsterSpawns() && !HordesSpawning.isDarkEnoughToSpawn((ServerLevelAccessor) level, cand)) continue;
                if (!hasSolidBlockBelow(level, cand)) continue;
                if (!isAABBFreeForSpawn(level, new AABB(cand.getX() + 0.5 - 0.3, cand.getY() + 0.001, cand.getZ() + 0.5 - 0.3, cand.getX() + 0.5 + 0.3, cand.getY() + 1.8 - 0.001, cand.getZ() + 0.5 + 0.3))) continue;

                try {
                    // createPath may return null or an empty path if unreachable
                    var nav = probe.getNavigation();
                    // place probe at candidate center before asking it to path to the player
                    probe.setPos(cand.getX() + 0.5, cand.getY(), cand.getZ() + 0.5);
                    // single navigation check to player (candidate -> player)
                    Path path = nav.createPath(player, 0);

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

    /*
    @Nullable
    public Path createPath(Entity pEntity, int pAccuracy) {
        return this.createPath(ImmutableSet.of(pEntity.blockPosition()), 16, true, pAccuracy);
    }

    @Nullable
    protected static Path createPath(Set<BlockPos> pTargets, int pRegionOffset, boolean pOffsetUpward, int pAccuracy) {
        return createPath(pTargets, pRegionOffset, pOffsetUpward, pAccuracy, (float)128);
    }

    @Nullable
    protected static Path createPath(Set<BlockPos> pTargets, int pRegionOffset, boolean pOffsetUpward, int pAccuracy, float pFollowRange) {
        if (pTargets.isEmpty()) {
            return null;
        } else if (this.mob.getY() < (double)this.level.getMinBuildHeight()) {
            return null;
        } else if (!this.canUpdatePath()) {
            return null;
        } else if (this.path != null && !this.path.isDone() && pTargets.contains(this.targetPos)) {
            return this.path;
        } else {
            this.level.getProfiler().push("pathfind");
            BlockPos blockpos = pOffsetUpward ? this.mob.blockPosition().above() : this.mob.blockPosition();
            int i = (int)(pFollowRange + (float)pRegionOffset);
            PathNavigationRegion pathnavigationregion = new PathNavigationRegion(this.level, blockpos.offset(-i, -i, -i), blockpos.offset(i, i, i));
            Path path = this.pathFinder.findPath(pathnavigationregion, this.mob, pTargets, pFollowRange, pAccuracy, this.maxVisitedNodesMultiplier);
            this.level.getProfiler().pop();
            if (path != null && path.getTarget() != null) {
                this.targetPos = path.getTarget();
                this.reachRange = pAccuracy;
                this.resetStuckTimeout();
            }

            return path;
        }
    }

     */

    /**
     * Quick, cheap standability check used as a prefilter before the expensive full AABB checks.
     * It checks:
     *  - block below has collision or is slab/stair/fence/door
     *  - the foot block and up to 2 blocks above (depending on mobHeight) don't have full collision shapes
     * The intent is to filter out obvious bad candidates quickly.
     */
    private static boolean quickStandable(Level level, BlockPos pos, float mobWidth, float mobHeight) {
        if (level == null || pos == null) return false;
        // below
        BlockPos below = pos.below();
        if (below.getY() < level.getMinBuildHeight()) return false;
        BlockState belowState = level.getBlockState(below);
        boolean belowSolid = !belowState.getCollisionShape(level, below).isEmpty()
                || belowState.is(BlockTags.SLABS)
                || belowState.is(BlockTags.STAIRS)
                || belowState.getBlock() instanceof FenceBlock
                || belowState.getBlock() instanceof FenceGateBlock
                || belowState.getBlock() instanceof DoorBlock;
        if (!belowSolid) return false;

        // quick check for headspace: only check a couple of blocks above
        int checks = Math.min(2, Math.max(1, (int)Math.ceil(mobHeight)));
        for (int dy = 0; dy < checks; dy++) {
            BlockPos p = pos.above(dy);
            BlockState s = level.getBlockState(p);
            if (!s.getCollisionShape(level, p).isEmpty()) {
                // allow non-solid such as fence/fencegate/door by their shapes being small; treat as blocking here
                return false;
            }
            if (s.is(Blocks.LAVA) || s.getFluidState().is(FluidTags.LAVA)) return false;
        }
        return true;
    }

    public static BlockPos findSpawnablePosition(Level level, BlockPos center, int radius) {
        return findSpawnablePosition(level, center, radius, 5);
    }

    public static BlockPos findSpawnablePosition(Level level, BlockPos center, int radius, boolean allowWater, float mobWidth, float mobHeight) {
        // preserve signature for callers but delegate to deltaY-based search with default deltaY=5
        return findSpawnablePosition(level, center, radius, 5);
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

    /**
     * Heuristic to determine if a player at the given position is inside a cave.
     * Note: player-built houses (or similar player structures) are explicitly
     * excluded and do NOT count as caves.
     *
     * Logic:
     *  - Quick surface check: if the position is at/near surface, not a cave.
     *  - Uses existing caveCheckStageOne/Two to identify likely caves.
     *  - House detection heuristic: scans a small area around the position and
     *    looks for typical building blocks (wood planks, glass, doors, fences,
     *    torches, lanterns). If multiple building/lighting blocks are present,
     *    we assume it's a house and return false (not a cave).
     */
    public static boolean isPlayerInCave(Level level, BlockPos pos) {
        if (level == null || pos == null) return false;

        // If the player is very close to or at the surface, it's not a cave
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        if (pos.getY() >= surfaceY - 2) return false;

        // quick existing checks: if stage one or two indicate not cave -> not cave
        if (!caveCheckStageOne(level, pos)) return false;
        if (!caveCheckStageTwo(level, pos)) return false;

        // House detection heuristic: collect evidence of player-built structure
        // Scan radius and vertical window
        final int hx = 6; // horizontal scan radius
        final int hy = 3; // vertical scan +/- from pos.y

        // Typical building materials / fixtures that indicate a constructed house
        var buildingMaterials = ImmutableSet.of(
                Blocks.OAK_PLANKS, Blocks.SPRUCE_PLANKS, Blocks.BIRCH_PLANKS, Blocks.JUNGLE_PLANKS,
                Blocks.ACACIA_PLANKS, Blocks.DARK_OAK_PLANKS, Blocks.CRIMSON_PLANKS, Blocks.WARPED_PLANKS,
                Blocks.GLASS, Blocks.GLASS_PANE,
                Blocks.BRICKS, Blocks.STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS,
                Blocks.TERRACOTTA, Blocks.WHITE_TERRACOTTA, Blocks.BRICK_STAIRS, Blocks.STONE_BRICK_STAIRS
        );

        int buildCount = 0;
        int lightCount = 0;
        int doorOrEntranceCount = 0;

        for (int dx = -hx; dx <= hx; dx++) {
            for (int dz = -hx; dz <= hx; dz++) {
                for (int dy = -hy; dy <= hy; dy++) {
                    BlockPos p = pos.offset(dx, dy, dz);
                    BlockState s = level.getBlockState(p);
                    Block b = s.getBlock();

                    // Never count obviously-natural cave fluids as building
                    if (b == Blocks.WATER || b == Blocks.LAVA) continue;

                    if (buildingMaterials.contains(b)) buildCount++;

                    // Lighting / fixtures commonly placed by players
                    if (b == Blocks.TORCH || b == Blocks.WALL_TORCH || b == Blocks.LANTERN || b == Blocks.SOUL_LANTERN || b == Blocks.GLOWSTONE || b == Blocks.SEA_LANTERN || b == Blocks.REDSTONE_LAMP) {
                        lightCount++;
                    }

                    // Doors, fence gates, fences indicate entrances / constructions
                    if (b instanceof DoorBlock || b instanceof FenceGateBlock || b instanceof FenceBlock) {
                        doorOrEntranceCount++;
                    }

                    // Early exit if very strong evidence of a building
                    if (buildCount >= 8 && (lightCount >= 1 || doorOrEntranceCount >= 1)) {
                        return false; // considered a house -> not a cave
                    }
                }
            }
        }

        // Final decision: if we saw several building blocks + fixtures, treat as house
        if (buildCount >= 6 && (lightCount >= 1 || doorOrEntranceCount >= 1)) return false;

        // Otherwise, it's a cave by our combined heuristics
        return true;
    }

    /** Convenience overload that accepts a ServerPlayer. */
    public static boolean isPlayerInCave(Level level, ServerPlayer player) {
        if (player == null) return false;
        return isPlayerInCave(level, player.blockPosition());
    }
}
