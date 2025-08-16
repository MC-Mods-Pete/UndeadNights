package net.petemc.undeadnights.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.entity.ModEntities;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(UndeadNights.MOD_ID);

    public static final DeferredHolder<Item, SpawnEggItem> HORDE_ZOMBIE_SPAWN_EGG = ITEMS.register("horde_zombie_spawn_egg",
            () -> new SpawnEggItem(ModEntities.HORDE_ZOMBIE.get(), (new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(UndeadNights.MOD_ID, "horde_zombie_spawn_egg"))))));

    public static final DeferredHolder<Item, SpawnEggItem> ELITE_ZOMBIE_SPAWN_EGG = ITEMS.register("elite_zombie_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ELITE_ZOMBIE.get(), (new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(UndeadNights.MOD_ID, "elite_zombie_spawn_egg"))))));

    public static final DeferredHolder<Item, SpawnEggItem> DEMOLITION_ZOMBIE_SPAWN_EGG = ITEMS.register("demolition_zombie_spawn_egg",
            () -> new SpawnEggItem(ModEntities.DEMOLITION_ZOMBIE.get(), (new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(UndeadNights.MOD_ID, "demolition_zombie_spawn_egg"))))));

    public static final DeferredItem<Item> SLIMY_ROTTEN_FLESH = ITEMS.register("slimy_rotten_flesh",
            () -> new Item(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(UndeadNights.MOD_ID, "slimy_rotten_flesh")))));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
