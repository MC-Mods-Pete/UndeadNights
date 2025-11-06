package net.petemc.undeadnights.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.concurrent.atomic.AtomicBoolean;

public class Helpers {
    public static boolean caveCheckStageOne(Level level, BlockPos pos) {
        int layersAbove = 0;
        int x = pos.getX();
        int z = pos.getZ();
        int y;

        UndeadNights.LOGGER.info("--------------> Player position {} {} {}", x, pos.getY(), z);

        y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        UndeadNights.LOGGER.info("----------------------------------------> Player is on surface (MOTION_BLOCKING_NO_LEAVES) {} {} {}", y, pos.getY(), level.getBlockState(pos));
        if (y == pos.getY()) {
            return false; // on surface
        }

        y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
        UndeadNights.LOGGER.info("----------------------------------------> Player is on surface (MOTION_BLOCKING) {} {} {}", y, pos.getY(), level.getBlockState(pos));
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
        UndeadNights.LOGGER.info("--------------------> Cave2 check at position {} {} {}", pos.getX(), pos.getY(), pos.getZ());
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
    public static java.util.List<BlockPos> findPathAStar(Level level, BlockPos start, BlockPos end, float mobWidth, float mobHeight, int maxNodes) {
        // maintain previous default behavior: allow drops up to 4 blocks
        return findPathAStarWithLimits(level, start, end, mobWidth, mobHeight, maxNodes, 4);
    }

    public static java.util.List<BlockPos> findPathAStar(Level level, BlockPos start, BlockPos end, float mobWidth, float mobHeight, int maxNodes, int maxDown) {
        return findPathAStarWithLimits(level, start, end, mobWidth, mobHeight, maxNodes, maxDown);
    }

    public static java.util.List<BlockPos> findPathAStar(Level level, BlockPos start, BlockPos end, float mobWidth, float mobHeight, int maxNodes, int maxDown, int maxUp, int endRadius) {
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
        java.util.List<BlockPos> path = findPathAStar(level, start, end, mobWidth, mobHeight, maxNodes, maxDown, maxUp, Math.max(0, endRadius));
        return !path.isEmpty();
    }

    /**
     * Internal implementation supporting configurable maxUp (how many blocks can be stepped up in one move)
     * and endRadius (goal tolerance). This is an overloaded variant of the older findPathAStarWithLimits.
     */
    private static java.util.List<BlockPos> findPathAStarWithLimits(Level level, BlockPos start, BlockPos end, float mobWidth, float mobHeight, int maxNodes, int maxDown, int maxUp, int endRadius) {
        class PathNode {
            final BlockPos pos;
            final double g; // cost from start
            final double f; // g + heuristic
            final PathNode parent;
            PathNode(BlockPos pos, double g, double f, PathNode parent) { this.pos = pos; this.g = g; this.f = f; this.parent = parent; }
        }

        BlockPos fixedStart = start == null ? null : start.immutable();
        BlockPos fixedEnd = end == null ? null : end.immutable();
        if (fixedStart == null || fixedEnd == null) return java.util.Collections.emptyList();

        if (level.getBlockState(fixedStart).is(Blocks.WATER)) {
            BlockPos alt = findNearbyStandable(level, start, mobWidth, mobHeight, 5);
            if (alt == null || level.getBlockState(alt).is(Blocks.WATER)) return java.util.Collections.emptyList();
            fixedStart = alt.immutable();
        }

        java.util.PriorityQueue<PathNode> open = new java.util.PriorityQueue<>(java.util.Comparator.comparingDouble(n -> n.f));
        java.util.Map<BlockPos, Double> gScore = new java.util.HashMap<>();
        java.util.Set<BlockPos> closed = new java.util.HashSet<>();

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
                java.util.LinkedList<BlockPos> path = new java.util.LinkedList<>();
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

        return java.util.Collections.emptyList();
    }

    /**
     * Backwards-compatible A* implementation that requires reaching the exact end (endRadius=0).
     */
    private static java.util.List<BlockPos> findPathAStarWithLimits(Level level, BlockPos start, BlockPos end, float mobWidth, float mobHeight, int maxNodes, int maxDown) {
        // Delegate to the full-parameter implementation with default maxUp=1 and endRadius=0 (exact target).
        return findPathAStarWithLimits(level, start, end, mobWidth, mobHeight, maxNodes, maxDown, 1, 0);
    }

    /**
     * Search for a nearby standable position within the given horizontal radius and -1..1 vertical offset.
     * Returns the closest (by manhattan distance) standable BlockPos or null if none found.
     */
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
        // Use Euclidean distance as heuristic (admissible for diagonal movement)
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

    /**
     * Returns true if an entity with given width/height can stand at the given block position (feet at pos.getY()).
     * It checks that the space for the entity is free and that there's a solid block below its feet (so it won't fall).
     */
    private static boolean canStandAt(Level level, BlockPos pos, float mobWidth, float mobHeight) {
        // Build the entity AABB for the candidate standing position (feet at pos.getY()).
        double cx = pos.getX() + 0.5;
        double cz = pos.getZ() + 0.5;
        double bottomY = pos.getY();
        double topY = bottomY + mobHeight;
        double halfWidth = mobWidth / 2.0;

        AABB box = new AABB(cx - halfWidth, bottomY, cz - halfWidth, cx + halfWidth, topY, cz + halfWidth);

        // The entity space must be free (so we don't stand inside a solid block). This allows standing in
        // non-colliding blocks such as grass or flowers because their collision shapes are empty.
        if (!isAABBFree(level, box)) return false;

        // There must be a solid supporting block directly below the feet position.
        return hasSolidBlockBelow(level, pos);
    }

    /**
     * Returns true if the given AABB does not intersect any block collision shapes.
     */
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

    /**
     * Returns true if the given AABB does not intersect any block collision shapes.
     * Strict version for spawn position validation: any non-empty collision shape blocks spawning.
     */
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
     * Returns true if there is a solid (collidable) block directly below the given center position.
     */
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

   public static BlockPos findStartPositionForPathAStar(Level level, BlockPos end, int distance) {
        // default settings: do not allow end in water, mob size standard, large node limit, maxDown=4, maxUp=1
        return findStartPositionForPathAStar(level, end, distance, false, 0.6f, 1.8f, 10000, 4, 1, 0);
    }

    public static BlockPos findStartPositionForPathAStar(Level level, BlockPos start, int distance, boolean canEndInWater, float mobWidth, float mobHeight, int maxNodes, int maxDown, int maxUp) {
        // default radius 0 for exact target behavior
        return findStartPositionForPathAStar(level, start, distance, canEndInWater, mobWidth, mobHeight, maxNodes, maxDown, maxUp, 10);
    }

    public static BlockPos findStartPositionForPathAStar(Level level, BlockPos start, int distance, boolean canEndInWater, float mobWidth, float mobHeight, int maxNodes, int maxDown, int maxUp, int radius) {
        if (start == null || level == null) return null;

        final int[][] dirs = new int[][]{{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
        final int minY = level.getMinBuildHeight();
        final int maxY = level.getMaxBuildHeight();
        final BlockPos startPos = start.immutable();

        class Node { final BlockPos pos; final int depth; final double score; Node(BlockPos p, int d, double s) { pos = p; depth = d; score = s; } }

        java.util.PriorityQueue<Node> open = new java.util.PriorityQueue<>(java.util.Comparator.comparingDouble(n -> n.score));
        java.util.Map<BlockPos, Integer> bestDepth = new java.util.HashMap<>();

        // initial node: prefer nodes that are already far (score negative horizontal distance)
        double startScore = -0.0 + 0.0;
        open.add(new Node(startPos, 0, startScore));
        bestDepth.put(startPos, 0);

        int expanded = 0;

        while (!open.isEmpty() && expanded < Math.max(1, maxNodes)) {
            Node cur = open.poll();
            if (cur == null) break;

            // If we've reached the exact target path length, check radius and end-water rules
            if (cur.depth == distance) {
                double dx = (double)cur.pos.getX() - (double)startPos.getX();
                double dz = (double)cur.pos.getZ() - (double)startPos.getZ();
                double horizDist = Math.sqrt(dx*dx + dz*dz);
                if (horizDist > (double)radius) {
                    BlockState endState = level.getBlockState(cur.pos);
                    boolean isWater = endState.is(Blocks.WATER) || endState.getFluidState().is(FluidTags.WATER);
                    if (isWater) {
                        if (canEndInWater) return cur.pos;
                    } else {
                        return cur.pos;
                    }
                }
                // do not expand further
                continue;
            }

            // If we already have a better (smaller) depth recorded for this pos, skip
            Integer recorded = bestDepth.get(cur.pos);
            if (recorded != null && cur.depth > recorded) continue;

            expanded++;

            for (int[] d : dirs) {
                int nx = cur.pos.getX() + d[0];
                int nz = cur.pos.getZ() + d[1];

                int scanTop = Math.min(cur.pos.getY() + maxUp, maxY);
                int scanBottom = Math.max(cur.pos.getY() - maxDown, minY);

                for (int ny = scanTop; ny >= scanBottom; ny--) {
                    int vertDiff = ny - cur.pos.getY();
                    if (vertDiff > maxUp || vertDiff < -maxDown) continue;

                    BlockPos neighbor = new BlockPos(nx, ny, nz);
                    int nextDepth = cur.depth + 1;

                    // reject if we've seen a better depth for this neighbor
                    Integer nbRecorded = bestDepth.get(neighbor);
                    if (nbRecorded != null && nextDepth >= nbRecorded) continue;

                    BlockState neighborState = level.getBlockState(neighbor);
                    boolean neighborIsWater = neighborState.is(Blocks.WATER) || neighborState.getFluidState().is(FluidTags.WATER);
                    if (nextDepth < distance && neighborIsWater) continue; // avoid water in intermediate steps

                    if (!canStandAt(level, neighbor, mobWidth, mobHeight)) continue;

                    // compute score: prefer larger horizontal distance from start (so negative distance -> smaller score)
                    double dx = (double)neighbor.getX() - (double)startPos.getX();
                    double dz = (double)neighbor.getZ() - (double)startPos.getZ();
                    double horizDist = Math.sqrt(dx*dx + dz*dz);
                    double score = -horizDist + (nextDepth * 0.001); // tie-break by smaller depth

                    bestDepth.put(neighbor, nextDepth);
                    open.add(new Node(neighbor, nextDepth, score));

                    // stop vertical scanning when we found a standable block
                    if (canStandAt(level, neighbor, mobWidth, mobHeight)) break;
                }
            }
        }

        return null;
    }

    /**
     * Find a spawnable position within `radius` (blocks) of `center` using default mob size, no water, include caves and randomize.
     * Returns a BlockPos (feet position) or null if none found.
     */
    public static BlockPos findSpawnablePosition(Level level, BlockPos center, int radius) {
        return findSpawnablePosition(level, center, radius, false, 0.6f, 1.8f, true, true);
    }

    /**
     * Backwards-compatible overload that includes caves and random selection by default.
     */
    public static BlockPos findSpawnablePosition(Level level, BlockPos center, int radius, boolean allowWater, float mobWidth, float mobHeight) {
        return findSpawnablePosition(level, center, radius, allowWater, mobWidth, mobHeight, true, true);
    }

    /**
     * Full implementation: search for a spawnable position within `radius` of `center`.
     * - includeCaves: if true, scan a limited vertical column below the surface (fast) to find cave positions.
     * - randomize: if true, pick a random candidate among valid positions; otherwise prefer nearest positions and return early.
     *
     * This simplified variant reduces vertical scanning and early-exits whenever possible for performance.
     */
    public static BlockPos findSpawnablePosition(Level level, BlockPos center, int radius, boolean allowWater, float mobWidth, float mobHeight, boolean includeCaves, boolean randomize) {
        if (level == null || center == null || radius < 0) return null;

        int cx = center.getX();
        int cz = center.getZ();
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight();

        // Prepare XZ candidates inside the circle (squared distance) - allocate once
        java.util.List<int[]> xzList = new java.util.ArrayList<>();
        int r2 = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int dsq = dx*dx + dz*dz;
                if (dsq <= r2) xzList.add(new int[]{cx + dx, cz + dz, dsq});
            }
        }

        // Ensure the exact center is included so the start position itself can be chosen sometimes
        xzList.add(new int[]{cx, cz, 0});

        // Use a short-lived Random seeded with System.nanoTime() to avoid repeated identical shuffles
        java.util.Random shuffler = new java.util.Random(System.nanoTime());
        if (randomize) {
            java.util.Collections.shuffle(xzList, shuffler);
        } else {
            xzList.sort(java.util.Comparator.comparingInt(a -> a[2]));
        }

        // ThreadLocalRandom for final selection
        java.util.concurrent.ThreadLocalRandom trnd = java.util.concurrent.ThreadLocalRandom.current();

        // small candidates list to avoid large memory usage
        java.util.List<BlockPos> candidates = new java.util.ArrayList<>();
        final int MAX_CANDIDATES = 512;

        // Heightmap cache: map keyed by (x,z) packed into a long to avoid repeated level.getHeight calls
        java.util.Map<Long, Integer> heightCacheBlocking = new java.util.HashMap<>(xzList.size());
        java.util.Map<Long, Integer> heightCacheNoLeaves = new java.util.HashMap<>(xzList.size());
        java.util.function.BiFunction<Integer,Integer,Integer> getSurfaceY = (xx, zz) -> {
            long key = (((long)xx) << 32) ^ (zz & 0xffffffffL);
            return heightCacheBlocking.computeIfAbsent(key, k -> level.getHeight(Heightmap.Types.MOTION_BLOCKING, xx, zz));
        };
        java.util.function.BiFunction<Integer,Integer,Integer> getSurfaceYNoLeaves = (xx, zz) -> {
            long key = (((long)xx) << 32) ^ (zz & 0xffffffffL);
            return heightCacheNoLeaves.computeIfAbsent(key, k -> level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, xx, zz));
        };

        for (int[] c : xzList) {
            int x = c[0];
            int z = c[1];

            if (!includeCaves) {
                // Surface only: use heightmap (fast) and check a tiny vertical neighborhood
                int surfaceY = getSurfaceY.apply(x, z);
                if (surfaceY < minY || surfaceY > maxY) continue;

                BlockPos pos = new BlockPos(x, surfaceY, z);
                BlockState state = level.getBlockState(pos);
                // if the surface block itself is solid (collision), spawn on the block above
                if (!state.getCollisionShape(level, pos).isEmpty()) pos = pos.above();

                if (isValidSpawnPos(level, pos, mobWidth, mobHeight, allowWater)) {
                    if (!randomize) return pos; // prefer nearest immediately when not randomized
                    candidates.add(pos);
                }
            } else {
                // Include caves: start scan around the provided center Y (so deep centers are found)
                int surfaceY = getSurfaceY.apply(x, z);
                // Prefer starting at the requested center Y (clamped to world bounds)
                int requestedY = Math.min(Math.max(center.getY(), minY), maxY - 1);
                int startY = requestedY;

                // Compute a scan depth that ensures we search sufficiently downward when center is deep.
                // Base depth at least 32, but expand if the surface is far above the requestedY.
                int scanDepth = Math.max(32, Math.abs(surfaceY - startY) + 32);
                scanDepth = Math.min(scanDepth, Math.max(32, maxY - minY)); // cap to world height range

                boolean foundInColumn = false;

                // Build a Y-list that scans down from startY into the cave (and a tiny check above).
                int minScanY = Math.max(minY, startY - scanDepth + 1);
                java.util.List<Integer> ys = new java.util.ArrayList<>(Math.max(1, startY - minScanY + 1));
                for (int y = startY; y >= minScanY; y--) ys.add(y);
                // Also check one block above startY (handles thin ceilings)
                if (startY + 1 <= maxY - 1) ys.add(startY + 1);
                if (randomize) java.util.Collections.shuffle(ys, shuffler);

                for (int y : ys) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    boolean isWater = state.is(Blocks.WATER) || state.getFluidState().is(FluidTags.WATER);
                    if (!allowWater && isWater) continue;

                    // If feet block itself is inside a solid block and not water, skip quickly
                    if (!state.getCollisionShape(level, pos).isEmpty() && !isWater) continue;

                    if (isValidSpawnPos(level, pos, mobWidth, mobHeight, allowWater)) {
                        if (!randomize) return pos; // immediate return when ordered search
                        candidates.add(pos);
                        foundInColumn = true;
                        break; // don't search deeper once we found a candidate in this column
                    }
                }

                // optional small check above surface if we haven't found anything (handles leaf-covered surfaces)
                if (!foundInColumn && startY + 1 <= maxY) {
                    BlockPos pos = new BlockPos(x, startY + 1, z);
                    if (isValidSpawnPos(level, pos, mobWidth, mobHeight, allowWater)) {
                        if (!randomize) return pos;
                        candidates.add(pos);
                    }
                }
            }

            if (candidates.size() >= MAX_CANDIDATES) break;
        }

