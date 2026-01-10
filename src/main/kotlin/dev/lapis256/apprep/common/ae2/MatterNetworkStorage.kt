package dev.lapis256.apprep.common.ae2

import appeng.api.config.Actionable
import appeng.api.networking.security.IActionSource
import appeng.api.stacks.AEKey
import appeng.api.stacks.KeyCounter
import appeng.api.storage.MEStorage
import com.buuz135.replication.api.matter_fluid.IMatterTank
import com.buuz135.replication.api.network.IMatterTanksSupplier
import com.buuz135.replication.network.MatterNetwork
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.api.ae2.MatterKey
import dev.lapis256.apprep.api.text.AppRepGuiText
import dev.lapis256.apprep.common.logic.ReplicationConnectorLogicHost
import net.minecraft.network.chat.Component


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

    private fun collectAvailableStacks(): KeyCounter =
        KeyCounter().apply {
            AppliedReplicaticsAPI.LOGGER.debug("Collecting available matter stacks from Matter Network Storage")
            matterNetwork.matterStacksHolders
                .asSequence()
                .filter { it.level.isLoaded(it.pos) }
                .map { it.level.getBlockEntity(it.pos) }
                .filterNot { it is ReplicationConnectorLogicHost }
                .mapNotNull { it as? IMatterTanksSupplier }
                .sortedBy(IMatterTanksSupplier::getPriority)
                .flatMap(IMatterTanksSupplier::getTanks)
                .map(IMatterTank::getMatter)
                .forEach { matterStack ->
                    val key = MatterKey.of(matterStack.matterType)
                    this[key] += matterStack.amount.toLong()
                }
        }

    private var _availableStacksCache: KeyCounter? = null
    private val availableStacksCache
        get() = _availableStacksCache
            ?: collectAvailableStacks().also { _availableStacksCache = it }

    fun invalidateCache() {
        _availableStacksCache = null
    }

    override fun getAvailableStacks(out: KeyCounter) {
        out.addAll(availableStacksCache)
    }
}
