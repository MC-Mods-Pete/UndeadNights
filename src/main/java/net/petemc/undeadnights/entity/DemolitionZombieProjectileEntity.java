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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class DemolitionZombieProjectileEntity extends AbstractArrow implements ItemSupplier {
    public DemolitionZombieProjectileEntity(EntityType<? extends DemolitionZombieProjectileEntity> type, Level world) {
        super(type, world);
    }

    public DemolitionZombieProjectileEntity(LivingEntity entity, Level level) {
        super(ModEntities.TNT_PROJECTILE.get(), entity, level, new ItemStack(Items.TNT), null);
    }

    public DemolitionZombieProjectileEntity(EntityType<? extends DemolitionZombieProjectileEntity> type, double x, double y, double z, Level level, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.TNT_PROJECTILE.get(), x, y, z, level, pickupItemStack, null);
    }

    protected @NotNull SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.SLIME_BLOCK_PLACE;
    }

    protected void onHitBlock(@NotNull BlockHitResult pResult) {
        super.onHitBlock(pResult);
        this.setSoundEvent(SoundEvents.SLIME_BLOCK_PLACE);
    }

    @Override
    protected @NotNull ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.TNT);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public @NotNull ItemStack getItem() {
        return new ItemStack(Items.TNT);
    }
}
