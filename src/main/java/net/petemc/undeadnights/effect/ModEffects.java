package net.petemc.undeadnights.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.petemc.undeadnights.UndeadNights;

public class ModEffects {
    public static StatusEffect LURE_HORDE;

    public static StatusEffect registerLureHordeStatusEffect(String name) {
        return Registry.register(Registries.STATUS_EFFECT, new Identifier(UndeadNights.MOD_ID, name),
                new LureHordeMobsEffect(StatusEffectCategory.BENEFICIAL, 0x10ff10));
    }

    public static void registerEffects() {
        LURE_HORDE = registerLureHordeStatusEffect("lure_horde");
    }
}