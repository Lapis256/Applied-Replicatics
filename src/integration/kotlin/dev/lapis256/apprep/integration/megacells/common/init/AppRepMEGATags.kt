package dev.lapis256.apprep.integration.megacells.common.init

import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block

object AppRepMEGATags {
    object Items {
        val SKY_REPLICA_INGOT = create("forge:ingots/sky_replica")

        private fun create(tag: String): TagKey<Item> = ItemTags.create(ResourceLocation.parse(tag))
    }

    object Blocks {
        val SKY_REPLICA_STORAGE_BLOCK = create("forge:storage_blocks/sky_replica")

        private fun create(tag: String): TagKey<Block> = BlockTags.create(ResourceLocation.parse(tag))
    }
}
