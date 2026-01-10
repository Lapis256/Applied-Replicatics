package dev.lapis256.apprep.api.replication.matter_network

import dev.lapis256.apprep.api.asm.HookListener


interface MatterNetworkListener : HookListener {
    fun onAddedTanksSupplier()
    fun onRemovedTanksSupplier()
    fun onTankValueChanged()
}
