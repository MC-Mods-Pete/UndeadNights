package net.petemc.undeadnights.sound;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.petemc.undeadnights.UndeadNights;

public class UndeadNightsSounds {

    public static final SoundEvent HORDE_SCREAM = registerSoundEvent("horde_scream");

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(UndeadNights.MOD_ID, name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void registerSounds() {
        UndeadNights.LOGGER.info("Registering Sounds for " + UndeadNights.MOD_ID);
    }
}
