package dev.lapis256.apprep.common.init

import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.api.extension.BlockDefinition
import dev.lapis256.apprep.api.extension.register
import dev.lapis256.apprep.common.block.ReplicationConnectorBlock
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraftforge.registries.DeferredRegister

@Suppress("Unused")
object AppRepBlocks {
    val REGISTRY: DeferredRegister<Block> = DeferredRegister.create(Registries.BLOCK, AppliedReplicaticsAPI.MOD_ID)

    val BLOCKS = mutableListOf<BlockDefinition<*>>()

    val REPLICATION_CONNECTOR = register("ME Replication Connector", "replication_connector", ::ReplicationConnectorBlock)

    private fun <BLOCK : Block> register(
        englishName: String,
        name: String,
        supplier: () -> BLOCK,
        itemSupplier: ((Block, Item.Properties) -> BlockItem)? = null,
    ) = REGISTRY
        .register(englishName, name, supplier, AppRepItems.REGISTRY, itemSupplier)
        .also(AppRepCreativeTab::registerItem)
        .also(BLOCKS::add)
}
