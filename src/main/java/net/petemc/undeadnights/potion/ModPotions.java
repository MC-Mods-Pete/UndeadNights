package net.petemc.undeadnights.potion;

import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.effect.ModEffects;
import net.petemc.undeadnights.item.ModItems;

public class ModPotions {
    public static RegistryEntry<Potion> LURE_HORDE_POTION;
    public static RegistryEntry<Potion> STRONG_LURE_HORDE_POTION;

    public static RegistryEntry<Potion> registerPotion(String name, StatusEffectInstance statusEffectInstance) {
        return Registry.registerReference(Registries.POTION, Identifier.of(UndeadNights.MOD_ID, name), new Potion(statusEffectInstance));
    }

    public static void registerPotions() {
        LURE_HORDE_POTION = registerPotion("lure_horde_potion", new StatusEffectInstance(ModEffects.LURE_HORDE, UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsLureEffect().getDurationForLureHordeEffect() *20, 0));
        STRONG_LURE_HORDE_POTION = registerPotion("strong_lure_horde_potion", new StatusEffectInstance(ModEffects.STRONG_LURE_HORDE, UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsLureEffect().getDurationForLureHordeEffect() *20, 0));
        registerPotionRecipes();
    }

    private static void registerPotionRecipes() {
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
            builder.registerPotionRecipe(Potions.AWKWARD, ModItems.SLIMY_ROTTEN_FLESH, ModPotions.LURE_HORDE_POTION);
            builder.registerPotionRecipe(ModPotions.LURE_HORDE_POTION,ModItems.SLIMY_ROTTEN_FLESH, ModPotions.STRONG_LURE_HORDE_POTION);
        });
    }
}
