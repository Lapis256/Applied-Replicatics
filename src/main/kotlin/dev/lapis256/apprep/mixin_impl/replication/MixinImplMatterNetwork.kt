package dev.lapis256.apprep.mixin_impl.replication

import com.buuz135.replication.api.task.IReplicationTask
import com.buuz135.replication.api.task.ReplicationTask
import dev.lapis256.apprep.api.asm.HookListenerHolderForImpl
import dev.lapis256.apprep.api.replication.matter_network.MatterNetworkListener
import dev.lapis256.apprep.api.replication.task.internalMatterStacks
import dev.lapis256.apprep.common.logic.ReplicationConnectorLogicHost
import net.minecraft.world.level.Level


class MixinImplMatterNetwork : HookListenerHolderForImpl<MatterNetworkListener>() {
    fun onAddedTanksSupplier() {
        fire { it.onAddedTanksSupplier() }
    }

    fun onRemovedTanksSupplier(removed: Boolean) {
        if (removed) {
            fire { it.onRemovedTanksSupplier() }
        }
    }

    fun onTankValueChanged() {
        fire { it.onTankValueChanged() }
    }

    fun onAddedChipSupplier() {
        fire { it.onAddedChipSupplier() }
    }

    fun onRemovedChipSupplier(removed: Boolean) {
        if (removed) {
            fire { it.onRemovedChipSupplier() }
        }
    }

    fun onChipValuesChanged() {
        fire { it.onChipValuesChanged() }
    }
}


/**
 * 自動クラフトのキャンセル時に、内部タンクに保持されているマターをネットワークに返却します
 */
fun returnInternalMatterStacks(level: Level, task: IReplicationTask) {
    val replicationTask = task as? ReplicationTask ?: return
    if (replicationTask.internalMatterStacks.isEmpty()) {
        return
    }

    val host = level.getBlockEntity(replicationTask.source)
        as? ReplicationConnectorLogicHost ?: return

    host.logic.returnMatterStacksToNetwork(replicationTask.internalMatterStacks)
}