        if (candidates.isEmpty()) return null;
        // If randomized, shuffle candidates with the nano-time shuffler and return the first one.
        if (randomize) {
            java.util.Collections.shuffle(candidates, shuffler);
            // debug log for problematic coordinate to inspect candidate count
            BlockPos query = new BlockPos(cx, center.getY(), cz);
            if (query.getX() == 917 && query.getY() == -49 && query.getZ() == 211) {
                UndeadNights.LOGGER.info("findSpawnablePosition debug: center={} radius={} candidates={}", query, radius, candidates.size());
            }
            return candidates.get(0);
        }

        // nearest preference: candidates were collected in increasing distance order if not randomized
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos p : candidates) {
            double dx = p.getX() - cx;
            double dz = p.getZ() - cz;
            double d2 = dx*dx + dz*dz;
            if (d2 < bestDist) { bestDist = d2; best = p; }
        }
        return best;
    }

    /**
     * Return true if the given BlockPos is a valid spawn/stand position for an entity with the given size.
     * - bottomY = pos.getY() (feet)
     * - requires the entity AABB to be free and a solid supporting block below
     * - if allowWater==false, water cells inside the entity volume are rejected
     *
     * This simplified version relies on the stricter AABB test and a targeted water check for performance.
     */
    private static boolean isValidSpawnPos(Level level, BlockPos pos, float mobWidth, float mobHeight, boolean allowWater) {
        if (level == null || pos == null) return false;
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight();
        if (pos.getY() < minY || pos.getY() >= maxY) return false;

        BlockState feetState = level.getBlockState(pos);
        boolean feetIsWater = feetState.is(Blocks.WATER) || feetState.getFluidState().is(FluidTags.WATER);
        if (feetIsWater && !allowWater) return false;

        // feet block itself must not be a blocking collision (otherwise we'd spawn inside a block)
        VoxelShape feetShape = feetState.getCollisionShape(level, pos);
        if (!feetShape.isEmpty() && !feetIsWater) return false;

        double cx = pos.getX() + 0.5;
        double cz = pos.getZ() + 0.5;
        double bottomY = pos.getY();
        double topY = bottomY + mobHeight;
        double halfWidth = mobWidth / 2.0;

        // small epsilon to avoid integer-boundary edge cases
        final double eps = 1e-3;
        AABB box = new AABB(cx - halfWidth + eps, bottomY + eps, cz - halfWidth + eps, cx + halfWidth - eps, topY - eps, cz + halfWidth - eps);

        // strict AABB collision test for spawn (iterates necessary blocks internally)
        if (!isAABBFreeForSpawn(level, box)) return false;

        // If water isn't allowed, do a small vertical check in the column covered by the AABB to detect fluids.
        if (!allowWater) {
            int bottomBlock = (int) Math.floor(box.minY);
            int topBlock = (int) Math.floor(box.maxY);
            for (int by = bottomBlock; by <= topBlock; by++) {
                BlockState s = level.getBlockState(new BlockPos(pos.getX(), by, pos.getZ()));
                if (s.is(Blocks.WATER) || s.getFluidState().is(FluidTags.WATER)) return false;
            }
        }

        // require a supporting block below
        if (!hasSolidBlockBelow(level, pos)) return false;

        return true;
    }
}
