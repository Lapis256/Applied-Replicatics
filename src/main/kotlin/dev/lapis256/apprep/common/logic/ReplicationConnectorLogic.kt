package dev.lapis256.apprep.common.logic

import appeng.api.config.Actionable
import appeng.api.crafting.IPatternDetails
import appeng.api.networking.GridFlags
import appeng.api.networking.IGridNode
import appeng.api.networking.IManagedGridNode
import appeng.api.networking.IStackWatcher
import appeng.api.networking.crafting.ICraftingProvider
import appeng.api.networking.security.IActionSource
import appeng.api.networking.storage.IStorageWatcherNode
import appeng.api.networking.ticking.IGridTickable
import appeng.api.networking.ticking.TickRateModulation
import appeng.api.networking.ticking.TickingRequest
import appeng.api.stacks.AEItemKey
import appeng.api.stacks.AEKey
import appeng.api.stacks.KeyCounter
import appeng.api.storage.IStorageMounts
import appeng.api.storage.IStorageProvider
import appeng.api.util.AECableType
import com.buuz135.replication.api.IMatterType
import com.buuz135.replication.api.matter_fluid.IMatterTank
import com.buuz135.replication.api.network.IMatterTanksConsumer
import com.buuz135.replication.api.network.IMatterTanksSupplier
import com.buuz135.replication.api.pattern.IMatterPatternHolder
import com.buuz135.replication.network.MatterNetwork
import com.hrznstudio.titanium.block_network.Network
import com.hrznstudio.titanium.block_network.element.NetworkElement
import com.mojang.logging.LogUtils
import com.mojang.serialization.Codec
import dev.lapis256.apprep.api.ae2.stack.MatterKey
import dev.lapis256.apprep.api.extension.getCodec
import dev.lapis256.apprep.api.extension.putCodec
import dev.lapis256.apprep.api.replication.matter_network.MatterNetworkListener
import dev.lapis256.apprep.api.replication.matter_network.addListener
import dev.lapis256.apprep.api.replication.matter_network.removeListener
import dev.lapis256.apprep.api.replication.task.MEReplicationTask
import dev.lapis256.apprep.api.replication.util.MATTER_TYPES
import dev.lapis256.apprep.api.replication.util.addTask
import dev.lapis256.apprep.api.titanium.network_element.NetworkElementListener
import dev.lapis256.apprep.api.titanium.network_element.addListener
import dev.lapis256.apprep.api.titanium.network_element.removeListener
import dev.lapis256.apprep.api.util.ResettableLazy
import dev.lapis256.apprep.common.ae2.crafting.ReplicationPattern
import dev.lapis256.apprep.common.ae2.storage.DelegatingMatterNetworkStorage
import dev.lapis256.apprep.common.ae2.storage.MatterNetworkStorage
import dev.lapis256.apprep.common.replication.MENetworkMatterTankList
import dev.lapis256.apprep.common.storage.ReplicationConnectorReturnInventory
import it.unimi.dsi.fastutil.objects.Object2LongMap
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.core.UUIDUtil
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import org.slf4j.Logger
import java.util.*


