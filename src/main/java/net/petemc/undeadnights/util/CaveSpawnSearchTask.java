package net.petemc.undeadnights.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import net.petemc.undeadnights.entity.ModEntities;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Spreads the cave spawn position search (pathfinding) across multiple server ticks
 * so that the server thread is never blocked for more than a few milliseconds per tick.
 *
 * <h4>Usage</h4>
 * <pre>
 *   // Create once per player/horde-event:
 *   CaveSpawnSearchTask task = new CaveSpawnSearchTask(level, player, distance, allowWater);
 *
 *   // On every server tick:
 *   switch (task.tick()) {
 *       case FOUND   -> spawnHordeAt(task.getResult());
 *       case FAILED  -> handleFailure();
 *       case SEARCHING -> { /* still in progress, call again next tick *\/ }
 *   }
 * </pre>
 *
 * <h4>Why player movement doesn't matter</h4>
 * The search takes at most {@value MAX_ATTEMPTS} / {@value ATTEMPTS_PER_TICK} ticks
 * (~0.5 s at 20 TPS).  During that time the player moves ≤ 10 blocks, which does not
 * invalidate cave connectivity.  After spawning, the zombie uses {@code setTarget(player)}
 * and navigates to the player's <em>current</em> position dynamically.
 */
public class CaveSpawnSearchTask {

    public enum State { SEARCHING, FOUND, FAILED }

    /** Pathfinding attempts per tick – keeps per-tick cost ≤ ~40 ms. */
    private static final int ATTEMPTS_PER_TICK = 8;
    /**
     * Maximum total attempts before giving up.
     * 320 attempts = 40 ticks ≈ 2 s at 20 TPS.
     */
    private static final int MAX_ATTEMPTS = 320;
    /** Vertical search window around the player's Y level. */
    private static final int MAX_Y_DELTA = 12;

    private final Level level;
    private final ServerPlayer player;
    private final int distance;
    private final boolean allowEndInWater;
    private final boolean debug;

    /**
     * Player position captured at task-creation.  Used as the centre of the ring-sampling
     * so that the candidate distance from the player stays consistent even as the player moves.
     */
    private final BlockPos startPos;

    private int attemptsDone = 0;

    /** Reusable probe entity – never added to the world, only used for bounding-box / pathfinding. */
    private final HordeZombieEntity probe;
    private final ThreadLocalRandom rnd = ThreadLocalRandom.current();

    private State state = State.SEARCHING;
    private BlockPos result = null;

    /**
     * Best partial-path candidate seen so far.
     * Zombies can break blocks to close the remaining gap, so we accept partial paths
     * where A* covered at least (1 - PARTIAL_ACCEPT_THRESHOLD) of the route.
     * With threshold 0.7 → A* only needs to cover 30 % of the distance because the
     * zombie will break through the rest.  Completely disconnected caves (e.g. separate
     * cave systems behind a mountain) still leave distToTarget near the full search
     * distance and are therefore rejected.
     */
    private BlockPos bestPartialCandidate = null;
    private double  bestPartialDistLeft  = Double.MAX_VALUE;
    private static final double PARTIAL_ACCEPT_THRESHOLD = 0.7;

    public CaveSpawnSearchTask(Level level, ServerPlayer player, int distance, boolean allowEndInWater) {
        this.level = level;
        this.player = player;
        this.distance = distance;
        this.allowEndInWater = allowEndInWater;
        this.debug = MainConfig.getPrintDebugMessages();
        this.startPos = player.blockPosition();

        probe = new HordeZombieEntity(ModEntities.HORDE_ZOMBIE.get(), level);
        probe.setOnGround(true);
        // Raise follow-range so the pathfinder is never limited by tracking distance.
        var followRange = probe.getAttribute(Attributes.FOLLOW_RANGE);
        if (followRange != null) followRange.setBaseValue(256.0);
    }

    /**
     * Runs one batch of up to {@value ATTEMPTS_PER_TICK} pathfinding attempts.
     * Must be called once per server tick until the returned state is no longer
     * {@link State#SEARCHING}.
     *
     * @return current task state
     */
    public State tick() {
        if (state != State.SEARCHING) return state;

        for (int i = 0; i < ATTEMPTS_PER_TICK && attemptsDone < MAX_ATTEMPTS; i++, attemptsDone++) {

            // ── Circular ring sampling ──────────────────────────────────────────────
            // Every candidate is placed exactly ~distance blocks from the start position,
            // so no attempt is ever wasted on wrong-distance positions.
            double angle = rnd.nextDouble() * 2.0 * Math.PI;
            int jitter = rnd.nextInt(-3, 4);
            int r = Math.max(1, distance + jitter);
            int dx = (int) Math.round(Math.cos(angle) * r);
            int dz = (int) Math.round(Math.sin(angle) * r);
            int cx = startPos.getX() + dx;
            int cz = startPos.getZ() + dz;

            // ── Geometric pre-filter: find the best walkable Y in this column ───────
            BlockPos candidate = SpawnLocationFinder.findBestCaveY(
                    level, cx, startPos.getY(), cz, MAX_Y_DELTA, allowEndInWater);
            if (candidate == null) continue;

            try {
                // ── Pathfinding check (vanilla A*) ───────────────────────────────
                // Navigate FROM the candidate TO the player; confirms cave connectivity.
                // The player's CURRENT position is used here – so even if they walked
                // a few blocks since the task was created, the path check is accurate.
                probe.setPos(candidate.getX() + 0.5, candidate.getY(), candidate.getZ() + 0.5);
                GroundPathNavigationLegacy nav = new GroundPathNavigationLegacy(probe, level);
                Path path = nav.createPathLegacy(player, 0);

                if (path == null) continue; // start node could not be evaluated

                // ── Phase 1: complete path (guaranteed reachability) ───────────────
                if (path.canReach()) {
                    double minSafeDistSq = (distance * 0.5) * (distance * 0.5);
                    if (player.blockPosition().distSqr(candidate) < minSafeDistSq) {
                        if (debug) UndeadNights.LOGGER.info(
                                "CaveSpawnSearch: candidate {} discarded – player moved too close (attempt {})",
                                candidate, attemptsDone);
                        continue;
                    }
                    result = candidate;
                    state = State.FOUND;
                    if (debug) UndeadNights.LOGGER.info(
                            "CaveSpawnSearch: FOUND {} (complete path, attempt {})", candidate, attemptsDone);
                    return state;
                }

                // ── Phase 2: partial path – accept only if A* got close enough ──────
                // distToTarget < distance*0.4  →  A* covered >60% of the route,
                // very likely connected. Truly disconnected caves leave distToTarget
                // near the full search distance.
                double distLeft = path.getDistToTarget();
                double acceptThreshold = distance * PARTIAL_ACCEPT_THRESHOLD;
                if (distLeft < acceptThreshold && distLeft < bestPartialDistLeft) {
                    bestPartialCandidate = candidate;
                    bestPartialDistLeft  = distLeft;
                }
                if (debug) UndeadNights.LOGGER.info(
                        "CaveSpawnSearch: partial path at {} distLeft={} (attempt {}, threshold={})",
                        candidate, String.format("%.1f", distLeft), attemptsDone,
                        String.format("%.1f", acceptThreshold));

            } catch (Throwable t) {
                UndeadNights.LOGGER.warn(
                        "CaveSpawnSearch: navigation threw for {}: {}", candidate, t.toString());
            }
        }

        // All attempts exhausted → decide on final result
        if (attemptsDone >= MAX_ATTEMPTS) {
            concludeSearch();
        }
        return state;
    }

    /**
     * Called once all attempts are done.
     * Prefers positions with a complete A* path ({@link Path#canReach()}).
     * Falls back to the best partial-path candidate if A* covered >60 % of the
     * route (distToTarget &lt; distance * {@value PARTIAL_ACCEPT_THRESHOLD}).
     * Truly disconnected caves leave distToTarget near the full search distance
     * and are therefore rejected.
     */
    private void concludeSearch() {
        if (bestPartialCandidate != null) {
            double minSafeDistSq = (distance * 0.5) * (distance * 0.5);
            if (player.blockPosition().distSqr(bestPartialCandidate) >= minSafeDistSq) {
                result = bestPartialCandidate;
                state  = State.FOUND;
                if (debug) UndeadNights.LOGGER.info(
                        "CaveSpawnSearch: FOUND {} via partial-path fallback (distLeft={}, attempt {})",
                        result, String.format("%.1f", bestPartialDistLeft), attemptsDone);
                return;
            }
        }
        state = State.FAILED;
        if (debug) UndeadNights.LOGGER.info(
                "CaveSpawnSearch: no reachable candidate found after {} attempts (distance={})",
                attemptsDone, distance);
    }

    // ── Accessors ────────────────────────────────────────────────────────────────

    public State    getState()        { return state; }
    /** Valid only when {@link #getState()} == {@link State#FOUND}. */
    public BlockPos getResult()       { return result; }
    public int      getAttemptsDone() { return attemptsDone; }
}

