package net.petemc.undeadnights.entity.ai.goal;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.casts.BlockBreakingZombie;
import net.petemc.undeadnights.command.HordeMobsCommand;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.entity.EliteZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;

public class BreakBlockGoal extends Goal {
    private final ZombieEntity mob;
    private final float breakProgressPerTick = 0.04f;
    private BlockPos targetBlock;
    private float scaledTargetDestroyTime;
    private float breakProgress;
    private float ratio;

    public BreakBlockGoal(ZombieEntity mob) {
        this.mob = mob;
    }

    public static float blockPosDistance(Entity entity, BlockPos pos) {
        return blockPosDistance(entity.getBlockPos(), pos);
    }

    public static float blockPosDistance(BlockPos pos1, BlockPos pos2) {
        float x = (pos1.getX() - pos2.getX());
        float y = (pos1.getY() - pos2.getY());
        float z = (pos1.getZ() - pos2.getZ());
        return MathHelper.sqrt(x * x + y * y + z * z);
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public boolean shouldContinue() {
        return blockPosDistance(mob, targetBlock) <= 3 && this.breakProgress <= this.scaledTargetDestroyTime;
    }

    @Override
    public void start() {
        if (this.mob instanceof HordeZombieEntity hordeZombie) {
            hordeZombie.setBreakingBlock(true);
        }
        if (this.mob instanceof EliteZombieEntity eliteZombie) {
            eliteZombie.setBreakingBlock(true);
        }
        if (this.mob instanceof BlockBreakingZombie zombie) {
            zombie.setBreakingBlock(true);
        }
    }

    @Override
    public boolean canStop() {
        return false;
    }

    @Override
    public void tick() {
        breakProgress += breakProgressPerTick;
        mob.swingHand(Hand.MAIN_HAND);
        if (breakProgress >= scaledTargetDestroyTime) {
            mob.getWorld().breakBlock(this.targetBlock, true);
            return;
        }
        mob.getWorld().setBlockBreakingInfo(mob.getId(), targetBlock, (int) (breakProgress * ratio));
    }

    @Override
    public void stop() {
        mob.getWorld().setBlockBreakingInfo(mob.getId(), targetBlock, 0);
        breakProgress = 0;
        targetBlock = null;
        scaledTargetDestroyTime = 0;
        if (this.mob instanceof HordeZombieEntity hordeZombie) {
            hordeZombie.setBreakingBlock(false);
        }
        mob.getNavigation().recalculatePath();
    }



    @Override
    public boolean canStart() {
        if (!HordeMobsCommand.hordeZombiesCanBreakBlocks) {
            return false;
        }

        LivingEntity targetEntity = mob.getTarget();
        if (targetEntity == null) {
            return false;
        }

        if (this.mob.getRandom().nextFloat() < 0.5F) {
            return false;
        }

        final World world = mob.getWorld();
        final Direction direction = mob.getHorizontalFacing();

        BlockPos blockPos = mob.getBlockPos();
        blockPos = blockPos.add(direction.getVector()).add(0, 1, 0);

        if (!mob.getNavigation().isIdle()) {
            return false;
        }

        int yDistance = Math.abs(targetEntity.getBlockY() - mob.getBlockY());
        int yCheckModifier;
        if (yDistance > 2) {
            yCheckModifier = targetEntity.getBlockY() > mob.getY() ? 1 : -2;
        } else {
            yCheckModifier = -1;
        }
        BlockState state = world.getBlockState(blockPos);
        Block block = state.getBlock();

        if (block instanceof AirBlock || block.canMobSpawnInside(state)) {
            blockPos = blockPos.add(0, yCheckModifier, 0);
            state = world.getBlockState(blockPos);
            block = state.getBlock();
            if (block instanceof AirBlock || block.canMobSpawnInside(state)) {
                return false;
            }
        }

        targetBlock = blockPos;
        float destroyTime = world.getBlockState(targetBlock).getBlock().getHardness();
        scaledTargetDestroyTime = destroyTime * 2;
        if (MainConfig.getPrintDebugMessages()) {
            UndeadNights.LOGGER.info("Block: {} destroyTime: {} Stage: {}", world.getBlockState(targetBlock).getBlock(), world.getBlockState(targetBlock).getBlock().getHardness(), UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getBlockBreakingTier());
        }

        if (block instanceof DoorBlock) {
            if (destroyTime == 3.0f) {
                ratio = 10 / scaledTargetDestroyTime;
                return true;
            }
        }

        if (scaledTargetDestroyTime < 0) {
            return false;
        }
        if ((destroyTime > 0.6f) && (HordeMobsCommand.hordeZombiesBlockBreakingTier <= 1)) {
            return false;
        }
        if ((destroyTime > 3.0f) && (HordeMobsCommand.hordeZombiesBlockBreakingTier == 2)) {
            return false;
        }
        if ((destroyTime > 20.0f) && (HordeMobsCommand.hordeZombiesBlockBreakingTier == 3)) {
            return false;
        }
        if (destroyTime > 75.0f) {
            return false;
        }

        if (destroyTime >= 50.0f) {
            scaledTargetDestroyTime = 25.0f;
        }

        ratio = 10 / scaledTargetDestroyTime;
        return true;
    }
}
