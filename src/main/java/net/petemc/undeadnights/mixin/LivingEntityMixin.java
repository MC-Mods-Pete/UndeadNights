package net.petemc.undeadnights.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.casts.UndeadNightsExtendedPlayer;
import net.petemc.undeadnights.effect.ModEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin
{
    @Inject(method = "getWaterSlowDown", at = @At("HEAD"), cancellable = true)
    protected void getWaterSlowDown(CallbackInfoReturnable<Float> cir) {
        if (((Entity) (Object) this) instanceof Zombie) {
            if (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().isHordeZombiesAreFasterOnWater()) {
                cir.setReturnValue(0.94f);
            }
        }
    }

    @Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSource;getEntity()Lnet/minecraft/world/entity/Entity;", shift = At.Shift.AFTER))
    public void die_lureEffect(DamageSource pDamageSource, CallbackInfo ci) {
        if ((pDamageSource != null) && (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsLureEffect().isEnableLureHordeEffect())) {
            if (pDamageSource.getEntity() instanceof ServerPlayer player) {
                boolean isZombie = (((Entity)(Object) this) instanceof Zombie);
                boolean flag = (UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsLureEffect().isNonHordeZombiesCanCauseLureHordeEffect() && isZombie);
                if ((UndeadNights.serverState.spawnedHordeMobs.contains(((Entity)(Object) this).getUUID())) || flag) {
                    RandomSource randomSource = player.level().random;
                    double rand = randomSource.nextDouble();
                    if (rand <= UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsLureEffect().getChanceForLureHordeEffect()) {
                        if (!player.hasEffect(ModEffects.LURE_HORDE.getHolder().get())) {
                            if (player instanceof UndeadNightsExtendedPlayer hordeLurePlayer) {
                                hordeLurePlayer.undeadnights_setHordeLureEffect(false);
                            }
                            player.addEffect(new MobEffectInstance(ModEffects.LURE_HORDE.getHolder().get(), UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsLureEffect().getDurationForLureHordeEffect() * 20, 0));
                        }
                    }
                }
            }
        }
    }
}
