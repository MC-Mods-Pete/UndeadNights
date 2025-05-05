package net.petemc.undeadnights.entity.ai.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class TntIgniteAndThrowGoal extends Goal {
	private final DemolitionZombieEntity demolitionZombie;
	private int tntCoolDown = 5;
	@Nullable
	private LivingEntity target;

	public TntIgniteAndThrowGoal(DemolitionZombieEntity demolitionZombie) {
		this.demolitionZombie = demolitionZombie;
	}

	@Override
	public boolean canStart() {
		LivingEntity target = this.demolitionZombie.getTarget();
		if (!(target instanceof PlayerEntity)) {
			return false;
		}
		return this.demolitionZombie.squaredDistanceTo(target) < 18.0;
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
				if ((this.demolitionZombie.squaredDistanceTo(this.target) < 18.0)
				&& (this.demolitionZombie.squaredDistanceTo(this.target) > 5.0)
						&& this.demolitionZombie.getVisibilityCache().canSee(this.target)
						&& this.demolitionZombie.getMainHandStack().getCount() > 0) {
					TntEntity tntEntity = new TntEntity(this.demolitionZombie.getWorld(), (double) pos.getX() + 0.5, pos.getY() + 0.5, (double) pos.getZ() + 0.5, this.demolitionZombie);

					Random random = this.demolitionZombie.world.random;
					double d0 = target.getY() + (double)target.getStandingEyeHeight() - 1.1;
					double d1 = target.getX() - this.demolitionZombie.getX();
					double d3 = target.getZ() - this.demolitionZombie.getZ();
					double pX = d1;
					double pY = d0 - tntEntity.getY() + Math.sqrt(d1 * d1 + d3 * d3) * (double)0.2F;
					double pZ = d3;
					double pVelocity = 0.8F;
					double pInaccuracy = 12.0F;
					Vec3d vec3 = (new Vec3d(pX, pY, pZ)).normalize().add(triangle(random,0.0D, 0.0172275D * (double)pInaccuracy), triangle(random,0.0D, 0.0172275D * (double)pInaccuracy), triangle(random,0.0D, 0.0172275D * (double)pInaccuracy)).multiply((double)pVelocity);
					tntEntity.setVelocity(vec3);
					double d0a = vec3.horizontalLength();
					tntEntity.setYaw((float)(Math.atan2(vec3.x, vec3.z) * (double)(180F / (float)Math.PI)));
					tntEntity.setPitch((float)(Math.atan2(vec3.y, d0a) * (double)(180F / (float)Math.PI)));
					tntEntity.prevYaw = tntEntity.getYaw();
					tntEntity.prevPitch = tntEntity.getPitch();

					this.demolitionZombie.getWorld().spawnEntity(tntEntity);
					this.demolitionZombie.getWorld().playSound(null, tntEntity.getX(), tntEntity.getY(), tntEntity.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
					if (this.demolitionZombie.getNumberTnt() != 0) {
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

	private double triangle(Random random, double pMin, double pMax) {
		return pMin + pMax * (random.nextDouble() - random.nextDouble());
	}
}
