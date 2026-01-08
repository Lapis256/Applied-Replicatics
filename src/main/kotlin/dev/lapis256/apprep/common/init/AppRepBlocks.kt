package dev.lapis256.apprep.common.init

import appeng.core.definitions.BlockDefinition
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.api.extension.register
import dev.lapis256.apprep.common.block.MEReplicationConnectorBlock
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.neoforged.neoforge.registries.DeferredRegister


@Suppress("Unused")
object AppRepBlocks {
    val REGISTRY: DeferredRegister.Blocks = DeferredRegister.createBlocks(AppliedReplicaticsAPI.MOD_ID)

    val BLOCKS = mutableListOf<BlockDefinition<*>>()


    val ME_REPLICATION_CONNECTOR = register("ME Replication Connector", "me_replication_connector", ::MEReplicationConnectorBlock)


    private fun <BLOCK : Block> register(
        englishName: String,
        name: String,
        supplier: () -> BLOCK,
        itemSupplier: ((Block, Item.Properties) -> BlockItem)? = null
    ) = REGISTRY
        .register(englishName, name, supplier, AppRepItems.REGISTRY, itemSupplier)
        .also(AppRepCreativeTab::registerItem)
        .also(BLOCKS::add)
}
