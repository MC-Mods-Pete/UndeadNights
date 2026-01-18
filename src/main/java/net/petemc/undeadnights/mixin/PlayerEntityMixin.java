package net.petemc.undeadnights.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.petemc.undeadnights.casts.UndeadNightsExtendedPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements UndeadNightsExtendedPlayer {
    @Unique
    private boolean hasHordeLureEffect = false;
    @Unique
    private boolean isInCave = false;
    @Unique
    private boolean isInCaveStageOne = false;
    @Unique
    private boolean previousIsInCaveStageOne = false;
    @Unique
    private boolean isInCaveStageTwo = false;
    @Unique
    private boolean previousIsInCaveStageTwo = false;
    @Unique
    private int coolDown = 5 * 20;
    @Unique
    private int delay = 3;

    @Override
    public void undeadnights_setHordeLureEffect(boolean hordeLureValue) {
        hasHordeLureEffect = hordeLureValue;
    }

    @Override
    public boolean undeadnights_hasHordeLureEffect() {
        return hasHordeLureEffect;
    }

    @Override
    public void undeadnights_setIsInCave(boolean isInCaveValue) {
        isInCave = isInCaveValue;
    }

    @Override
    public boolean undeadnights_isInCave() {
        return isInCave;
    }

    @Override
    public boolean undeadnights_isInCaveDelayed() {
        return isInCaveStageOne;
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;updatePose()V", shift = At.Shift.AFTER))
    public void tick(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (!player.getWorld().isClient()) {
            if (coolDown > 0) {
                coolDown--;
            } else {
                coolDown = 5 * 20;

                this.isInCaveStageOne = caveCheckStageOne(player.getWorld(), player.getBlockPos());

                if (isInCaveStageOne) {
                    if (delay > 0) {
                        delay--;
                    } else {
                        delay = 3;
                        isInCaveStageTwo = caveCheckStageTwo(player.getWorld(), player.getBlockPos());
                        if (isInCaveStageTwo) {
                            undeadnights_setIsInCave(true);
                        }
                    }
                } else {
                    isInCaveStageTwo = false;
                    previousIsInCaveStageTwo = false;
                    undeadnights_setIsInCave(false);
                    delay = 3;
                }

                if (previousIsInCaveStageOne != isInCaveStageOne) {
                    previousIsInCaveStageOne = isInCaveStageOne;
                }

                if (isInCaveStageTwo != previousIsInCaveStageTwo) {
                    previousIsInCaveStageTwo = isInCaveStageTwo;
                }
            }
        }
    }

    // Stage one: quick check for obvious surface locations
    @Unique
    private static boolean caveCheckStageOne(World level, BlockPos pos) {
        int layersAbove = 0;
        int x = pos.getX();
        int z = pos.getZ();
        int y;

        y = level.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (y == pos.getY()) {
            return false; // on surface
        }

        y = level.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z);
        if (y == pos.getY()) {
            return false; // on surface
        }

        BlockPos.Mutable checkPos = new BlockPos.Mutable(x, pos.getY() + 1, z);
        while (checkPos.getY() < y) {
            BlockState state = level.getBlockState(checkPos);

            if (state.isAir()) {
                layersAbove = 0;
                checkPos.move(0, 1, 0);
                continue;
            }
            if (state.isOf(Blocks.WATER)) {
                layersAbove = 0;
                checkPos.move(0, 1, 0);
                continue;
            }
            if ((state.isOf(Blocks.DEEPSLATE)) && (checkPos.getY() > 8)) {
                layersAbove = 0;
                checkPos.move(0, 1, 0);
                continue;
            }
            if (state.isOf(Blocks.COBBLESTONE)) {
                layersAbove = 0;
                checkPos.move(0, 1, 0);
                continue;
            }
            if (state.isIn(BlockTags.LEAVES)) {
                layersAbove = 0;
                checkPos.move(0, 1, 0);
                continue;
            }
            layersAbove++;
            if (layersAbove > 3) {
                return true;
            }
            checkPos.move(0, 1, 0);
        }
        return false; // too few layers above -> do not consider as cave
    }

    // Stage two: more thorough check for surface proximity
    @Unique
    private static boolean caveCheckStageTwo(World level, BlockPos pos) {
        Box box = new Box(pos).expand(10, 0, 10);
        AtomicBoolean isCave = new AtomicBoolean(true);
        BlockPos.Mutable.stream(box)
                .forEach(c -> {
                    int y1 = level.getTopY(Heightmap.Type.MOTION_BLOCKING, c.getX(), c.getZ());
                    if ((c.getY() + 5) >= y1) {
                        isCave.set(false);
                    }
                });
        return isCave.get();
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void injectToReadNbt(NbtCompound nbt, CallbackInfo ci) {
        this.isInCave = nbt.getBoolean("undeadnights_is_player_in_cave");
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void injectToWriteNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putBoolean("undeadnights_is_player_in_cave", this.isInCave);
    }
}
