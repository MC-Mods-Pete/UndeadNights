package net.petemc.undeadnights.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.entity.DemolitionZombieEntity;
import net.petemc.undeadnights.entity.EliteZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public class MobEntityMixin {

    /**
     * In MC 26.1, isSunSensitive() is dead code - sun burning is now controlled via
     * EntityTypeTags.BURN_IN_DAYLIGHT + Mob.burnUndead().
     * Custom mobs are added to the tag via data/minecraft/tags/entity_type/burn_in_daylight.json,
     * but we suppress burnUndead() here based on config.
     * Vanilla zombie burn control (previously in ZombieEntityMixin.isSunSensitive) is also handled here.
     */
    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;burnUndead()V"), cancellable = true)
    private void overrideBurnInDaylight(CallbackInfo ci) {
        Object self = this;
        if (self instanceof HordeZombieEntity || self instanceof EliteZombieEntity || self instanceof DemolitionZombieEntity) {
            if (!UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().isHordeZombiesBurnInTheSun()) {
                ci.cancel();
            }
        } else if (self instanceof Zombie) {
            // Vanilla zombies (zombie, zombie_villager, drowned, husk, etc.)
            if (!UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().isVanillaZombiesBurnInTheSun()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "removeWhenFarAway", at = @At("HEAD"), cancellable = true)
    public void removeWhenFarAway(double pDistanceToClosestPlayer, CallbackInfoReturnable<Boolean> cir) {
        if (MainConfig.getPreventDespawnWhenInsideMaxTrackingRange()) {
            if (UndeadNights.serverState.spawnedHordeMobs.containsKey(((Entity) (Object) this).getUUID())) {
                LivingEntity livingEntity = ((LivingEntity) (Object) this);
                AttributeInstance attributeInstance = livingEntity.getAttribute(Attributes.FOLLOW_RANGE);
                double trackingRange = UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsHordeMobs().getHordeMobsTrackingRange();
                if (attributeInstance != null) {
                    trackingRange = attributeInstance.getValue();
                }
                boolean returnValue = (trackingRange * trackingRange) + 16.0D < pDistanceToClosestPlayer;
                cir.setReturnValue(returnValue);
            }
        }
    }
}
