package dev.lapis256.apprep.integration.megacells.common.init

import appeng.block.AEBaseBlockItem
import appeng.decorative.AEDecorativeBlock
import com.buuz135.replication.ReplicationRegistry
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.api.extension.BlockDefinition
import dev.lapis256.apprep.api.extension.register
import dev.lapis256.apprep.common.init.AppRepCreativeTab
import dev.lapis256.apprep.integration.megacells.common.MEGACellsIntegration
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraftforge.registries.DeferredRegister

object AppRepMEGABlocks {
    val REGISTRY: DeferredRegister<Block> = DeferredRegister.create(Registries.BLOCK, AppliedReplicaticsAPI.MOD_ID)

    val BLOCKS = mutableListOf<BlockDefinition<*>>()

    val SKY_REPLICA_BLOCK =
        register(
            "Sky Replica Block",
            "sky_replica_block",
            {
                AEDecorativeBlock(
                    BlockBehaviour.Properties
                        .copy(ReplicationRegistry.Blocks.REPLICA_BLOCK.get())
                        .strength(4.5F, 6.0F),
                )
            },
            { b, p -> AEBaseBlockItem(b, p.fireResistant()) },
        )

    private fun <BLOCK : Block> register(
        englishName: String,
        name: String,
        supplier: () -> BLOCK,
        itemSupplier: ((Block, Item.Properties) -> BlockItem)? = null,
    ) = REGISTRY
        .register(englishName, name, supplier, MEGACellsIntegration.ITEM_REGISTRY, itemSupplier)
        .also(AppRepCreativeTab::registerItem)
        .also(BLOCKS::add)
}
