package net.petemc.undeadnights.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DemolitionZombieProjectileEntity extends PersistentProjectileEntity implements FlyingItemEntity {
    public DemolitionZombieProjectileEntity(EntityType<? extends DemolitionZombieProjectileEntity> type, World world) {
        super(type, world);
    }

    public DemolitionZombieProjectileEntity(LivingEntity entity, World world) {
        super(ModEntities.TNT_PROJECTILE, entity, world, new ItemStack(Items.TNT), null);
    }

    public DemolitionZombieProjectileEntity(EntityType<? extends DemolitionZombieProjectileEntity> type, double x, double y, double z, World world, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.TNT_PROJECTILE, x, y, z, world, pickupItemStack, null);
    }

    protected @NotNull SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.BLOCK_SLIME_BLOCK_PLACE;
    }

    protected void onBlockHit(@NotNull BlockHitResult pResult) {
        super.onBlockHit(pResult);
        this.setSound(SoundEvents.BLOCK_SLIME_BLOCK_PLACE);
    }

    @Override
    protected @NotNull ItemStack asItemStack() {
        return new ItemStack(Items.TNT);
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return null;
    }

    @Override
    public @NotNull ItemStack getStack() {
        return new ItemStack(Items.TNT);
    }
}
