package dev.lapis256.apprep.data.provider.server.loot

import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.common.init.AppRepBlocks
import dev.lapis256.apprep.integration.megacells.common.init.AppRepMEGABlocks
import net.minecraft.data.loot.BlockLootSubProvider
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.level.block.Block
import net.minecraftforge.registries.ForgeRegistries

class AppRepBlockLootSubProvider : BlockLootSubProvider(emptySet(), FeatureFlags.DEFAULT_FLAGS) {
    override fun getKnownBlocks(): Iterable<Block> =
        ForgeRegistries.BLOCKS.values.filter {
            it.lootTable.namespace == AppliedReplicaticsAPI.MOD_ID
        }

    override fun generate() {
        dropSelf(AppRepBlocks.REPLICATION_CONNECTOR.block())
        dropSelf(AppRepMEGABlocks.SKY_REPLICA_BLOCK.block())
    }
}
