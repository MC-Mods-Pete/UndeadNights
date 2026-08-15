package net.petemc.undeadnights.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.casts.UndeadNightsExtendedPlayer;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.effect.ModEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(Player.class)
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

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;updatePlayerPose()V", shift = At.Shift.AFTER))
    public void tick(CallbackInfo ci) {
        Player player = (Player) (Object) this;

        if (!player.level().isClientSide()) {
            if (player instanceof UndeadNightsExtendedPlayer hordeLurePlayer) {
                if (hordeLurePlayer.undeadnights_hasHordeLureEffect()) {
                    if (!player.hasEffect(ModEffects.LURE_HORDE) && !player.hasEffect(ModEffects.STRONG_LURE_HORDE)) {
                        if (MainConfig.getPrintDebugMessages()) {
                            UndeadNights.LOGGER.info("Horde lure flag for Player " + player.getName().getString() + " set to false due to missing HORDE_LURE effects.");
                        }
                        hordeLurePlayer.undeadnights_setHordeLureEffect(false);
                    }
                }
            }

            if (coolDown > 0) {
                coolDown--;
            } else {
                coolDown = 5 * 20;

                this.isInCaveStageOne = caveCheckStageOne(player.level(), player.blockPosition());

                if (isInCaveStageOne) {
                    if (delay > 0) {
                        delay--;
                    } else {
                        delay = 3;
                        isInCaveStageTwo = caveCheckStageTwo(player.level(), player.blockPosition());
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
    private static boolean caveCheckStageOne(Level level, BlockPos pos) {
        int layersAbove = 0;
        int x = pos.getX();
        int z = pos.getZ();
        int y;

        y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (y == pos.getY()) {
            return false; // on surface
        }

        y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
        if (y == pos.getY()) {
            return false; // on surface
        }

        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos(x, pos.getY() + 1, z);
        while (checkPos.getY() < y) {
            BlockState state = level.getBlockState(checkPos);

            if (state.isAir()) {
                layersAbove = 0;
                checkPos.move(0, 1, 0);
                continue;
            }
            if (state.is(Blocks.WATER)) {
                layersAbove = 0;
                checkPos.move(0, 1, 0);
                continue;
            }
            if ((state.is(Blocks.DEEPSLATE)) && (checkPos.getY() > 8)) {
                layersAbove = 0;
                checkPos.move(0, 1, 0);
                continue;
            }
            if (state.is(Blocks.COBBLESTONE)) {
                layersAbove = 0;
                checkPos.move(0, 1, 0);
                continue;
            }
            if (state.is(BlockTags.LEAVES)) {
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
    private static boolean caveCheckStageTwo(Level level, BlockPos pos) {
        AABB box = new AABB(pos).inflate(10, 0, 10);
        AtomicBoolean isCave = new AtomicBoolean(true);
        BlockPos.MutableBlockPos.betweenClosedStream(box)
                .forEach(c -> {
                    int y1 = level.getHeight(Heightmap.Types.MOTION_BLOCKING, c.getX(), c.getZ());
                    if ((c.getY() + 5) >= y1) {
                        isCave.set(false);
                    }
                });
        return isCave.get();
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void injectToReadNbt(CompoundTag nbt, CallbackInfo ci) {
        this.isInCave = nbt.getBoolean("undeadnights_is_player_in_cave");
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void injectToWriteNbt(CompoundTag nbt, CallbackInfo ci) {
        nbt.putBoolean("undeadnights_is_player_in_cave", this.isInCave);
    }
}
