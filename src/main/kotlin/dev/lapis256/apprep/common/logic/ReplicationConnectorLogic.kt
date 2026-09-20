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
import appeng.api.upgrades.IUpgradeInventory
import appeng.api.upgrades.UpgradeInventories
import appeng.api.util.AECableType
import com.buuz135.replication.api.IMatterType
import com.buuz135.replication.api.matter_fluid.IMatterTank
import com.buuz135.replication.api.network.IMatterTanksConsumer
import com.buuz135.replication.api.network.IMatterTanksSupplier
import com.buuz135.replication.api.pattern.IMatterPatternHolder
import com.buuz135.replication.api.task.IReplicationTask
import com.buuz135.replication.api.task.ReplicationTask
import com.buuz135.replication.block.tile.ReplicatorBlockEntity
import com.buuz135.replication.network.MatterNetwork
import com.hrznstudio.titanium.block_network.Network
import com.hrznstudio.titanium.block_network.element.NetworkElement
import com.mojang.logging.LogUtils
import com.mojang.serialization.Codec
import dev.lapis256.apprep.api.ae2.stack.MatterKey
import dev.lapis256.apprep.api.connector.ReplicationConnectorExtension
import dev.lapis256.apprep.api.connector.ReplicationConnectorExtensionContext
import dev.lapis256.apprep.api.connector.ReplicationConnectorExtensions
import dev.lapis256.apprep.api.connector.ReplicationConnectorUpgrades
import dev.lapis256.apprep.api.extension.getCodec
import dev.lapis256.apprep.api.extension.putCodec
import dev.lapis256.apprep.api.replication.matter_network.MatterNetworkListener
import dev.lapis256.apprep.api.replication.matter_network.addListener
import dev.lapis256.apprep.api.replication.matter_network.removeListener
import dev.lapis256.apprep.api.replication.task.MEReplicationTask
import dev.lapis256.apprep.api.replication.task.isMEAutoCraftingTask
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
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import org.slf4j.Logger


