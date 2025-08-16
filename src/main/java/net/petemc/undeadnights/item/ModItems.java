package net.petemc.undeadnights.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.entity.ModEntities;

public class ModItems {
    public static final Item HORDE_ZOMBIE_SPAWN_EGG = registerItem("horde_zombie_spawn_egg",
            new SpawnEggItem(ModEntities.HORDE_ZOMBIE, -9543745, -10987715, (new Item.Settings())));
    public static final Item ELITE_ZOMBIE_SPAWN_EGG = registerItem("elite_zombie_spawn_egg",
            new SpawnEggItem(ModEntities.ELITE_ZOMBIE, -10127264, -6685775, (new Item.Settings())));
    public static final Item DEMOLITION_ZOMBIE_SPAWN_EGG = registerItem("demolition_zombie_spawn_egg",
            new SpawnEggItem(ModEntities.DEMOLITION_ZOMBIE, -16765109, -16738748, (new Item.Settings())));
    public static final Item SLIMY_ROTTEN_FLESH = registerItem("slimy_rotten_flesh", new Item(new Item.Settings()));

    private static void addItemsToFoodDrinkItemGroup(FabricItemGroupEntries entries) {
        entries.add(HORDE_ZOMBIE_SPAWN_EGG);
        entries.add(ELITE_ZOMBIE_SPAWN_EGG);
        entries.add(DEMOLITION_ZOMBIE_SPAWN_EGG);
    }

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(UndeadNights.MOD_ID, name), item);
    }

    public static void registerItems() {
        UndeadNights.LOGGER.info("Registering Mod Items for " + UndeadNights.MOD_ID);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(ModItems::addItemsToFoodDrinkItemGroup);
    }
}