class ReplicationConnectorLogic(gridNode: IManagedGridNode, val host: ReplicationConnectorLogicHost) :
    IMatterTanksConsumer,
    IMatterTanksSupplier {

    companion object {
        val LOGGER: Logger = LogUtils.getLogger()

        val PUSHED_REPLICATION_TASKS_CODEC: Codec<ObjectOpenHashSet<UUID>> =
            UUIDUtil.CODEC.listOf().fieldOf("pushed_replication_tasks").codec().xmap(
                { ObjectOpenHashSet(it) },
                { it.toList() }
            )

        val PENDING_TASK_CODEC: Codec<PendingTask> = PendingTask.CODEC.fieldOf("pending_task").codec()
    }

    private val source: IActionSource get() = IActionSource.ofMachine(mainNode::getNode)

    private var _priority: Int = 0
        set(value) {
            field = value
            host.saveChanges()
            remountMatterNetworkStorage()
        }

    fun setPriority(newValue: Int) {
        _priority = newValue
    }

    private var pendingTask: PendingTask? = null
    private var pushedReplicationTasks: ObjectOpenHashSet<UUID> = ObjectOpenHashSet()

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

    val returnInventory = ReplicationConnectorReturnInventory {
        alertDevice()
        host.saveChanges()
    }

    fun insertReplicatorResult(itemStack: ItemStack): Long {
        return returnInventory.insert(
            AEItemKey.of(itemStack),
            itemStack.count.toLong(),
            Actionable.MODULATE,
            source
        )
    }

    fun addDrops(level: Level, pos: BlockPos, drops: MutableList<ItemStack>) {
        returnInventory.addDrops(level, pos, drops)
    }

    fun clearContent() {
        returnInventory.clear()
    }

    private val _patterns = ResettableLazy {
        val matterNetwork = host.matterNetwork ?: return@ResettableLazy emptyList()
        matterNetwork.chipSuppliers
            .asSequence()
            .filter { it.level.isLoaded(it.pos) }
            .mapNotNull { it.level.getBlockEntity(it.pos) }
            .flatMap {
                val level = it.level ?: return@flatMap emptyList()
                if (it is IMatterPatternHolder<*>) {
                    @Suppress("UNCHECKED_CAST")
                    (it as IMatterPatternHolder<BlockEntity>).getPatterns(level, it).toList()
                } else {
                    emptyList()
                }
            }
            .map { ReplicationPattern(it.stack) }
            .toList()
    }
    private val patterns by _patterns

    private fun updatePatterns() {
        _patterns.reset()
        ICraftingProvider.requestUpdate(mainNode)
    }

    inner class CraftingProvider : ICraftingProvider {

        private fun canPushNextPattern(): Boolean {
            val matterNetwork = host.matterNetwork ?: return false
            val taskManager = matterNetwork.taskManager

            val iterator = pushedReplicationTasks.iterator()
            while (iterator.hasNext()) {
                val taskUuid = iterator.next()
                if (!taskManager.pendingTasks.containsKey(taskUuid.toString())) {
                    iterator.remove()
                    continue
                }

                val task = taskManager.pendingTasks[taskUuid.toString()] ?: continue
                val completedPercent = task.currentAmount.toDouble() / task.totalAmount.toDouble()
                if (completedPercent <= 0.25) {
                    return false
                }
            }
            return true
        }

        override fun getAvailablePatterns(): List<IPatternDetails> = patterns

        override fun pushPattern(patternDetails: IPatternDetails, inputHolder: Array<KeyCounter>): Boolean {
            if (!canPushNextPattern()) {
                return false
            }

            val output = patternDetails.outputs[0] ?: return false
            val item = output.what as? AEItemKey ?: return false

            if (pendingTask != null && pendingTask?.output != item) {
                return false
            }

            if (pendingTask == null) {
                pendingTask = PendingTask(inputHolder, item)
            }
            pendingTask!!.increaseProcessingCount()

            alertDevice()

            return true
        }

        override fun isBusy(): Boolean {
            return !canPushNextPattern()
        }
    }

    inner class Ticker : IGridTickable {
        override fun getTickingRequest(node: IGridNode) =
            TickingRequest(5, 120, true)

        override fun tickingRequest(node: IGridNode, ticksSinceLastCall: Int): TickRateModulation {
            if (!shouldTick()) {
                return TickRateModulation.SLEEP
            }

            val taskPushed = pushPendingTask()
            val inserted = insertReturnedItems()

            if (taskPushed || inserted) {
                return TickRateModulation.FASTER
            }

            return if (shouldTick()) {
                TickRateModulation.FASTER
            } else {
                TickRateModulation.SLEEP
            }
        }

        private fun shouldTick(): Boolean {
            if (pendingTask != null) {
                return true
            }
            if (!returnInventory.isEmpty()) {
                return true
            }
            return false
        }

        private fun pushPendingTask(): Boolean {
            val toPushTask = pendingTask ?: return false

            val matterNetwork = host.matterNetwork ?: return false
            val level = host.matterNetworkElement?.level as? ServerLevel ?: return false
            val pos = host.matterNetworkElement?.pos ?: return false

            val extracted = Object2LongOpenHashMap<IMatterType>()
            toPushTask.input.forEach {
                val what = it.key as? MatterKey ?: return@forEach
                extracted[what.type] = it.longValue * toPushTask.count
            }

            val task = MEReplicationTask.create(extracted, toPushTask.output, toPushTask.count, pos)

            matterNetwork.taskManager.addTask(task)
            matterNetwork.onTaskValueChanged(task, level)

            pushedReplicationTasks.add(task.uuid)
            pendingTask = null

            return true
        }

        private fun insertReturnedItems(): Boolean {
            val inventory = mainNode.grid?.storageService?.inventory ?: return false
            return returnInventory.returnIntoStorage(inventory, source)
        }
    }

    fun returnMatterStacksToNetwork(stacks: Object2LongMap<IMatterType>) {
        val inventory = mainNode.grid?.storageService?.inventory ?: return
        stacks.forEach { (type, amount) ->
            val key = MatterKey.of(type)
            inventory.insert(key, amount, Actionable.MODULATE, source)
        }
        stacks.clear()
    }

    val mainNode: IManagedGridNode = gridNode
        .setFlags(GridFlags.REQUIRE_CHANNEL)
        .addService(IStorageProvider::class.java, StorageProvider())
        .addService(IStorageWatcherNode::class.java, StackWatcher())
        .addService(ICraftingProvider::class.java, CraftingProvider())
        .addService(IGridTickable::class.java, Ticker())

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

        override fun onAddedChipSupplier() {
            updatePatterns()
        }

        override fun onRemovedChipSupplier() {
            updatePatterns()
        }

        override fun onChipValuesChanged() {
            updatePatterns()
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

    private fun alertDevice() {
        if (mainNode.isActive) {
            mainNode.ifPresent { grid, node ->
                grid.tickManager.alertDevice(node)
            }
        }
    }

    fun notifyNeighbors() {
        host.getBlockEntity()?.invalidateCapabilities()
    }

    fun gridChanged() {
        _tanks.reset()
        updatePatterns()
        notifyNeighbors()
    }

    fun writeToNBT(tag: CompoundTag, @Suppress("unused") registries: HolderLookup.Provider) {
        tag.putInt("priority", priority)

        tag.put("return_inventory", returnInventory.writeToTag(registries))

        pendingTask?.let { tag.putCodec(PENDING_TASK_CODEC, it) }
        tag.putCodec(PUSHED_REPLICATION_TASKS_CODEC, pushedReplicationTasks)
    }

    fun readFromNBT(tag: CompoundTag, @Suppress("unused") registries: HolderLookup.Provider) {
        notifyNeighbors()
        priority = tag.getInt("priority")

        returnInventory.readFromTag(tag.getList("return_inventory", Tag.TAG_COMPOUND.toInt()), registries)

        pendingTask = tag.getCodec(PENDING_TASK_CODEC)
        pushedReplicationTasks = tag.getCodec(PUSHED_REPLICATION_TASKS_CODEC) ?: ObjectOpenHashSet()
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
        MENetworkMatterTankList(cachedMatters, grid.storageService.inventory, source)
    }
    private val tanks by _tanks

    override fun getTanks(): List<IMatterTank> {
        return tanks
    }

    override fun getPriority(): Int = _priority
}
