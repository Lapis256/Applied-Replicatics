package dev.lapis256.apprep.data.provider.server.tags

import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.common.init.AppRepBlocks
import dev.lapis256.apprep.integration.megacells.common.init.AppRepMEGABlocks
import dev.lapis256.apprep.integration.megacells.common.init.AppRepMEGATags
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.tags.BlockTags
import net.minecraftforge.common.Tags
import net.minecraftforge.common.data.BlockTagsProvider
import net.minecraftforge.common.data.ExistingFileHelper
import java.util.concurrent.CompletableFuture

class AppRepBlockTagsProvider(
    output: PackOutput,
    provider: CompletableFuture<HolderLookup.Provider>,
    helper: ExistingFileHelper,
) : BlockTagsProvider(output, provider, AppliedReplicaticsAPI.MOD_ID, helper) {
    override fun addTags(provider: HolderLookup.Provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .add(AppRepBlocks.REPLICATION_CONNECTOR.block())
            .addOptional(AppRepMEGABlocks.SKY_REPLICA_BLOCK.id())

        tag(AppRepMEGATags.Blocks.SKY_REPLICA_STORAGE_BLOCK)
            .addOptional(AppRepMEGABlocks.SKY_REPLICA_BLOCK.id())

        tag(Tags.Blocks.STORAGE_BLOCKS)
            .addOptional(AppRepMEGABlocks.SKY_REPLICA_BLOCK.id())
    }
}
