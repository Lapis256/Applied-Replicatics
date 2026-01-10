package dev.lapis256.apprep.api.replication.matter_network

import com.buuz135.replication.network.MatterNetwork


fun MatterNetwork.addListener(listener: MatterNetworkListener): Boolean =
    (this as MatterNetworkListenerHolder).addListener(listener)

fun MatterNetwork.removeListener(listener: MatterNetworkListener): Boolean =
    (this as MatterNetworkListenerHolder).removeListener(listener)
