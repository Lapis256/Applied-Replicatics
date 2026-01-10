package dev.lapis256.apprep.api.titanium.network_element

import com.hrznstudio.titanium.block_network.Network
import dev.lapis256.apprep.api.asm.HookListener


interface NetworkElementListener : HookListener {
    fun onAddedNetwork(network: Network)
    fun onRemoveNetwork(network: Network)
}
