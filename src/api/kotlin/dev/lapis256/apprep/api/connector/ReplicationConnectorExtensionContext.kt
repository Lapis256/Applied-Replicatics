package dev.lapis256.apprep.api.connector

import appeng.api.networking.IManagedGridNode
import appeng.api.networking.security.IActionSource
import appeng.api.upgrades.IUpgradeInventory
import com.buuz135.replication.network.MatterNetwork


interface ReplicationConnectorExtensionContext {
    val managedNode: IManagedGridNode
    val upgrades: IUpgradeInventory
    val actionSource: IActionSource
    val matterNetwork: MatterNetwork?

    fun saveChanges()
}
