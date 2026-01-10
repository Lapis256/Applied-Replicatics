package dev.lapis256.apprep.mixin_impl.titanium

import com.hrznstudio.titanium.block_network.Network
import dev.lapis256.apprep.api.asm.HookListenerHolderForImpl
import dev.lapis256.apprep.api.titanium.network_element.NetworkElementListener


class MixinImplNetworkElement : HookListenerHolderForImpl<NetworkElementListener>() {
    fun onAddedNetwork(network: Network) {
        fire { it.onAddedNetwork(network) }
    }

    fun onRemoveNetwork(network: Network) {
        fire { it.onRemoveNetwork(network) }
    }
}
