package net.petemc.undeadnights.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.casts.BlockBreakingZombie;
import net.petemc.undeadnights.command.HordeMobsCommand;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.entity.EliteZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;

public class BreakBlockGoal extends Goal {
    private final Zombie mob;
    private final float breakProgressPerTick = 0.04f;
    private BlockPos targetBlock;
    private float scaledTargetDestroyTime;
    private float breakProgress;
    private float ratio;

    public BreakBlockGoal(Zombie mob) {
        this.mob = mob;
    }

    public static float blockPosDistance(Entity entity, BlockPos pos) {
        return blockPosDistance(entity.blockPosition(), pos);
    }

    public static float blockPosDistance(BlockPos pos1, BlockPos pos2) {
        float x = (pos1.getX() - pos2.getX());
        float y = (pos1.getY() - pos2.getY());
        float z = (pos1.getZ() - pos2.getZ());
        return Mth.sqrt(x * x + y * y + z * z);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canContinueToUse() {
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
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public void tick() {
        breakProgress += breakProgressPerTick;
        mob.swing(InteractionHand.MAIN_HAND);
        if (breakProgress >= scaledTargetDestroyTime) {
            mob.level().destroyBlock(this.targetBlock, true);
            return;
        }
        mob.level().destroyBlockProgress(mob.getId(), targetBlock, (int) (breakProgress * ratio));
    }

    @Override
    public void stop() {
        mob.level().destroyBlockProgress(mob.getId(), targetBlock, 0);
        breakProgress = 0;
        targetBlock = null;
        scaledTargetDestroyTime = 0;
        if (this.mob instanceof HordeZombieEntity hordeZombie) {
            hordeZombie.setBreakingBlock(false);
        }
        mob.getNavigation().recomputePath();
    }



    @Override
    public boolean canUse() {
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

        final Level world = mob.level();
        final Direction direction = mob.getDirection();

        BlockPos blockPos = mob.blockPosition();
        blockPos = blockPos.offset(direction.getUnitVec3i()).offset(0, 1, 0);

        if (!mob.getNavigation().isDone()) {
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

        if (block instanceof AirBlock || block.isPossibleToRespawnInThis(state)) {
            blockPos = blockPos.offset(0, yCheckModifier, 0);
            state = world.getBlockState(blockPos);
            block = state.getBlock();
            if (block instanceof AirBlock || block.isPossibleToRespawnInThis(state)) {
                return false;
            }
        }

        targetBlock = blockPos;
        float destroyTime = world.getBlockState(targetBlock).getBlock().defaultDestroyTime();
        scaledTargetDestroyTime = destroyTime * 2;
        if (MainConfig.getPrintDebugMessages()) {
            UndeadNights.LOGGER.info("Block: {} destroyTime: {} Stage: {}", world.getBlockState(targetBlock).getBlock(), world.getBlockState(targetBlock).getBlock().defaultDestroyTime(), MainConfig.getZombiesBlockBreakTier());
        }

        if (block instanceof DoorBlock) {
            if (destroyTime == 3.0f) {
                ratio = 10 / scaledTargetDestroyTime;
                return true;
            }
        }

        String blockName = world.getBlockState(targetBlock).getBlock().toString();
        if (blockName.contains("securitycraft") && blockName.contains("reinforced") && MainConfig.getSecurityCraftCompatibility()) {
            return false;
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
