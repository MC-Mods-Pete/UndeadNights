package net.petemc.undeadnights.potion;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.effect.ModEffects;

public class ModPotions {
    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(ForgeRegistries.POTIONS, UndeadNights.MOD_ID);

    public static final RegistryObject<Potion> LURE_HORDE_POTION = POTIONS.register("lure_horde_potion",
            () -> new Potion("lure_horde_potion", new MobEffectInstance(ModEffects.LURE_HORDE.getHolder().get(), UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsLureEffect().getDurationForLureHordeEffect() * 20, 0)));

    public static final RegistryObject<Potion> STRONG_LURE_HORDE_POTION = POTIONS.register("strong_lure_horde_potion",
            () -> new Potion("strong_lure_horde_potion", new MobEffectInstance(ModEffects.STRONG_LURE_HORDE.getHolder().get(), UndeadNights.difficultyConfig.getCurrentDifficultyLevel().getDifficultySettingsLureEffect().getDurationForLureHordeEffect() * 2 * 20, 0)));

    public static void register(BusGroup modBusGroup) {
        POTIONS.register(modBusGroup);
    }
}
