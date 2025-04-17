package net.petemc.undeadnights.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class DemolitionZombieProjectileEntity extends AbstractArrow implements ItemSupplier {
    public DemolitionZombieProjectileEntity(EntityType<? extends DemolitionZombieProjectileEntity> type, Level world) {
        super(type, world);
    }

    public DemolitionZombieProjectileEntity(LivingEntity entity, Level world) {
        super(ModEntities.TNT_PROJECTILE.get(), entity, world);
    }

    public DemolitionZombieProjectileEntity(double x, double y, double z, Level world) {
        super(ModEntities.TNT_PROJECTILE.get(), x, y, z, world);
    }

    protected @NotNull SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.SLIME_BLOCK_PLACE;
    }

    protected void onHitBlock(@NotNull BlockHitResult pResult) {
        super.onHitBlock(pResult);
        this.setSoundEvent(SoundEvents.SLIME_BLOCK_PLACE);
    }

    @Override
    protected @NotNull ItemStack getPickupItem() {
        return new ItemStack(Items.SLIME_BALL);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public @NotNull ItemStack getItem() {
        return new ItemStack(Items.SLIME_BALL);
    }
}
