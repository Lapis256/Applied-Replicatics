package dev.lapis256.apprep.common.ae2

import appeng.api.config.Actionable
import appeng.api.networking.security.IActionSource
import appeng.api.stacks.AEKey
import appeng.api.stacks.KeyCounter
import appeng.api.storage.MEStorage
import com.buuz135.replication.api.matter_fluid.MatterTank
import com.buuz135.replication.api.network.IMatterTanksSupplier
import com.buuz135.replication.network.MatterNetwork
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.api.ae2.MatterKey
import dev.lapis256.apprep.api.text.AppRepGuiText
import dev.lapis256.apprep.common.logic.ReplicationConnectorLogicHost
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import net.minecraft.network.chat.Component
import kotlin.math.roundToLong


class MatterNetworkStorage(private val matterNetwork: MatterNetwork) : MEStorage {
    override fun getDescription(): Component = AppRepGuiText.MATTER_NETWORK_STORAGE.text()

    override fun isPreferredStorageFor(what: AEKey?, source: IActionSource?): Boolean {
        return super.isPreferredStorageFor(what, source)
    }

    override fun insert(what: AEKey, amount: Long, mode: Actionable, source: IActionSource): Long {
        return super.insert(what, amount, mode, source)
    }

    override fun extract(what: AEKey, amount: Long, mode: Actionable, source: IActionSource): Long {
        return super.extract(what, amount, mode, source)
    }

    private fun collectAvailableTanks(): ObjectLinkedOpenHashSet<MatterTank> =
        matterNetwork.matterStacksHolders
            .asSequence()
            .filter { it.level.isLoaded(it.pos) }
            .map { it.level.getBlockEntity(it.pos) }
            .filterNot { it is ReplicationConnectorLogicHost }
            .mapNotNull { it as? IMatterTanksSupplier }
            .sortedBy(IMatterTanksSupplier::getPriority)
            .flatMap(IMatterTanksSupplier::getTanks)
            .filterIsInstance<MatterTank>()
            .toSet()
            .let { ObjectLinkedOpenHashSet<MatterTank>(it) }

    private var _availableTanksCache: ObjectLinkedOpenHashSet<MatterTank>? = null
    private val availableTanksCache
        get() = _availableTanksCache
            ?: collectAvailableTanks().also { _availableTanksCache = it }

    private fun collectAvailableStacks(): KeyCounter =
        KeyCounter().apply {
            AppliedReplicaticsAPI.LOGGER.debug("Collecting available matter stacks from Matter Network Storage")
            availableTanksCache
                .forEach { tank ->
                    val matterStack = tank.matter
                    val key = MatterKey.of(matterStack.matterType)
                    this[key] += matterStack.amount.roundToLong()
                }
        }

    private var _availableStacksCache: KeyCounter? = null
    private val availableStacksCache
        get() = _availableStacksCache
            ?: collectAvailableStacks().also { _availableStacksCache = it }

    override fun getAvailableStacks(out: KeyCounter) {
        out.addAll(availableStacksCache)
    }

    fun invalidateCache() {
        _availableTanksCache = null
        _availableStacksCache = null
    }
}
