package net.petemc.undeadnights.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public class MobEntityMixin {

    @Inject(method = "removeWhenFarAway", at = @At("HEAD"), cancellable = true, remap = false)
    public void removeWhenFarAway(double pDistanceToClosestPlayer, CallbackInfoReturnable<Boolean> cir) {
        if (MainConfig.getPreventDespawnWhenInsideMaxTrackingRange()) {
            if (UndeadNights.serverState.spawnedHordeMobs.containsKey(((Entity) (Object) this).getUUID())) {
                LivingEntity livingEntity = ((LivingEntity) (Object) this);
                AttributeInstance attributeInstance = livingEntity.getAttribute(Attributes.FOLLOW_RANGE);
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
