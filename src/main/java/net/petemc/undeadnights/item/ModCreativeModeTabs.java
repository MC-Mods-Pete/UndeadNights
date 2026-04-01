package net.petemc.undeadnights.item;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.potion.ModPotions;

public class ModCreativeModeTabs {
    public static final CreativeModeTab UNDEAD_NIGHTS_ITEMS_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(UndeadNights.MOD_ID, "undeadnights"),
            FabricCreativeModeTab.builder().title(Component.translatable("itemgroup.undeadnights"))
                    .icon(() -> new ItemStack(ModItems.SLIMY_ROTTEN_FLESH)).displayItems((displayContext, entries) -> {
                        entries.accept(ModItems.SLIMY_ROTTEN_FLESH);
                        entries.accept(PotionContents.createItemStack(Items.POTION, ModPotions.LURE_HORDE_POTION));
                        entries.accept(PotionContents.createItemStack(Items.LINGERING_POTION, ModPotions.LURE_HORDE_POTION));
                        entries.accept(PotionContents.createItemStack(Items.SPLASH_POTION, ModPotions.LURE_HORDE_POTION));
                        entries.accept(PotionContents.createItemStack(Items.TIPPED_ARROW, ModPotions.LURE_HORDE_POTION));
                        entries.accept(PotionContents.createItemStack(Items.POTION, ModPotions.STRONG_LURE_HORDE_POTION));
                        entries.accept(PotionContents.createItemStack(Items.LINGERING_POTION, ModPotions.STRONG_LURE_HORDE_POTION));
                        entries.accept(PotionContents.createItemStack(Items.SPLASH_POTION, ModPotions.STRONG_LURE_HORDE_POTION));
                        entries.accept(PotionContents.createItemStack(Items.TIPPED_ARROW, ModPotions.STRONG_LURE_HORDE_POTION));
                        entries.accept(ModItems.HORDE_ZOMBIE_SPAWN_EGG);
                        entries.accept(ModItems.ELITE_ZOMBIE_SPAWN_EGG);
                        entries.accept(ModItems.DEMOLITION_ZOMBIE_SPAWN_EGG);
                    }).build());

    public static void registerItemGroups() {
        UndeadNights.LOGGER.info("Registering Item Groups for " + UndeadNights.MOD_ID);
    }
}
