package net.petemc.undeadnights.util;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.petemc.undeadnights.UndeadNights;

public class ModTags {
    public static class EntityTypes {
        public static final TagKey<EntityType<?>> HORDE_MOBS = tag("horde_mobs");

        private static TagKey<EntityType<?>> tag(String name) {
            return TagKey.of(Registries.ENTITY_TYPE.getKey(), Identifier.of(UndeadNights.MOD_ID, name));
        }
    }
}
