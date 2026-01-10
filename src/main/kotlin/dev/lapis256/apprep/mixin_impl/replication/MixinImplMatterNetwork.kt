package dev.lapis256.apprep.mixin_impl.replication

import dev.lapis256.apprep.api.asm.HookListenerHolderForImpl
import dev.lapis256.apprep.api.replication.matter_network.MatterNetworkListener


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
}
