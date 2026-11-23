package dev.lapis256.apprep.data.provider.server.tags

import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item

object AppRepTags {
    val OPTIONAL_SKY_REPLICA_BLOCK = ItemTags.create(AppliedReplicaticsAPI.rl("sky_replica_block"))
    val SKY_STONE_DUST = parseTag("forge:dusts/sky_stone")
    val REPLICA_INGOT = parseTag("forge:ingots/replica")
    val REPLICA_STORAGE_BLOCK = parseTag("forge:storage_blocks/replica")

    private fun parseTag(tag: String): TagKey<Item> = ItemTags.create(ResourceLocation.parse(tag))
}
