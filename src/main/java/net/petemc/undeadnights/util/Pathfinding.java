package net.petemc.undeadnights.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

/**
 * Lightweight wrapper helpers for pathfinding-related convenience calls.
 * This class delegates to the existing Helpers implementation and provides
 * sensible defaults for common use-cases.
 */
public final class Pathfinding {
    private Pathfinding() {}

    // Mutable defaults (can be changed at runtime via setters)
    private static float mobWidth = 0.6f;
    private static float mobHeight = 1.8f;
    private static int maxNodes = 10000;
    private static int maxDown = 4;
    private static int maxUp = 1;
    private static int endRadius = 10; // default radius (10) to treat success when near target

    // Getters and setters (formatted consistently)
    public static float getMobWidth() { return mobWidth; }
    public static void setMobWidth(float value) { mobWidth = value; }

    public static float getMobHeight() { return mobHeight; }
    public static void setMobHeight(float value) { mobHeight = value; }

    public static int getMaxNodes() { return maxNodes; }
    public static void setMaxNodes(int value) { maxNodes = value; }

    public static int getMaxDown() { return maxDown; }
    public static void setMaxDown(int value) { maxDown = value; }

    public static int getMaxUp() { return maxUp; }
    public static void setMaxUp(int value) { maxUp = value; }

    public static int getEndRadius() { return endRadius; }
    public static void setEndRadius(int value) { endRadius = value; }

    /**
     * Asynchronously find a suitable start position for a path to the given end.
     * Uses the configured defaults for mob size, maxNodes, maxDown/maxUp and end radius.
     * The computation runs on the common ForkJoinPool and returns a CompletableFuture
     * that completes with the found BlockPos or null if none was found.
     */
    public static CompletableFuture<BlockPos> findEndPositionAStarAsync(Level level, BlockPos end, int distance) {
        return CompletableFuture.supplyAsync(() -> Helpers.findEndPositionForPathAStar(
                level,
                end,
                distance,
                false,
                getMobWidth(),
                getMobHeight(),
                getMaxNodes(),
                getMaxDown(),
                getMaxUp()
        ), ForkJoinPool.commonPool());
    }

    /**
     * Find a path using A* with current default mob size and search limits.
     * Uses the configured maxDown, maxUp and endRadius defaults.
     * Returns an immutable list of BlockPos (start..end) or an empty list when no path found.
     */
    public static List<BlockPos> findPath(Level level, BlockPos start, BlockPos end) {
        return Helpers.findPathAStar(level, start, end, getMobWidth(), getMobHeight(), getMaxNodes(), getMaxDown(), getMaxUp(), getEndRadius());
    }

    /**
     * Find a path with custom mob size and maxNodes, using configured vertical limits.
     */
    public static List<BlockPos> findPath(Level level, BlockPos start, BlockPos end, float mobWidth, float mobHeight, int maxNodes) {
        return Helpers.findPathAStar(level, start, end, mobWidth, mobHeight, maxNodes, getMaxDown(), getMaxUp(), getEndRadius());
    }

    /**
     * Find a path with custom mob size and custom maxDown/maxUp.
     */
    public static List<BlockPos> findPath(Level level, BlockPos start, BlockPos end, float mobWidth, float mobHeight, int maxNodes, int maxDown, int maxUp) {
        return Helpers.findPathAStar(level, start, end, mobWidth, mobHeight, maxNodes, maxDown, maxUp, getEndRadius());
    }

    /**
     * Find a path with full custom controls including endRadius.
     */
    public static List<BlockPos> findPath(Level level, BlockPos start, BlockPos end, float mobWidth, float mobHeight, int maxNodes, int maxDown, int maxUp, int endRadius) {
        return Helpers.findPathAStar(level, start, end, mobWidth, mobHeight, maxNodes, maxDown, maxUp, endRadius);
    }

    /**
     * Check whether a path exists (boolean) using current configured defaults.
     */
    public static boolean canPathfind(Level level, BlockPos start, BlockPos end) {
        return Helpers.canMobPathfindAStar(level, start, end, getMobWidth(), getMobHeight(), getMaxNodes(), getMaxDown(), getMaxUp(), getEndRadius());
    }

    /**
     * Check whether a path exists with custom parameters (including maxDown, maxUp and endRadius).
     */
    public static boolean canPathfind(Level level, BlockPos start, BlockPos end, float mobWidth, float mobHeight, int maxNodes, int maxDown, int maxUp, int endRadius) {
        return Helpers.canMobPathfindAStar(level, start, end, mobWidth, mobHeight, maxNodes, maxDown, maxUp, endRadius);
    }
}
