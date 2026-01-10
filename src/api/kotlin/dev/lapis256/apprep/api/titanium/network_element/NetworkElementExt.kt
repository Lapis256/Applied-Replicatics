package dev.lapis256.apprep.api.titanium.network_element

import com.hrznstudio.titanium.block_network.element.NetworkElement


fun NetworkElement.addListener(listener: NetworkElementListener): Boolean =
    (this as NetworkElementListenerHolder).addListener(listener)

fun NetworkElement.removeListener(listener: NetworkElementListener): Boolean =
    (this as NetworkElementListenerHolder).removeListener(listener)
