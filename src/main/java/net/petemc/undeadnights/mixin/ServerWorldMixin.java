package net.petemc.undeadnights.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.petemc.undeadnights.world.spawner.UndeadSpawner;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

@Mixin(ServerLevel.class)
public class ServerWorldMixin
{
    @Mutable
    @Shadow @Final private List<CustomSpawner> customSpawners;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(MinecraftServer pServer, Executor pDispatcher, LevelStorageSource.LevelStorageAccess pLevelStorageAccess, ServerLevelData pServerLevelData, ResourceKey<Level> pDimension, LevelStem pLevelStem, boolean pIsDebug, long pBiomeZoomSeed, List<CustomSpawner> pCustomSpawners, boolean pTickTime, @Nullable RandomSequences pRandomSequences, CallbackInfo ci)
    {
        ArrayList<CustomSpawner> undeadSpawner = new ArrayList<>(this.customSpawners);
        undeadSpawner.add(new UndeadSpawner());
        this.customSpawners = undeadSpawner.stream().toList();
    }
}