class ReplicationConnectorLogic(gridNode: IManagedGridNode, val host: ReplicationConnectorLogicHost) :
    IMatterTanksConsumer,
    IMatterTanksSupplier {

    companion object {
        val LOGGER: Logger = LogUtils.getLogger()

        // TODO(next major): Remove legacy pending-task save compatibility when bumping the mod major version.
        val PENDING_TASK_CODEC: Codec<PendingTask> = PendingTask.CODEC.fieldOf("pending_task").codec()
    }

    private val source: IActionSource get() = IActionSource.ofMachine(mainNode::getNode)

    private val extensions = mutableListOf<ReplicationConnectorExtension>()

    val upgrades: IUpgradeInventory = UpgradeInventories.forMachine(
        host.upgradableItem,
        ReplicationConnectorUpgrades.slotCount,
    ) {
        host.saveChanges()
        extensions.forEach(ReplicationConnectorExtension::onUpgradesChanged)
    }

    private val extensionContext = object : ReplicationConnectorExtensionContext {
        override val managedNode: IManagedGridNode = gridNode
        override val upgrades get() = this@ReplicationConnectorLogic.upgrades
        override val actionSource: IActionSource get() = IActionSource.ofMachine(gridNode::getNode)
        override val matterNetwork: MatterNetwork? get() = host.matterNetwork

        override fun saveChanges() = host.saveChanges()
    }

    private var _priority: Int = 0
        set(value) {
            field = value
            host.saveChanges()
            remountStorage()
            refreshCraftingProvider()
        }

    fun setPriority(newValue: Int) {
        _priority = newValue
    }

    // TODO(next major): Remove when save compatibility can be broken (e.g. 21.1-2.x or a new Minecraft line such as 26.1-x.y.z).
    private var pendingTask: PendingTask? = null

    val delegatingStorage = DelegatingMatterNetworkStorage()
    private var connectedMatterNetwork: MatterNetwork? = null

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
        upgrades.asSequence()
            .filterNot(ItemStack::isEmpty)
            .forEach(drops::add)
    }

    fun clearContent() {
        returnInventory.clear()
        upgrades.clear()
    }

    private val _patterns = ResettableLazy {
        val matterNetwork = connectedMatterNetwork ?: return@ResettableLazy emptyList()
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
        refreshCraftingProvider()
    }

    inner class CraftingProvider : ICraftingProvider {

        private fun getAvailableReplicatorPositions(matterNetwork: MatterNetwork): Set<Long> {
            return matterNetwork.replicators
                .asSequence()
                .filter { it.level.isLoaded(it.pos) }
                .mapNotNull { it.level.getBlockEntity(it.pos) as? ReplicatorBlockEntity }
                .filterNot { it.isInfinite }
                .map { it.blockPos.asLong() }
                .toSet()
        }

        private fun canPushNextPattern(): Boolean {
            // Finish a queued 21.x batch before accepting work using the new task model.
            if (pendingTask != null) {
                return false
            }

            val matterNetwork = host.matterNetwork ?: return false
            val availableReplicators = getAvailableReplicatorPositions(matterNetwork)
            if (availableReplicators.isEmpty()) {
                return false
            }

            val tasks = matterNetwork.taskManager.pendingTasks.values

            val occupiedReplicators = tasks
                .asSequence()
                .flatMap { it.replicatorsOnTask.asSequence() }
                .count { it in availableReplicators }

            val queuedAutoCraftingTasks = tasks.count { task ->
                task is ReplicationTask &&
                    task.isMEAutoCraftingTask &&
                    task.replicatorsOnTask.isEmpty()
            }

            return occupiedReplicators + queuedAutoCraftingTasks < availableReplicators.size
        }

        override fun getAvailablePatterns(): List<IPatternDetails> = patterns

        override fun pushPattern(patternDetails: IPatternDetails, inputHolder: Array<KeyCounter>): Boolean {
            if (!canPushNextPattern()) {
                return false
            }

            val matterNetwork = host.matterNetwork ?: return false
            val level = host.matterNetworkElement?.level as? ServerLevel ?: return false
            val pos = host.matterNetworkElement?.pos ?: return false

            val output = patternDetails.outputs[0] ?: return false
            val item = output.what as? AEItemKey ?: return false

            val extracted = Object2LongOpenHashMap<IMatterType>()
            inputHolder.forEach { counter ->
                counter.forEach {
                    val what = it.key as? MatterKey ?: return@forEach
                    extracted[what.type] = extracted.getLong(what.type) + it.longValue
                }
            }

            val task = MEReplicationTask.create(
                extracted,
                item,
                1,
                pos,
                IReplicationTask.Mode.SINGLE,
                autoCraftingTask = true
            )

            matterNetwork.taskManager.addTask(task)
            matterNetwork.onTaskValueChanged(task, level)

            return true
        }

        override fun isBusy(): Boolean = !canPushNextPattern()

        override fun getPatternPriority() = priority
    }

    inner class Ticker : IGridTickable {
        override fun getTickingRequest(node: IGridNode) =
            TickingRequest(5, 120, true)

        override fun tickingRequest(node: IGridNode, ticksSinceLastCall: Int): TickRateModulation {
            if (!shouldTick()) {
                return TickRateModulation.SLEEP
            }

            val taskPushed = pushLegacyPendingTask()
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

        // TODO(next major): Remove legacy batched-task migration when bumping the mod major version.
        private fun pushLegacyPendingTask(): Boolean {
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

            pendingTask = null
            host.saveChanges()

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
        .also { extensions += ReplicationConnectorExtensions.install(it, extensionContext) }

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

    private fun connectMatterNetwork(network: Network) {
        val matterNetwork = network as? MatterNetwork
            ?: return LOGGER.error("Connected network is not MatterNetwork: {}", network)

        if (connectedMatterNetwork === matterNetwork) {
            return
        }

        connectedMatterNetwork?.removeListener(matterNetworkListener)

        connectedMatterNetwork = matterNetwork
        delegatingStorage.storage = MatterNetworkStorage(matterNetwork)
        matterNetwork.addListener(matterNetworkListener)

        remountStorage()
        updatePatterns()
    }

    private fun disconnectMatterNetwork(network: Network) {
        val matterNetwork = network as? MatterNetwork
            ?: return LOGGER.error("Disconnected network is not MatterNetwork: {}", network)

        if (connectedMatterNetwork !== matterNetwork) {
            return
        }

        matterNetwork.removeListener(matterNetworkListener)
        connectedMatterNetwork = null
        delegatingStorage.storage = null

        remountStorage()
        updatePatterns()
    }

    inner class NetworkElementListenerImpl : NetworkElementListener {
        override fun onAddedNetwork(network: Network) {
            connectMatterNetwork(network)
        }

        override fun onRemoveNetwork(network: Network) {
            disconnectMatterNetwork(network)
        }
    }

    private val networkElementListener by lazy { NetworkElementListenerImpl() }

    fun addNetworkElementListener(element: NetworkElement) {
        if (!element.addListener(networkElementListener)) {
            return
        }

        element.network?.let(::connectMatterNetwork)
    }

    fun removeNetworkElementListener(element: NetworkElement) {
        if (!element.removeListener(networkElementListener)) {
            return
        }

        element.network?.let(::disconnectMatterNetwork)
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

    fun gridChanged() {
        _tanks.reset()
        updatePatterns()
    }

    fun writeToNBT(tag: CompoundTag, registries: HolderLookup.Provider) {
        tag.putInt("priority", priority)

        tag.put("return_inventory", returnInventory.writeToTag(registries))

        upgrades.writeToNBT(tag, "upgrades", registries)

        // TODO(next major): Remove legacy pending-task persistence when bumping the mod major version.
        pendingTask?.let { tag.putCodec(PENDING_TASK_CODEC, it) }
    }

    fun readFromNBT(tag: CompoundTag, registries: HolderLookup.Provider) {
        _priority = tag.getInt("priority")

        returnInventory.readFromTag(tag.getList("return_inventory", Tag.TAG_COMPOUND.toInt()), registries)

        upgrades.readFromNBT(tag, "upgrades", registries)

        // TODO(next major): Remove legacy pending-task migration when bumping the mod major version.
        pendingTask = tag.getCodec(PENDING_TASK_CODEC)
    }

    private fun remountStorage() {
        IStorageProvider.requestUpdate(mainNode)
    }

    private fun refreshCraftingProvider() {
        ICraftingProvider.requestUpdate(mainNode)
    }

    var wasOnline = false

    fun onMainNodeStateChanged() {
        val currentOnline: Boolean = mainNode.isOnline
        if (wasOnline != currentOnline) {
            wasOnline = currentOnline
            host.saveChanges()
            remountStorage()
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
