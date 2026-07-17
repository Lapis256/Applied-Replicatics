package dev.lapis256.apprep.mixin_impl.replication

import com.buuz135.replication.api.task.IReplicationTask
import com.buuz135.replication.api.task.ReplicationTask
import com.buuz135.replication.block.tile.ReplicatorBlockEntity
import com.llamalad7.mixinextras.sugar.ref.LocalRef
import dev.lapis256.apprep.api.replication.task.internalMatterStacks
import dev.lapis256.apprep.common.logic.ReplicationConnectorLogicHost
import dev.lapis256.apprep.mixin.replication.AccessorNetworkBlockEntity


fun keepTaskOnChunkUnload(
    original: IReplicationTask?,
    blockEntity: ReplicatorBlockEntity
): IReplicationTask? {
    val task = original as? ReplicationTask ?: return original
    val isUnloaded = (blockEntity as AccessorNetworkBlockEntity).isUnloaded

    return if (isUnloaded && task.internalMatterStacks.isNotEmpty()) null else original
}

fun setConnectorHost(
    original: Boolean,
    blockEntity: ReplicatorBlockEntity,
    hostRef: LocalRef<ReplicationConnectorLogicHost>,
    task: IReplicationTask
): Boolean {
    val level = blockEntity.getLevel() ?: return original
    val entity = level.getBlockEntity(task.source)
        as? ReplicationConnectorLogicHost ?: return original

    hostRef.set(entity)
    return true
}

fun insertResultToConnector(host: ReplicationConnectorLogicHost?, task: IReplicationTask): Boolean {
    if (host == null) {
        return false
    }
    return host.logic.insertReplicatorResult(task.replicatingStack.copyWithCount(1)) == 1L
}
