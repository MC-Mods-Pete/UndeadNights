package net.petemc.undeadnights.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

// Legacy ground path navigation that adjusts for air/solid blocks when creating paths
// only used in specific cases (e.g. cave spawning) to maintain old behavior

public class GroundPathNavigationLegacy extends EntityNavigation {
    private boolean avoidSunlight;

    public GroundPathNavigationLegacy(MobEntity mobEntity, World world) {
        super(mobEntity, world);
    }

    protected PathNodeNavigator createPathNodeNavigator(int range) {
        this.nodeMaker = new LandPathNodeMaker();
        this.nodeMaker.setCanEnterOpenDoors(true);
        return new PathNodeNavigator(this.nodeMaker, range);
    }

    protected boolean isAtValidPosition() {
        return this.entity.isOnGround() || this.entity.isInFluid() || this.entity.hasVehicle();
    }

    protected Vec3d getPos() {
        return new Vec3d(this.entity.getX(), (double)this.getPathfindingY(), this.entity.getZ());
    }

    // Legacy path creation that adjusts for air/solid blocks
    public Path createPathLegacy(BlockPos target, int pAccuracy) {
        if (this.world.getBlockState(target).isAir()) {
            BlockPos blockPos;
            for(blockPos = target.down(); blockPos.getY() > this.world.getBottomY() && this.world.getBlockState(blockPos).isAir(); blockPos = blockPos.down()) {
            }

            if (blockPos.getY() > this.world.getBottomY()) {
                return super.findPathTo(blockPos.up(), pAccuracy);
            }

            while(blockPos.getY() < this.world.getTopYInclusive() + 1 && this.world.getBlockState(blockPos).isAir()) {
                blockPos = blockPos.up();
            }

            target = blockPos;
        }

        if (!this.world.getBlockState(target).isSolid()) {
            return super.findPathTo(target, pAccuracy);
        } else {
            BlockPos blockPos;
            for(blockPos = target.up(); blockPos.getY() < this.world.getTopYInclusive() + 1 && this.world.getBlockState(blockPos).isSolid(); blockPos = blockPos.up()) {
            }

            return super.findPathTo(blockPos, pAccuracy);
        }
    }

    // Legacy path creation that adjusts for air/solid blocks
    public Path createPathLegacy(Entity pEntity, int pAccuracy) {
        return this.findPathTo(pEntity.getBlockPos(), pAccuracy);
    }

    public Path findPathTo(BlockPos target, int distance) {
        if (this.world.getBlockState(target).isAir()) {
            BlockPos blockPos;
            for(blockPos = target.down(); blockPos.getY() > this.world.getBottomY() && this.world.getBlockState(blockPos).isAir(); blockPos = blockPos.down()) {
            }

            if (blockPos.getY() > this.world.getBottomY()) {
                return super.findPathTo(blockPos.up(), distance);
            }

            while(blockPos.getY() < this.world.getTopYInclusive() + 1 && this.world.getBlockState(blockPos).isAir()) {
                blockPos = blockPos.up();
            }

            target = blockPos;
        }

        if (!this.world.getBlockState(target).isSolid()) {
            return super.findPathTo(target, distance);
        } else {
            BlockPos blockPos;
            for(blockPos = target.up(); blockPos.getY() < this.world.getTopYInclusive() + 1 && this.world.getBlockState(blockPos).isSolid(); blockPos = blockPos.up()) {
            }

            return super.findPathTo(blockPos, distance);
        }
    }

    public Path findPathTo(Entity entity, int distance) {
        return this.findPathTo(entity.getBlockPos(), distance);
    }

    private int getPathfindingY() {
        if (this.entity.isTouchingWater() && this.canSwim()) {
            int i = this.entity.getBlockY();
            BlockState blockState = this.world.getBlockState(BlockPos.ofFloored(this.entity.getX(), (double)i, this.entity.getZ()));
            int j = 0;

            while(blockState.isOf(Blocks.WATER)) {
                ++i;
                blockState = this.world.getBlockState(BlockPos.ofFloored(this.entity.getX(), (double)i, this.entity.getZ()));
                ++j;
                if (j > 16) {
                    return this.entity.getBlockY();
                }
            }

            return i;
        } else {
            return MathHelper.floor(this.entity.getY() + (double)0.5F);
        }
    }

    protected void adjustPath() {
        super.adjustPath();
        if (this.avoidSunlight) {
            if (this.world.isSkyVisible(BlockPos.ofFloored(this.entity.getX(), this.entity.getY() + (double)0.5F, this.entity.getZ()))) {
                return;
            }

            for(int i = 0; i < this.currentPath.getLength(); ++i) {
                PathNode pathNode = this.currentPath.getNode(i);
                if (this.world.isSkyVisible(new BlockPos(pathNode.x, pathNode.y, pathNode.z))) {
                    this.currentPath.setLength(i);
                    return;
                }
            }
        }

    }

    protected boolean canWalkOnPath(PathNodeType pathType) {
        if (pathType == PathNodeType.WATER) {
            return false;
        } else if (pathType == PathNodeType.LAVA) {
            return false;
        } else {
            return pathType != PathNodeType.OPEN;
        }
    }

    public void setCanPathThroughDoors(boolean canPathThroughDoors) {
        this.nodeMaker.setCanOpenDoors(canPathThroughDoors);
    }

    public boolean canControlOpeningDoors() {
        return this.nodeMaker.canEnterOpenDoors();
    }

    public void setCanEnterOpenDoors(boolean canEnterOpenDoors) {
        this.nodeMaker.setCanEnterOpenDoors(canEnterOpenDoors);
    }

    public boolean canEnterOpenDoors() {
        return this.nodeMaker.canEnterOpenDoors();
    }

    public void setAvoidSunlight(boolean avoidSunlight) {
        this.avoidSunlight = avoidSunlight;
    }

    public void setCanWalkOverFences(boolean canWalkOverFences) {
        this.nodeMaker.setCanWalkOverFences(canWalkOverFences);
    }
}
