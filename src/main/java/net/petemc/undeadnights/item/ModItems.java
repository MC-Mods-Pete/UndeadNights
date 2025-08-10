package net.petemc.undeadnights.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.entity.ModEntities;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, UndeadNights.MOD_ID);

    public static final RegistryObject<Item> HORDE_ZOMBIE_SPAWN_EGG = ITEMS.register("horde_zombie_spawn_egg",
            () -> new SpawnEggItem(ModEntities.HORDE_ZOMBIE.get(), (new Item.Properties())
                    .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(UndeadNights.MOD_ID, "horde_zombie_spawn_egg")))));

    public static final RegistryObject<Item> ELITE_ZOMBIE_SPAWN_EGG = ITEMS.register("elite_zombie_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ELITE_ZOMBIE.get(), (new Item.Properties())
                    .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(UndeadNights.MOD_ID, "elite_zombie_spawn_egg")))));

    public static final RegistryObject<Item> DEMOLITION_ZOMBIE_SPAWN_EGG = ITEMS.register("demolition_zombie_spawn_egg",
            () -> new SpawnEggItem(ModEntities.DEMOLITION_ZOMBIE.get(), (new Item.Properties())
                    .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(UndeadNights.MOD_ID, "demolition_zombie_spawn_egg")))));

    public static void register(BusGroup modBusGroup) {
        ITEMS.register(modBusGroup);
    }

}

