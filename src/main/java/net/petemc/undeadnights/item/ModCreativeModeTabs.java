package net.petemc.undeadnights.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.potion.ModPotions;

public class ModCreativeModeTabs {
    public static final ItemGroup UNDEAD_NIGHTS_ITEMS_TAB = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(UndeadNights.MOD_ID, "undeadnights"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.undeadnights"))
                    .icon(() -> new ItemStack(ModItems.SLIMY_ROTTEN_FLESH)).entries((displayContext, entries) -> {
                        entries.add(ModItems.SLIMY_ROTTEN_FLESH);
                        entries.add(PotionContentsComponent.createStack(Items.POTION, ModPotions.LURE_HORDE_POTION));
                        entries.add(PotionContentsComponent.createStack(Items.LINGERING_POTION, ModPotions.LURE_HORDE_POTION));
                        entries.add(PotionContentsComponent.createStack(Items.SPLASH_POTION, ModPotions.LURE_HORDE_POTION));
                        entries.add(PotionContentsComponent.createStack(Items.TIPPED_ARROW, ModPotions.LURE_HORDE_POTION));
                        entries.add(PotionContentsComponent.createStack(Items.POTION, ModPotions.STRONG_LURE_HORDE_POTION));
                        entries.add(PotionContentsComponent.createStack(Items.LINGERING_POTION, ModPotions.STRONG_LURE_HORDE_POTION));
                        entries.add(PotionContentsComponent.createStack(Items.SPLASH_POTION, ModPotions.STRONG_LURE_HORDE_POTION));
                        entries.add(PotionContentsComponent.createStack(Items.TIPPED_ARROW, ModPotions.STRONG_LURE_HORDE_POTION));
                        entries.add(ModItems.HORDE_ZOMBIE_SPAWN_EGG);
                        entries.add(ModItems.ELITE_ZOMBIE_SPAWN_EGG);
                        entries.add(ModItems.DEMOLITION_ZOMBIE_SPAWN_EGG);
                    }).build());

    public static void registerItemGroups() {
        UndeadNights.LOGGER.info("Registering Item Groups for " + UndeadNights.MOD_ID);
    }
}
