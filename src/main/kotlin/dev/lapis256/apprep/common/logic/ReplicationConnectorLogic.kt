package dev.lapis256.apprep.common.logic

import appeng.api.networking.GridFlags
import appeng.api.networking.IManagedGridNode
import appeng.api.networking.IStackWatcher
import appeng.api.networking.security.IActionSource
import appeng.api.networking.storage.IStorageWatcherNode
import appeng.api.stacks.AEKey
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
import dev.lapis256.apprep.api.ae2.stack.MatterKey
import dev.lapis256.apprep.api.replication.matter_network.MatterNetworkListener
import dev.lapis256.apprep.api.replication.matter_network.addListener
import dev.lapis256.apprep.api.replication.matter_network.removeListener
import dev.lapis256.apprep.api.replication.util.MATTER_TYPES
import dev.lapis256.apprep.api.titanium.network_element.NetworkElementListener
import dev.lapis256.apprep.api.titanium.network_element.addListener
import dev.lapis256.apprep.api.titanium.network_element.removeListener
import dev.lapis256.apprep.api.util.ResettableLazy
import dev.lapis256.apprep.common.ae2.storage.MatterNetworkStorage
import dev.lapis256.apprep.common.replication.MENetworkMatterTankList
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

    class DelegatingMatterNetworkStorage : DelegatingMEInventory(NullInventory.of()) {
        var storage: MatterNetworkStorage?
            get() = delegate as? MatterNetworkStorage
            set(value) {
                delegate = value ?: NullInventory.of()
            }
    }

    val delegatingStorage = DelegatingMatterNetworkStorage()

    private inner class StorageProvider : IStorageProvider {
        override fun mountInventories(storageMounts: IStorageMounts) {
            if (mainNode.isOnline) {
                storageMounts.mount(delegatingStorage, priority)
            }
        }
    }

    private lateinit var stackWatcher: IStackWatcher
    inner class StackWatcher : IStorageWatcherNode {
        override fun updateWatcher(newWatcher: IStackWatcher) {
            stackWatcher = newWatcher

            MATTER_TYPES.forEach {
                val what = MatterKey.of(it)
                stackWatcher.add(what)
            }
        }

        override fun onStackChange(what: AEKey, amount: Long) {
            tanks.updateCache(what, amount)
        }
    }

    val mainNode: IManagedGridNode = gridNode
        .setFlags(GridFlags.REQUIRE_CHANNEL)
        .addService(IStorageProvider::class.java, StorageProvider())
        .addService(IStorageWatcherNode::class.java, StackWatcher())

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
        _tanks.reset()
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

    // IMatterTanksConsumer / IMatterTanksSupplier

    private val _tanks = ResettableLazy {
        val grid = mainNode.grid ?: return@ResettableLazy MENetworkMatterTankList.empty()
        val inventory = grid.storageService.cachedInventory
        val cachedMatters = MATTER_TYPES.associateWith { inventory[MatterKey.of(it)] }
        MENetworkMatterTankList(cachedMatters, grid.storageService.inventory, IActionSource.ofMachine(mainNode::getNode))
    }
    private val tanks by _tanks

    override fun getTanks(): List<IMatterTank> {
        return tanks
    }

    override fun getPriority(): Int = _priority
}
