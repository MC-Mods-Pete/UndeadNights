package net.petemc.undeadnights.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public class MobEntityMixin {

    @Inject(method = "canImmediatelyDespawn", at = @At("HEAD"), cancellable = true)
    public void removeWhenFarAway(double pDistanceToClosestPlayer, CallbackInfoReturnable<Boolean> cir) {
        if (MainConfig.getPreventDespawnWhenInsideMaxTrackingRange()) {
            if (UndeadNights.serverState.spawnedHordeMobs.containsKey(((Entity) (Object) this).getUuid())) {
                LivingEntity livingEntity = ((LivingEntity) (Object) this);
                EntityAttributeInstance attributeInstance = livingEntity.getAttributeInstance(EntityAttributes.FOLLOW_RANGE);
                double trackingRange = UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getHordeMobsTrackingRange();
                if (attributeInstance != null) {
                    trackingRange = attributeInstance.getValue();
                }
                boolean returnValue = (trackingRange*trackingRange) + 16.0D < pDistanceToClosestPlayer;
                //UndeadNights.LOGGER.info("[UndeadNights] removeWhenFarAway2: Return Value: " + returnValue + " | Distance to Closest Player: " + pDistanceToClosestPlayer + " | Tracking Range + 16: " + ((trackingRange*trackingRange) + 16.0D));
                cir.setReturnValue(returnValue);
            }
        }
    }
}
