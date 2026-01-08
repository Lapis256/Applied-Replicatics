package dev.lapis256.apprep.common.init

import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.api.extension.registerType
import dev.lapis256.apprep.common.block.entity.MEReplicationConnector
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.entity.BlockEntityType
import net.neoforged.neoforge.registries.DeferredRegister


@Suppress("Unused")
object AppRepBlockEntities {
    val REGISTRY: DeferredRegister<BlockEntityType<*>> = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AppliedReplicaticsAPI.MOD_ID)


    val ME_REPLICATION_CONNECTOR = REGISTRY.registerType("me_replication_connector", ::MEReplicationConnector, AppRepBlocks.ME_REPLICATION_CONNECTOR)
}
