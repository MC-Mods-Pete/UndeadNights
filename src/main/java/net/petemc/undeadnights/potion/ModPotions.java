package net.petemc.undeadnights.potion;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.petemc.undeadnights.UndeadNights;
import net.petemc.undeadnights.config.MainConfig;
import net.petemc.undeadnights.effect.ModEffects;

public class ModPotions {
    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(BuiltInRegistries.POTION, UndeadNights.MOD_ID);

    public static final Holder<Potion>  LURE_HORDE_POTION = POTIONS.register("lure_horde_potion",
            () -> new Potion(new MobEffectInstance(ModEffects.LURE_HORDE, MainConfig.getDurationForLureHordeEffect() * 20, 0)));

    public static void register(IEventBus eventBus) {
        POTIONS.register(eventBus);
    }
}
