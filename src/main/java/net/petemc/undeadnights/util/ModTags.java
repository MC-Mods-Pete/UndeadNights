package net.petemc.undeadnights.util;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
import net.petemc.undeadnights.UndeadNights;

public class ModTags {
    public static class EntityTypes {
        public static final TagKey<EntityType<?>> HORDE_MOBS = tag("horde_mobs");

        private static TagKey<EntityType<?>> tag(String name) {
            return TagKey.create(ForgeRegistries.ENTITY_TYPES.getRegistryKey(), new ResourceLocation(UndeadNights.MOD_ID, name));
        }
    }
}
