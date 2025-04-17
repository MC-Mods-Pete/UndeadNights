package net.petemc.undeadnights.entity.ai.goal;

import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.petemc.undeadnights.UndeadNights;

public class BreakBlockGoalBack extends Goal {
    protected Mob mob;
    protected BlockPos doorPos = BlockPos.ZERO;
    protected boolean hasDoor;
    private static final int DEFAULT_DOOR_BREAK_TIME = 240;
    private final Predicate<Difficulty> validDifficulties;
    protected int breakTime;
    protected int lastBreakProgress = -1;
    protected int doorBreakTime = -1;

    public BreakBlockGoalBack(Mob pMob, Predicate<Difficulty> pValidDifficulties) {
        this.mob = pMob;
        if (!GoalUtils.hasGroundPathNavigation(pMob)) {
            throw new IllegalArgumentException("Unsupported mob type for DoorInteractGoal");
        }
        this.validDifficulties = pValidDifficulties;
    }

    public BreakBlockGoalBack(Mob pMob, int pDoorBreakTime, Predicate<Difficulty> pValidDifficulties) {
        this(pMob, pValidDifficulties);
        this.doorBreakTime = pDoorBreakTime;
    }

    protected int getDoorBreakTime() {
        return Math.max(120, this.doorBreakTime);
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean canUse() {
        if (!this.canUseSuper()) {
            return false;
        } else if (!net.minecraftforge.common.ForgeHooks.canEntityDestroy(this.mob.level(), this.doorPos, this.mob)) {
            UndeadNights.LOGGER.info("-------------------------------> canEntityDestroyFailed");
            return false;
        } else {
            UndeadNights.LOGGER.info("-------------------------------> Running getUse");
            return this.isValidDifficulty(this.mob.level().getDifficulty()) && !this.isOpen();
        }
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean canUseSuper() {
        if (!GoalUtils.hasGroundPathNavigation(this.mob)) {
            return false;
        } else if (!this.mob.horizontalCollision) {
            //UndeadNights.LOGGER.info("-------------------------------> canUseSuper horizontalCollision failed");
            return false;
        } else {
            //UndeadNights.LOGGER.info("-------------------------------> canUseSuper horizontalCollision passed");
            GroundPathNavigation groundpathnavigation = (GroundPathNavigation)this.mob.getNavigation();
            Path path = groundpathnavigation.getPath();
            if (path != null && !path.isDone() && groundpathnavigation.canOpenDoors()) {
                //UndeadNights.LOGGER.info("-------------------------------> canUseSuper if path");
                for(int i = 0; i < Math.min(path.getNextNodeIndex() + 4, path.getNodeCount()); ++i) {
                    Node node = path.getNode(i);
                    this.doorPos = new BlockPos(node.x, node.y + 1, node.z);
                    if (!(this.mob.distanceToSqr((double)this.doorPos.getX(), this.mob.getY(), (double)this.doorPos.getZ()) > 3.25D)) {
                        UndeadNights.LOGGER.info("-------------------------------> canUseSuper sqr {} {}", i, doorPos);
                        this.hasDoor = isWoodenDoor(this.mob.level(), this.doorPos);
                        if (this.hasDoor) {
                            return true;
                        }
                    }
                }

                UndeadNights.LOGGER.info("-------------------------------> canUseSuper doorPos {}", doorPos);
                this.doorPos = this.mob.blockPosition().above();
                this.hasDoor = isWoodenDoor(this.mob.level(), this.doorPos);
                return this.hasDoor;
            } else {
                return false;
            }
        }
    }

    public static boolean isWoodenDoor(Level pLevel, BlockPos pPos) {
        return isWoodenDoor(pLevel.getBlockState(pPos));
    }

    public static boolean isWoodenDoor(BlockState pState) {
        Block block = pState.getBlock();
        UndeadNights.LOGGER.info("-------------------------------> isWoodDoor {}", block);
        if (block instanceof DoorBlock doorblock) {
            UndeadNights.LOGGER.info("-----------------------------------------------------------> isWoodDoor");
            if (doorblock.type().canOpenByHand()) {
                return true;
            }
        }

        return false;
    }

    protected boolean isOpen() {
        if (!this.hasDoor) {
            return false;
        } else {
            BlockState blockstate = this.mob.level().getBlockState(this.doorPos);
            if (!(blockstate.getBlock() instanceof DoorBlock)) {
                this.hasDoor = false;
                return false;
            } else {
                return blockstate.getValue(DoorBlock.OPEN);
            }
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void start() {
        super.start();
        this.breakTime = 0;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean canContinueToUse() {
        return this.breakTime <= this.getDoorBreakTime() && !this.isOpen() && this.doorPos.closerToCenterThan(this.mob.position(), 2.0D) && this.isValidDifficulty(this.mob.level().getDifficulty());
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void stop() {
        super.stop();
        this.mob.level().destroyBlockProgress(this.mob.getId(), this.doorPos, -1);
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void tick() {
        super.tick();
        if (this.mob.getRandom().nextInt(20) == 0) {
            this.mob.level().levelEvent(1019, this.doorPos, 0);
            if (!this.mob.swinging) {
                this.mob.swing(this.mob.getUsedItemHand());
            }
        }

        ++this.breakTime;
        int i = (int)((float)this.breakTime / (float)this.getDoorBreakTime() * 10.0F);
        if (i != this.lastBreakProgress) {
            this.mob.level().destroyBlockProgress(this.mob.getId(), this.doorPos, i);
            this.lastBreakProgress = i;
        }

        if (this.breakTime == this.getDoorBreakTime() && this.isValidDifficulty(this.mob.level().getDifficulty())) {
            this.mob.level().removeBlock(this.doorPos, false);
            this.mob.level().levelEvent(1021, this.doorPos, 0);
            this.mob.level().levelEvent(2001, this.doorPos, Block.getId(this.mob.level().getBlockState(this.doorPos)));
        }

    }

    private boolean isValidDifficulty(Difficulty pDifficulty) {
        return this.validDifficulties.test(pDifficulty);
    }
}

