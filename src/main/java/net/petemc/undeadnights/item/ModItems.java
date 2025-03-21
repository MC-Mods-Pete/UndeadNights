package net.petemc.undeadnights.item;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.entity.ModEntities;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.createItems(UndeadNights.MOD_ID);

    public static final DeferredHolder<Item, DeferredSpawnEggItem> HORDE_ZOMBIE_SPAWN_EGG = ITEMS.register("horde_zombie_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.HORDE_ZOMBIE, -9543745, -10987715, (new Item.Properties())));

    public static final DeferredHolder<Item, DeferredSpawnEggItem> ELITE_ZOMBIE_SPAWN_EGG = ITEMS.register("elite_zombie_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.ELITE_ZOMBIE, -10127264, -6685775, (new Item.Properties())));

    public static final DeferredHolder<Item, DeferredSpawnEggItem> DEMOLITION_ZOMBIE_SPAWN_EGG = ITEMS.register("demolition_zombie_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.DEMOLITION_ZOMBIE, -16765109, -16738748, (new Item.Properties())));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}

