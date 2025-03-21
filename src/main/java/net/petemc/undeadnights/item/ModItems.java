package net.petemc.undeadnights.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.entity.ModEntities;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, UndeadNights.MOD_ID);

    public static final RegistryObject<Item> HORDE_ZOMBIE_SPAWN_EGG = ITEMS.register("horde_zombie_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.HORDE_ZOMBIE, -9543745, -10987715, (new Item.Properties().tab(CreativeModeTab.TAB_MISC))));

    public static final RegistryObject<Item> ELITE_ZOMBIE_SPAWN_EGG = ITEMS.register("elite_zombie_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ELITE_ZOMBIE, -10127264, -6685775, (new Item.Properties().tab(CreativeModeTab.TAB_MISC))));

    public static final RegistryObject<Item> DEMOLITION_ZOMBIE_SPAWN_EGG = ITEMS.register("demolition_zombie_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.DEMOLITION_ZOMBIE, -16765109, -16738748, (new Item.Properties().tab(CreativeModeTab.TAB_MISC))));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}

