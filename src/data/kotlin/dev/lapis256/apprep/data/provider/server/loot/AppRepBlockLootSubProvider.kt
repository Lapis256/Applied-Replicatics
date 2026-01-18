package dev.lapis256.apprep.data.provider.server.loot

import dev.apprep.apprep.integration.megacells.common.init.AppRepMEGABlocks
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.common.init.AppRepBlocks
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.loot.BlockLootSubProvider
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.level.block.Block


class AppRepBlockLootSubProvider(provider: HolderLookup.Provider) :
    BlockLootSubProvider(emptySet(), FeatureFlags.DEFAULT_FLAGS, provider) {

    override fun getKnownBlocks(): Iterable<Block> {
        return BuiltInRegistries.BLOCK.filter {
            it.lootTable.location().namespace == AppliedReplicaticsAPI.MOD_ID &&
                    it != AppRepMEGABlocks.SKY_REPLICA_BLOCK.block() // neoforge:mod_loaded ありのファイルを生成できないため
        }
    }

    override fun generate() {
        dropSelf(AppRepBlocks.REPLICATION_CONNECTOR.block())

//        dropSelf(AppRepMEGABlocks.SKY_REPLICA_BLOCK.block())
    }
}
