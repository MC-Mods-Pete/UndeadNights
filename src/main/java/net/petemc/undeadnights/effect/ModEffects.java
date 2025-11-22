package net.petemc.undeadnights.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.petemc.undeadnights.UndeadNights;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, UndeadNights.MOD_ID);

    public static final RegistryObject<MobEffect> LURE_HORDE = MOB_EFFECTS.register("lure_horde",
            () -> new LureHordeMobsEffect(MobEffectCategory.HARMFUL, 0x10ff10));

    public static final RegistryObject<MobEffect> STRONG_LURE_HORDE = MOB_EFFECTS.register("strong_lure_horde",
            () -> new StrongLureHordeMobsEffect(MobEffectCategory.HARMFUL, 0x13ff89));

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}