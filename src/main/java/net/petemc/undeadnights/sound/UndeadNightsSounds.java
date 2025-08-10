package net.petemc.undeadnights.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.petemc.undeadnights.UndeadNights;

import java.util.function.Supplier;

public class UndeadNightsSounds {
        public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, UndeadNights.MOD_ID);

    public static final Supplier<SoundEvent> HORDE_SCREAM = registerSoundEvent("horde_scream");

    private static Supplier<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.tryBuild(UndeadNights.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(BusGroup modBusGroup) {
        SOUND_EVENTS.register(modBusGroup);
    }
}
