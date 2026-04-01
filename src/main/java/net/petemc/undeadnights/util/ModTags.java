package net.petemc.undeadnights.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.petemc.undeadnights.UndeadNights;

public class ModTags {
    public static class EntityTypes {
        public static final TagKey<EntityType<?>> HORDE_MOBS = tag("horde_mobs");

        private static TagKey<EntityType<?>> tag(String name) {
            return TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(UndeadNights.MOD_ID, name));
        }
    }
}
