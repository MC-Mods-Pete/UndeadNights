package net.petemc.undeadnights.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.petemc.undeadnights.UndeadNights;

public class ModEffects {
    public static RegistryEntry<StatusEffect> LURE_HORDE;
    public static RegistryEntry<StatusEffect> STRONG_LURE_HORDE;

    private static RegistryEntry<StatusEffect> registerStatusEffect(String name, StatusEffect statusEffect) {
        return Registry.registerReference(Registries.STATUS_EFFECT, Identifier.of(UndeadNights.MOD_ID, name), statusEffect);
    }

    public static void registerEffects() {
        LURE_HORDE = registerStatusEffect("lure_horde", new LureHordeMobsEffect(StatusEffectCategory.HARMFUL, 0x10ff10));
        STRONG_LURE_HORDE = registerStatusEffect("strong_lure_horde", new StrongLureHordeMobsEffect(StatusEffectCategory.HARMFUL, 0x10ff10));
    }
}