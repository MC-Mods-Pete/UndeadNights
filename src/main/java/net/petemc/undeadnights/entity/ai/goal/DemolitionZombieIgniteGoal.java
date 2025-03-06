package net.petemc.undeadnights.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
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
	public boolean canUse() {
		LivingEntity target = this.demolitionZombie.getTarget();
		if (!(target instanceof Player)) {
			return false;
		}
		return this.demolitionZombie.distanceToSqr(target.getX(), target.getY(), target.getZ()) < 12.0;
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
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		BlockPos pos = this.demolitionZombie.blockPosition();
		if (tntCoolDown <= 0) {
			if (this.target != null) {
				if (this.demolitionZombie.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ()) < 12.0 && this.demolitionZombie.getSensing().hasLineOfSight(this.target)
						&& this.demolitionZombie.getMainHandItem().getCount() > 0) {
					PrimedTnt tntEntity = new PrimedTnt(this.demolitionZombie.level, (double) pos.getX() + 0.5, pos.getY(), (double) pos.getZ() + 0.5, this.demolitionZombie);
					this.demolitionZombie.level.addFreshEntity(tntEntity);
					this.demolitionZombie.level.playSound(null, tntEntity.getX(), tntEntity.getY(), tntEntity.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
					if (this.demolitionZombie.getNumberTnt() != 0) {
						this.demolitionZombie.getMainHandItem().shrink(1);
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
