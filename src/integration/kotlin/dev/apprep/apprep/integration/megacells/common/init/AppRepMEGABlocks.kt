package dev.apprep.apprep.integration.megacells.common.init

import appeng.block.AEBaseBlockItem
import appeng.core.definitions.BlockDefinition
import appeng.decorative.AEDecorativeBlock
import com.buuz135.replication.ReplicationRegistry
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.api.extension.register
import dev.lapis256.apprep.common.init.AppRepCreativeTab
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.neoforged.neoforge.registries.DeferredRegister


object AppRepMEGABlocks {
    val REGISTRY: DeferredRegister.Blocks = DeferredRegister.createBlocks(AppliedReplicaticsAPI.MOD_ID)

    val BLOCKS = mutableListOf<BlockDefinition<*>>()

    val SKY_REPLICA_BLOCK = register(
        "Sky Replica Block",
        "sky_replica_block",
        {
            AEDecorativeBlock(
                BlockBehaviour.Properties.ofFullCopy(ReplicationRegistry.Blocks.REPLICA_BLOCK.get())
                    .strength(4.5F, 6.0F)
            )
        },
        { b, p -> AEBaseBlockItem(b, p.fireResistant()) }
    )

    private fun <BLOCK : Block> register(
        englishName: String,
        name: String,
        supplier: () -> BLOCK,
        itemSupplier: ((Block, Item.Properties) -> BlockItem)? = null
    ) = REGISTRY
        .register(englishName, name, supplier, AppRepMEGAItems.REGISTRY, itemSupplier)
        .also(AppRepCreativeTab::registerItem)
        .also(BLOCKS::add)
}
