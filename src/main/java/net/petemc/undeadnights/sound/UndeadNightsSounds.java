package net.petemc.undeadnights.sound;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.petemc.undeadnights.UndeadNights;

public class UndeadNightsSounds {

    public static final SoundEvent HORDE_SCREAM = registerSoundEvent("horde_scream");

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = Identifier.of(UndeadNights.MOD_ID, name);
        return Registry.register(Registry.SOUND_EVENT, id, new SoundEvent(id));
    }

    public static void registerSounds() {
        UndeadNights.LOGGER.info("Registering Sounds for " + UndeadNights.MOD_ID);
    }
}
