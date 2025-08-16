package net.petemc.undeadnights.mixin;

import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.petemc.undeadnights.UndeadNights;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentEntitySectionManager.class)
public class PersistentEntitySectionManagerMixin
{
    @Inject(method = "unloadEntity", at = @At("HEAD"))
    public void getMessage (EntityAccess entityAccess, CallbackInfo ci)
    {
        if (UndeadNights.serverState.spawnedHordeMobs.containsKey(entityAccess.getUUID())) {
            UndeadNights.globalSpawnCounter--;
            UndeadNights.LOGGER.info("UNLOAD GlobalSpawnCount: {} {} {}", UndeadNights.globalSpawnCounter, "UNLOADED_TO_CHUNK", entityAccess.getUUID());
        }
    }
}
