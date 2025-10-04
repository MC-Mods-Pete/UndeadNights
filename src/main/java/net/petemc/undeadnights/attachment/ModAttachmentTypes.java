package net.petemc.undeadnights.attachment;

import com.mojang.serialization.Codec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.petemc.undeadnights.UndeadNights;

import java.util.function.Supplier;

public class ModAttachmentTypes {
    // Create the DeferredRegister for attachment types
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, UndeadNights.MOD_ID);

    // Serialization via map codec
    public static final Supplier<AttachmentType<Integer>> BLOCK_BREAKING = ATTACHMENT_TYPES.register(
            "block_breaking", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT.fieldOf("block_breaking")).build()
    );

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
