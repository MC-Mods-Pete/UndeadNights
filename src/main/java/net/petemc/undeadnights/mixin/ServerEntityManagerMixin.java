package net.petemc.undeadnights.mixin;

import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.world.entity.EntityLike;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerEntityManager.class)
public class ServerEntityManagerMixin
{
    @Inject(method = "unload(Lnet/minecraft/world/entity/EntityLike;)V", at = @At("HEAD"))
    public void unload(EntityLike entityLike, CallbackInfo ci) {
        if (UndeadNights.serverState.spawnedHordeMobs.contains(entityLike.getUuid())) {
            UndeadNights.globalSpawnCounter--;
            if (MainConfig.getPrintDebugMessages()) {
                UndeadNights.LOGGER.info("UNLOAD GlobalSpawnCount: {} {} {}", UndeadNights.globalSpawnCounter, "UNLOADED_TO_CHUNK", entityLike.getUuid());
            }
        }
    }
}
