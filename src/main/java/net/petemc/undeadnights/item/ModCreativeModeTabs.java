package net.petemc.undeadnights.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.potion.ModPotions;

public class ModCreativeModeTabs {
    public static final ItemGroup UNDEAD_NIGHTS_ITEMS_TAB = Registry.register(Registries.ITEM_GROUP,
            new Identifier(UndeadNights.MOD_ID, "undeadnights"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.undeadnights"))
                    .icon(() -> new ItemStack(ModItems.SLIMY_ROTTEN_FLESH)).entries((displayContext, entries) -> {
                        entries.add(ModItems.SLIMY_ROTTEN_FLESH);
                        entries.add(PotionUtil.setPotion(new ItemStack(Items.POTION), ModPotions.LURE_HORDE_POTION));
                        entries.add(PotionUtil.setPotion(new ItemStack(Items.LINGERING_POTION), ModPotions.LURE_HORDE_POTION));
                        entries.add(PotionUtil.setPotion(new ItemStack(Items.SPLASH_POTION), ModPotions.LURE_HORDE_POTION));
                        entries.add(PotionUtil.setPotion(new ItemStack(Items.TIPPED_ARROW), ModPotions.LURE_HORDE_POTION));
                        entries.add(ModItems.HORDE_ZOMBIE_SPAWN_EGG);
                        entries.add(ModItems.ELITE_ZOMBIE_SPAWN_EGG);
                        entries.add(ModItems.DEMOLITION_ZOMBIE_SPAWN_EGG);
                    }).build());

    public static void registerItemGroups() {
        UndeadNights.LOGGER.info("Registering Item Groups for " + UndeadNights.MOD_ID);
    }
}
