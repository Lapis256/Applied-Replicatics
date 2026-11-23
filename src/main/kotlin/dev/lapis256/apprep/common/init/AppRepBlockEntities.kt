package dev.lapis256.apprep.common.init

import appeng.block.AEBaseEntityBlock
import appeng.blockentity.AEBaseBlockEntity
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.api.extension.BlockDefinition
import dev.lapis256.apprep.api.extension.registerType
import dev.lapis256.apprep.common.block.entity.ReplicationConnectorBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.registries.DeferredRegister

@Suppress("Unused")
object AppRepBlockEntities {
    val REGISTRY: DeferredRegister<BlockEntityType<*>> = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AppliedReplicaticsAPI.MOD_ID)

    val REPLICATION_CONNECTOR = registerType("replication_connector", ::ReplicationConnectorBlockEntity, AppRepBlocks.REPLICATION_CONNECTOR)

    private inline fun <reified ENTITY : AEBaseBlockEntity, BLOCK : AEBaseEntityBlock<ENTITY>> registerType(
        name: String,
        crossinline factory: (BlockEntityType<ENTITY>, BlockPos, BlockState) -> ENTITY,
        vararg deferredBlocks: BlockDefinition<BLOCK>,
    ) = REGISTRY.registerType(name, factory, *deferredBlocks)
}
