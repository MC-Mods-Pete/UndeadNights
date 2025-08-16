package net.petemc.undeadnights.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.potion.ModPotions;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, UndeadNights.MOD_ID);

    public static final Supplier<CreativeModeTab> UNDEAD_NIGHTS_ITEMS_TAB = CREATIVE_MODE_TAB.register("undead_nights_items_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.SLIMY_ROTTEN_FLESH.get()))
                    .title(Component.translatable("itemgroup.undeadnights"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.SLIMY_ROTTEN_FLESH.get());
                        output.accept(PotionContents.createItemStack(Items.POTION, ModPotions.LURE_HORDE_POTION));
                        output.accept(PotionContents.createItemStack(Items.LINGERING_POTION, ModPotions.LURE_HORDE_POTION));
                        output.accept(PotionContents.createItemStack(Items.SPLASH_POTION, ModPotions.LURE_HORDE_POTION));
                        output.accept(PotionContents.createItemStack(Items.TIPPED_ARROW, ModPotions.LURE_HORDE_POTION));
                        output.accept(ModItems.HORDE_ZOMBIE_SPAWN_EGG.get());
                        output.accept(ModItems.ELITE_ZOMBIE_SPAWN_EGG.get());
                        output.accept(ModItems.DEMOLITION_ZOMBIE_SPAWN_EGG.get());
                    }).build());


    public static void register(IEventBus eventBus) {
        UndeadNights.LOGGER.info("Registering Item Groups for " + UndeadNights.MOD_ID);
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
