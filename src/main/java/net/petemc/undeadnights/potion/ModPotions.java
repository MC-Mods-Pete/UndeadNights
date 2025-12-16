package net.petemc.undeadnights.potion;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.effect.ModEffects;
import net.petemc.undeadnights.item.ModItems;
import net.petemc.undeadnights.mixin.BrewingRecipeRegistryMixin;

public class ModPotions {
    public static final Potion LURE_HORDE_POTION = registerPotion("lure_horde_potion",
            new Potion(new StatusEffectInstance(ModEffects.LURE_HORDE, UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsLureEffect().getDurationForLureHordeEffect() * 20, 0)));

    public static final Potion STRONG_LURE_HORDE_POTION = registerPotion("strong_lure_horde_potion",
            new Potion(new StatusEffectInstance(ModEffects.STRONG_LURE_HORDE, UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsLureEffect().getDurationForLureHordeEffect() * 2 * 20, 0)));

    private static Potion registerPotion(String name, Potion potion) {
        return Registry.register(Registries.POTION, new Identifier(UndeadNights.MOD_ID, name), potion);
    }

    public static void registerPotions() {
        UndeadNights.LOGGER.info("Registering Mod Potions for " + UndeadNights.MOD_ID);
        registerPotionRecipes();
    }

    private static void registerPotionRecipes() {
        BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(Potions.AWKWARD, ModItems.SLIMY_ROTTEN_FLESH,
                ModPotions.LURE_HORDE_POTION);
        BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(ModPotions.LURE_HORDE_POTION, ModItems.SLIMY_ROTTEN_FLESH,
                ModPotions.STRONG_LURE_HORDE_POTION);
    }
}
