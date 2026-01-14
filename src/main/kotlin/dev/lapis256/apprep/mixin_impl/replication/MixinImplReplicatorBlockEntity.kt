package dev.lapis256.apprep.mixin_impl.replication

import com.buuz135.replication.api.task.IReplicationTask
import com.buuz135.replication.block.tile.ReplicatorBlockEntity
import com.llamalad7.mixinextras.sugar.ref.LocalRef
import dev.lapis256.apprep.common.logic.ReplicationConnectorLogicHost


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
    host.logic.insertReplicatorResult(task.replicatingStack.copyWithCount(1))
    return true
}
