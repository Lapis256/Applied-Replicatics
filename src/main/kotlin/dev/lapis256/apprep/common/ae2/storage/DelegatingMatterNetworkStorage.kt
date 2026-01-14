package dev.lapis256.apprep.common.ae2.storage

import appeng.me.storage.DelegatingMEInventory
import appeng.me.storage.NullInventory


class DelegatingMatterNetworkStorage : DelegatingMEInventory(NullInventory.of()) {
    var storage: MatterNetworkStorage?
        get() = delegate as? MatterNetworkStorage
        set(value) {
            delegate = value ?: NullInventory.of()
        }
}
