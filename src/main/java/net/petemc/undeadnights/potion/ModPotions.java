package net.petemc.undeadnights.potion;

import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.effect.ModEffects;
import net.petemc.undeadnights.item.ModItems;

public class ModPotions {
    public static Holder<Potion> LURE_HORDE_POTION;
    public static Holder<Potion> STRONG_LURE_HORDE_POTION;

    public static Holder<Potion> registerPotion(String name, MobEffectInstance effectInstance) {
        return Registry.registerForHolder(BuiltInRegistries.POTION, Identifier.fromNamespaceAndPath(UndeadNights.MOD_ID, name), new Potion(name, effectInstance));
    }

    public static void registerPotions() {
        LURE_HORDE_POTION = registerPotion("lure_horde_potion", new MobEffectInstance(ModEffects.LURE_HORDE, UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsLureEffect().getDurationForLureHordeEffect() * 20, 0));
        STRONG_LURE_HORDE_POTION = registerPotion("strong_lure_horde_potion", new MobEffectInstance(ModEffects.STRONG_LURE_HORDE, UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsLureEffect().getDurationForLureHordeEffect() * 20, 0));
        registerPotionRecipes();
    }

    private static void registerPotionRecipes() {
        FabricPotionBrewingBuilder.BUILD.register(builder -> {
            builder.registerPotionRecipe(Potions.AWKWARD, Ingredient.of(ModItems.SLIMY_ROTTEN_FLESH), ModPotions.LURE_HORDE_POTION);
            builder.registerPotionRecipe(ModPotions.LURE_HORDE_POTION, Ingredient.of(ModItems.SLIMY_ROTTEN_FLESH), ModPotions.STRONG_LURE_HORDE_POTION);
        });
    }
}
