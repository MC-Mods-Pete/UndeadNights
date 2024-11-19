package net.petemc.undeadnights.sound;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.petemc.undeadnights.UndeadNights;

public class UndeadNightsSounds {

    public static final SoundEvent HORDE_SCREAM = registerSoundEvent("horde_scream");

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = Identifier.of(UndeadNights.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerSounds() {
        UndeadNights.LOGGER.info("Registering Sounds for " + UndeadNights.MOD_ID);
    }
}
