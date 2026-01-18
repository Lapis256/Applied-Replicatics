package dev.lapis256.apprep.data.provider.server.tags

import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item


object AppRepTags {
    val REPLICA_INGOT = parseTag("c:ingots/replica")
    val REPLICA_STORAGE_BLOCK = parseTag("c:storage_blocks/replica")

    private fun parseTag(tag: String): TagKey<Item> = ItemTags.create(ResourceLocation.parse(tag))
}
