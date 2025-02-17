package net.petemc.undeadnights.mixin;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
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
    public void init(MinecraftServer pServer, Executor p_203763_, LevelStorageSource.LevelStorageAccess p_203764_, ServerLevelData pLevelData, ResourceKey<Level> pDimension, Holder<DimensionType> pDimensionType, ChunkProgressListener p_203768_, ChunkGenerator p_203769_, boolean pIsDebug, long pSeed, List<CustomSpawner> pCustomSpawners, boolean pTickTime, CallbackInfo ci)
    {
        ArrayList<CustomSpawner> undeadSpawner = new ArrayList<>(this.customSpawners);
        undeadSpawner.add(new UndeadSpawner());
        this.customSpawners = undeadSpawner.stream().toList();
    }
}
