package dev.lapis256.apprep.data.provider.server.tags

import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.integration.megacells.common.init.AppRepMEGABlocks
import dev.lapis256.apprep.integration.megacells.common.init.AppRepMEGAItems
import dev.lapis256.apprep.integration.megacells.common.init.AppRepMEGATags
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.tags.ItemTagsProvider
import net.minecraft.world.level.block.Block
import net.minecraftforge.common.Tags
import net.minecraftforge.common.data.ExistingFileHelper
import java.util.concurrent.CompletableFuture

class AppRepItemTagsProvider(
    output: PackOutput,
    provider: CompletableFuture<HolderLookup.Provider>,
    blockTags: CompletableFuture<TagLookup<Block>>,
    helper: ExistingFileHelper,
) : ItemTagsProvider(output, provider, blockTags, AppliedReplicaticsAPI.MOD_ID, helper) {
    override fun addTags(provider: HolderLookup.Provider) {
        tag(AppRepTags.OPTIONAL_SKY_REPLICA_BLOCK).addOptional(AppRepMEGABlocks.SKY_REPLICA_BLOCK.id())
        tag(Tags.Items.INGOTS)
            .addOptional(AppRepMEGAItems.SKY_REPLICA_INGOT.id())

        tag(AppRepMEGATags.Items.SKY_REPLICA_INGOT)
            .addOptional(AppRepMEGAItems.SKY_REPLICA_INGOT.id())
    }
}
