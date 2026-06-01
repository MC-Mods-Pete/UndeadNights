package net.petemc.undeadnights.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.Vec3;

// Legacy ground path navigation that adjusts for air/solid blocks when creating paths
// only used in specific cases (e.g. cave spawning) to maintain old behavior

public class GroundPathNavigationLegacy extends PathNavigation {
    private boolean avoidSun;

    /**
     * 8× the vanilla node budget so winding cave routes (which can be 3–5× the
     * straight-line distance) are fully explored by the A* search.
     * This class is only used for the one-shot cave spawn-location check, not for
     * live zombie navigation, so the extra cost per call is acceptable.
     */
    private static final int NODE_BUDGET_MULTIPLIER = 8;

    public GroundPathNavigationLegacy(Mob mob, Level level) {
        super(mob, level);
    }

    protected PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new WalkNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);
        return new PathFinder(this.nodeEvaluator, maxVisitedNodes * NODE_BUDGET_MULTIPLIER);
    }

    protected boolean canUpdatePath() {
        return this.mob.onGround() || this.isInLiquid() || this.mob.isPassenger();
    }

    protected Vec3 getTempMobPos() {
        return new Vec3(this.mob.getX(), (double)this.getSurfaceY(), this.mob.getZ());
    }

    // Legacy path creation that adjusts for air/solid blocks
    public Path createPathLegacy(BlockPos pPos, int pAccuracy) {
        if (this.level.getBlockState(pPos).isAir()) {
            BlockPos blockpos;
            for(blockpos = pPos.below(); blockpos.getY() > this.level.getMinBuildHeight() && this.level.getBlockState(blockpos).isAir(); blockpos = blockpos.below()) {
            }

            if (blockpos.getY() > this.level.getMinBuildHeight()) {
                return super.createPath(blockpos.above(), pAccuracy);
            }

            while(blockpos.getY() < this.level.getMaxBuildHeight() && this.level.getBlockState(blockpos).isAir()) {
                blockpos = blockpos.above();
            }

            pPos = blockpos;
        }

        if (!this.level.getBlockState(pPos).isSolid()) {
            return super.createPath(pPos, pAccuracy);
        } else {
            BlockPos blockpos1;
            for(blockpos1 = pPos.above(); blockpos1.getY() < this.level.getMaxBuildHeight() && this.level.getBlockState(blockpos1).isSolid(); blockpos1 = blockpos1.above()) {
            }

            return super.createPath(blockpos1, pAccuracy);
        }
    }

    // Legacy path creation that adjusts for air/solid blocks
    public Path createPathLegacy(Entity pEntity, int pAccuracy) {
        return this.createPathLegacy(pEntity.blockPosition(), pAccuracy);
    }

    public Path createPath(BlockPos pos, int accuracy) {
        LevelChunk levelchunk = this.level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
        if (levelchunk == null) {
            return null;
        } else {
            if (levelchunk.getBlockState(pos).isAir()) {
                BlockPos blockpos;
                for(blockpos = pos.below(); blockpos.getY() > this.level.getMinBuildHeight() && levelchunk.getBlockState(blockpos).isAir(); blockpos = blockpos.below()) {
                }

                if (blockpos.getY() > this.level.getMinBuildHeight()) {
                    return super.createPath(blockpos.above(), accuracy);
                }

                while(blockpos.getY() < this.level.getMaxBuildHeight() && levelchunk.getBlockState(blockpos).isAir()) {
                    blockpos = blockpos.above();
                }

                pos = blockpos;
            }

            if (!levelchunk.getBlockState(pos).isSolid()) {
                return super.createPath(pos, accuracy);
            } else {
                BlockPos blockpos1;
                for(blockpos1 = pos.above(); blockpos1.getY() < this.level.getMaxBuildHeight() && levelchunk.getBlockState(blockpos1).isSolid(); blockpos1 = blockpos1.above()) {
                }

                return super.createPath(blockpos1, accuracy);
            }
        }
    }

    public Path createPath(Entity entity, int accuracy) {
        return this.createPath(entity.blockPosition(), accuracy);
    }

    private int getSurfaceY() {
        if (this.mob.isInWater() && this.canFloat()) {
            int i = this.mob.getBlockY();
            BlockState blockstate = this.level.getBlockState(BlockPos.containing(this.mob.getX(), (double)i, this.mob.getZ()));
            int j = 0;

            while(blockstate.is(Blocks.WATER)) {
                Level var10000 = this.level;
                double var10001 = this.mob.getX();
                ++i;
                blockstate = var10000.getBlockState(BlockPos.containing(var10001, (double)i, this.mob.getZ()));
                ++j;
                if (j > 16) {
                    return this.mob.getBlockY();
                }
            }

            return i;
        } else {
            return Mth.floor(this.mob.getY() + (double)0.5F);
        }
    }

    protected void trimPath() {
        super.trimPath();
        if (this.avoidSun) {
            if (this.level.canSeeSky(BlockPos.containing(this.mob.getX(), this.mob.getY() + (double)0.5F, this.mob.getZ()))) {
                return;
            }

            for(int i = 0; i < this.path.getNodeCount(); ++i) {
                Node node = this.path.getNode(i);
                if (this.level.canSeeSky(new BlockPos(node.x, node.y, node.z))) {
                    this.path.truncateNodes(i);
                    return;
                }
            }
        }

    }

    protected boolean hasValidPathType(BlockPathTypes pathType) {
        if (pathType == BlockPathTypes.WATER) {
            return false;
        } else {
            return pathType == BlockPathTypes.LAVA ? false : pathType != BlockPathTypes.OPEN;
        }
    }

    public void setCanOpenDoors(boolean canOpenDoors) {
        this.nodeEvaluator.setCanOpenDoors(canOpenDoors);
    }

    public boolean canPassDoors() {
        return this.nodeEvaluator.canPassDoors();
    }

    public void setCanPassDoors(boolean canPassDoors) {
        this.nodeEvaluator.setCanPassDoors(canPassDoors);
    }

    public boolean canOpenDoors() {
        return this.nodeEvaluator.canPassDoors();
    }

    public void setAvoidSun(boolean avoidSun) {
        this.avoidSun = avoidSun;
    }

    public void setCanWalkOverFences(boolean canWalkOverFences) {
        this.nodeEvaluator.setCanWalkOverFences(canWalkOverFences);
    }
}
