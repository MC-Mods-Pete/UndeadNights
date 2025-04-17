package net.petemc.undeadnights.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.phys.Vec3;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import org.jetbrains.annotations.Nullable;

public class TntIgniteAndThrowGoal extends Goal {
	private final DemolitionZombieEntity demolitionZombie;
	private int tntCoolDown = 5;
	@Nullable
	private LivingEntity target;

	public TntIgniteAndThrowGoal(DemolitionZombieEntity demolitionZombie) {
		this.demolitionZombie = demolitionZombie;
	}

	@Override
	public boolean canUse() {
		LivingEntity target = this.demolitionZombie.getTarget();
		if (!(target instanceof Player)) {
			return false;
		}
		return this.demolitionZombie.getPerceivedTargetDistanceSquareForMeleeAttack(target) < 18.0;
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
				if ((this.demolitionZombie.getPerceivedTargetDistanceSquareForMeleeAttack(this.target) < 18.0)
				&& (this.demolitionZombie.getPerceivedTargetDistanceSquareForMeleeAttack(this.target) > 5.0)
						&& this.demolitionZombie.getSensing().hasLineOfSight(this.target)
						&& this.demolitionZombie.getMainHandItem().getCount() > 0) {
					PrimedTnt tntEntity = new PrimedTnt(this.demolitionZombie.level(), (double) pos.getX() + 0.5, pos.getY() + 0.5, (double) pos.getZ() + 0.5, this.demolitionZombie);
					ThrownEnderpearl thrownEnderpearl = new ThrownEnderpearl(this.demolitionZombie.level(), this.demolitionZombie);

					RandomSource random = RandomSource.create();
					double d0 = target.getY() + (double)target.getEyeHeight() - 1.1;
					double d1 = target.getX() - this.demolitionZombie.getX();
					double d3 = target.getZ() - this.demolitionZombie.getZ();
					//thrownEnderpearl.shoot(d1, d0 - tntEntity.getY() + Math.sqrt(d1 * d1 + d3 * d3) * (double)0.2F, d3, 0.6F, 12.0F);
					//tntEntity.shoot(d1, d0 - tntEntity.getY() + Math.sqrt(d1 * d1 + d3 * d3) * (double)0.2F, d3, 1.6F, 12.0F);
					double pX = d1;
					double pY = d0 - tntEntity.getY() + Math.sqrt(d1 * d1 + d3 * d3) * (double)0.2F;
					double pZ = d3;
					double pVelocity = 0.8F;
					double pInaccuracy = 12.0F;
					//shoot(double pX, double pY, double pZ, float pVelocity, float pInaccuracy) {
					Vec3 vec3 = (new Vec3(pX, pY, pZ)).normalize().add(random.triangle(0.0D, 0.0172275D * (double)pInaccuracy), random.triangle(0.0D, 0.0172275D * (double)pInaccuracy), random.triangle(0.0D, 0.0172275D * (double)pInaccuracy)).scale((double)pVelocity);
					tntEntity.setDeltaMovement(vec3);
					double d0a = vec3.horizontalDistance();
					tntEntity.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * (double)(180F / (float)Math.PI)));
					tntEntity.setXRot((float)(Mth.atan2(vec3.y, d0a) * (double)(180F / (float)Math.PI)));
					tntEntity.yRotO = tntEntity.getYRot();
					tntEntity.xRotO = tntEntity.getXRot();

					//tntEntity.setDeltaMovement(0, 0.5, 0);
					this.demolitionZombie.level().addFreshEntity(tntEntity);
					//this.demolitionZombie.level().addFreshEntity(thrownEnderpearl);
					this.demolitionZombie.level().playSound(null, tntEntity.getX(), tntEntity.getY(), tntEntity.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
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
