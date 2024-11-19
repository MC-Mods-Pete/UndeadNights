package net.petemc.undeadnights.entity.ai.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.petemc.undeadnights.config.UndeadNightsConfig;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import org.jetbrains.annotations.Nullable;

public class DemolitionZombieIgniteGoal extends Goal {
	private final DemolitionZombieEntity demolitionZombie;
	private int tntCoolDown = 0;
	@Nullable
	private LivingEntity target;

	public DemolitionZombieIgniteGoal(DemolitionZombieEntity demolitionZombie) {
		this.demolitionZombie = demolitionZombie;
	}

	@Override
	public boolean canStart() {
		LivingEntity target = this.demolitionZombie.getTarget();
		return target != null && this.demolitionZombie.squaredDistanceTo(target) < 12.0;
	}

	@Override
	public void start() {
		this.target = this.demolitionZombie.getTarget();
	}

	@Override
	public void stop() {
		this.target = null;
	}

	@Override
	public boolean shouldRunEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		BlockPos pos = this.demolitionZombie.getBlockPos();
		if (tntCoolDown <= 0) {
			if (this.target != null) {
				if (this.demolitionZombie.squaredDistanceTo(this.demolitionZombie.getTarget()) < 12.0 && this.demolitionZombie.getVisibilityCache().canSee(this.target)
						&& this.demolitionZombie.getMainHandStack().getCount() > 0) {
					TntEntity tntEntity = new TntEntity(this.demolitionZombie.getEntityWorld(), (double) pos.getX() + 0.5, pos.getY(), (double) pos.getZ() + 0.5, this.demolitionZombie);
					this.demolitionZombie.getEntityWorld().spawnEntity(tntEntity);
					this.demolitionZombie.getEntityWorld().playSound(null, tntEntity.getX(), tntEntity.getY(), tntEntity.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
					if (UndeadNightsConfig.INSTANCE.demolitionZombieTntStackSize != 0) {
						this.demolitionZombie.getMainHandStack().decrement(1);
					}
					tntCoolDown = 5 * 20;
				}
			}
		} else {
			tntCoolDown--;
		}
		super.tick();
	}
}
