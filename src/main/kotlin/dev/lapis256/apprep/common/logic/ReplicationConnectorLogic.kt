package dev.lapis256.apprep.common.logic

import appeng.api.networking.GridFlags
import appeng.api.networking.IGridNode
import appeng.api.networking.IManagedGridNode
import appeng.api.networking.ticking.IGridTickable
import appeng.api.networking.ticking.TickRateModulation
import appeng.api.networking.ticking.TickingRequest
import appeng.api.storage.IStorageMounts
import appeng.api.storage.IStorageProvider
import appeng.api.util.AECableType
import appeng.me.storage.DelegatingMEInventory
import appeng.me.storage.NullInventory
import com.buuz135.replication.api.matter_fluid.IMatterTank
import com.buuz135.replication.api.network.IMatterTanksConsumer
import com.buuz135.replication.api.network.IMatterTanksSupplier
import com.buuz135.replication.network.MatterNetwork
import com.hrznstudio.titanium.block_network.Network
import com.hrznstudio.titanium.block_network.element.NetworkElement
import com.mojang.logging.LogUtils
import dev.lapis256.apprep.api.replication.matter_network.MatterNetworkListener
import dev.lapis256.apprep.api.replication.matter_network.addListener
import dev.lapis256.apprep.api.replication.matter_network.removeListener
import dev.lapis256.apprep.api.titanium.network_element.NetworkElementListener
import dev.lapis256.apprep.api.titanium.network_element.addListener
import dev.lapis256.apprep.api.titanium.network_element.removeListener
import dev.lapis256.apprep.common.ae2.storage.MatterNetworkStorage
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import org.slf4j.Logger


class ReplicationConnectorLogic(gridNode: IManagedGridNode, val host: ReplicationConnectorLogicHost) :
    IMatterTanksConsumer,
    IMatterTanksSupplier {

    companion object {
        val LOGGER: Logger = LogUtils.getLogger()
    }

    private var _priority: Int = 0
        set(value) {
            field = value
            host.saveChanges()
            remountMatterNetworkStorage()
        }

    fun setPriority(newValue: Int) {
        _priority = newValue
    }

    private class Ticker : IGridTickable {
        override fun getTickingRequest(node: IGridNode): TickingRequest {
            return TickingRequest(5, 120, true) // TODO: 調整する
        }

        override fun tickingRequest(node: IGridNode?, ticksSinceLastCall: Int): TickRateModulation {
            return TickRateModulation.SLEEP
        }
    }

    private inner class StorageProvider : IStorageProvider {
        override fun mountInventories(storageMounts: IStorageMounts) {
            if (mainNode.isOnline) {
                storageMounts.mount(delegatingStorage, priority)
            }
        }
    }

    val mainNode: IManagedGridNode = gridNode
        .setFlags(GridFlags.REQUIRE_CHANNEL)
        .addService(IGridTickable::class.java, Ticker())
        .addService(IStorageProvider::class.java, StorageProvider())

    inner class MatterNetworkListenerImpl : MatterNetworkListener {
        override fun onAddedTanksSupplier() {
            delegatingStorage.storage?.invalidateAll()
        }

        override fun onRemovedTanksSupplier() {
            delegatingStorage.storage?.invalidateAll()
        }

        override fun onTankValueChanged() {
            delegatingStorage.storage?.invalidateStacks()
        }
    }

    private val matterNetworkListener by lazy { MatterNetworkListenerImpl() }

    fun addMatterNetworkListener(network: MatterNetwork) {
        network.addListener(matterNetworkListener)
    }

    fun removeMatterNetworkListener(network: MatterNetwork) {
        network.removeListener(matterNetworkListener)
    }

    inner class NetworkElementListenerImpl : NetworkElementListener {
        override fun onAddedNetwork(network: Network) {
            val matterNetwork = network as? MatterNetwork
                ?: return LOGGER.error("Connected network is not MatterNetwork: {}", network)

            delegatingStorage.storage = MatterNetworkStorage(matterNetwork)
            addMatterNetworkListener(matterNetwork)
        }

        override fun onRemoveNetwork(network: Network) {
            delegatingStorage.storage = null

            val matterNetwork = network as? MatterNetwork
                ?: return LOGGER.error("Disconnected network is not MatterNetwork: {}", network)

            removeMatterNetworkListener(matterNetwork)
        }
    }

    private val networkElementListener by lazy { NetworkElementListenerImpl() }

    fun addNetworkElementListener(element: NetworkElement) {
        element.addListener(networkElementListener)
    }

    fun removeNetworkElementListener(element: NetworkElement) {
        element.removeListener(networkElementListener)
    }

    fun getCableConnectionType(@Suppress("unused") dir: Direction?): AECableType {
        return AECableType.SMART
    }

    fun notifyNeighbors() {
        if (mainNode.isActive) {
            mainNode.ifPresent { grid, node ->
                grid.tickManager.wakeDevice(node)
            }
        }

        host.getBlockEntity()?.invalidateCapabilities()
    }

    fun gridChanged() {
        notifyNeighbors()
    }

    fun writeToNBT(tag: CompoundTag, @Suppress("unused") registries: HolderLookup.Provider) {
        tag.putInt("priority", priority)
    }

    fun readFromNBT(tag: CompoundTag, @Suppress("unused") registries: HolderLookup.Provider) {
        notifyNeighbors()
        priority = tag.getInt("priority")
    }

    private fun remountMatterNetworkStorage() {
        IStorageProvider.requestUpdate(mainNode)
    }

    var wasOnline = false

    fun onMainNodeStateChanged() {
        val currentOnline: Boolean = mainNode.isOnline
        if (wasOnline != currentOnline) {
            wasOnline = currentOnline
            host.saveChanges()
            remountMatterNetworkStorage()
        }
    }

    class DelegatingMatterNetworkStorage : DelegatingMEInventory(NullInventory.of()) {
        var storage: MatterNetworkStorage?
            get() = delegate as? MatterNetworkStorage
            set(value) {
                delegate = value ?: NullInventory.of()
            }
    }

    val delegatingStorage = DelegatingMatterNetworkStorage()

    // IMatterTanksConsumer / IMatterTanksSupplier

    override fun getTanks(): List<IMatterTank> {
        return listOf() // MaterTankWrapperList() TODO: 実装する
    }

    override fun getPriority(): Int = _priority
}
