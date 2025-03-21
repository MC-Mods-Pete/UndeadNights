package net.petemc.undeadnights.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.entity.ModEntities;

public class ModItems {
    public static final Item HORDE_ZOMBIE_SPAWN_EGG = registerItem("horde_zombie_spawn_egg",
            new SpawnEggItem(ModEntities.HORDE_ZOMBIE, -9543745, -10987715, (new FabricItemSettings()
                    .group(ItemGroup.MISC))));
    public static final Item ELITE_ZOMBIE_SPAWN_EGG = registerItem("elite_zombie_spawn_egg",
            new SpawnEggItem(ModEntities.ELITE_ZOMBIE, -10127264, -6685775, (new FabricItemSettings()
                    .group(ItemGroup.MISC))));
    public static final Item DEMOLITION_ZOMBIE_SPAWN_EGG = registerItem("demolition_zombie_spawn_egg",
            new SpawnEggItem(ModEntities.DEMOLITION_ZOMBIE, -16765109, -16738748, (new FabricItemSettings()
                    .group(ItemGroup.MISC))));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registry.ITEM, new Identifier(UndeadNights.MOD_ID, name), item);
    }

    public static void registerItems() {
        UndeadNights.LOGGER.info("Registering Mod Items for " + UndeadNights.MOD_ID);
    }
}

