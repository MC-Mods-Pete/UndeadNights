package net.petemc.undeadnights.item;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.entity.ModEntities;

public class ModItems {
    public static final Item HORDE_ZOMBIE_SPAWN_EGG = registerItem("horde_zombie_spawn_egg",
            new SpawnEggItem(new Item.Properties().spawnEgg(ModEntities.HORDE_ZOMBIE)
                    .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(UndeadNights.MOD_ID, "horde_zombie_spawn_egg")))));
    public static final Item ELITE_ZOMBIE_SPAWN_EGG = registerItem("elite_zombie_spawn_egg",
            new SpawnEggItem(new Item.Properties().spawnEgg(ModEntities.ELITE_ZOMBIE)
                    .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(UndeadNights.MOD_ID, "elite_zombie_spawn_egg")))));
    public static final Item DEMOLITION_ZOMBIE_SPAWN_EGG = registerItem("demolition_zombie_spawn_egg",
            new SpawnEggItem(new Item.Properties().spawnEgg(ModEntities.DEMOLITION_ZOMBIE)
                    .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(UndeadNights.MOD_ID, "demolition_zombie_spawn_egg")))));
    public static final Item SLIMY_ROTTEN_FLESH = registerItem("slimy_rotten_flesh", new Item(new Item.Properties()
            .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(UndeadNights.MOD_ID, "slimy_rotten_flesh")))));

    private static void addItemsToSpawnEggsGroup(FabricCreativeModeTabOutput entries) {
        entries.accept(HORDE_ZOMBIE_SPAWN_EGG);
        entries.accept(ELITE_ZOMBIE_SPAWN_EGG);
        entries.accept(DEMOLITION_ZOMBIE_SPAWN_EGG);
    }

    private static Item registerItem(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(UndeadNights.MOD_ID, name), item);
    }

    public static void registerItems() {
        UndeadNights.LOGGER.info("Registering Mod Items for " + UndeadNights.MOD_ID);
        ResourceKey<CreativeModeTab> spawnEggsTab = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("spawn_eggs"));
        CreativeModeTabEvents.modifyOutputEvent(spawnEggsTab).register(ModItems::addItemsToSpawnEggsGroup);
    }
}
