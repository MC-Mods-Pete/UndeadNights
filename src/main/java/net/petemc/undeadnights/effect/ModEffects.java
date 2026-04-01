package net.petemc.undeadnights.effect;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.petemc.undeadnights.UndeadNights;

public class ModEffects {
    public static Holder<MobEffect> LURE_HORDE;
    public static Holder<MobEffect> STRONG_LURE_HORDE;

    private static Holder<MobEffect> registerStatusEffect(String name, MobEffect mobEffect) {
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Identifier.fromNamespaceAndPath(UndeadNights.MOD_ID, name), mobEffect);
    }

    public static void registerEffects() {
        LURE_HORDE = registerStatusEffect("lure_horde", new LureHordeMobsEffect(MobEffectCategory.HARMFUL, 0x10ff10));
        STRONG_LURE_HORDE = registerStatusEffect("strong_lure_horde", new StrongLureHordeMobsEffect(MobEffectCategory.HARMFUL, 0x10ff10));
    }
}